// Nicolas Bofill
/* This program creates and manages weight-based leftist heaps. A node within
the heap holds generic types Key and Value, where Key must be a comparable
type. The heap is ordered as a max heap and provides functionality that 
allows the users to merge two heaps, insert elements, remove the root, and
update the Key of any node within the heap using a locator.
 */
package cmsc420_s23;

import java.util.ArrayList;

public class WtLeftHeap<Key extends Comparable<Key>, Value> {
	
	private Node root;
	private int size;

	/*A node contains a key, value, and pointers to it's 
	parent and both its children */
	private class Node{
		private Key key;
		private Value value;

		private Node parent, left, right;
		private Locator loc;
	}

	/*Locator class will store nodes in an ArrayList representation
	of a heap*/
	public class Locator {
		private Node node;
	}

	/* Weight is a private helper function that that takes in a node and
	 * returns the amount of nodes in its tree (root, left subtree and 
	 * right subtree)
	 */
	private int weight(Node node) {
		if (node.left == null && node.right == null) {
			return 1;
		//iterate left subtree
		} else if (node.left == null && node.right != null) {
			return 1 + weight(node.right);
		//iterate right subtree
		} else if (node.left != null && node.right == null) {
			return 1 + weight(node.left);
		//iterate left and right subtree
		} else {
			return 1 + weight(node.right) + weight(node.left);
		}
	}

	/* Merge is a helper function that takes in two nodes and merges
	 * the heapss together into one node. The method then returns 
	 * the root of the new heap. 
	*/
	private Node merge(Node r1, Node r2) {
		/* If one node is null then no merging needs to take place
		 * so return non null node
		 */
		if (r1 == null) {
			return r2;
		} 
		
		if (r2 == null) {
			return r1;
		}

		/* Larger key needs to come first in max heap */
		if (r1.key.compareTo(r2.key) < 0) {
			Node temp = r1;
			r1 = r2;
			r2 = temp;
		}

		if (r1.left == null) {
			r1.left = r2;
			r2.parent = r1;

		} else {
			r1.right = merge(r1.right, r2);
			r1.right.parent = r1;
			
			/* Maintain leftist property if it has been broken*/
			if (weight(r1.left) < weight(r1.right)) {
				Node temp = r1.left;
				r1.left = r1.right;
				r1.right = temp;
			}
		}
		return r1;
	}

	/* listHelper is a helper method that does a right-to-left preorder
	 * traversal of the heap and formats the node information into a string
	 * that is then returned in an ArrayList<String>
	*/
	private ArrayList<String> listHelper(Node curr, ArrayList<String> list) {
		if (curr == null) {
			list.add("[]");
		} else {
			list.add("(" + curr.key + ", " + curr.value + ") ["
			+ weight(curr) + "]");

			listHelper(curr.right, list);
			listHelper(curr.left, list);
		}

		return list;
	}

	public WtLeftHeap() {
		root = null;
		size = 0;
	}

	/*private variable size keeps track of each element 
	that has been added or removed*/
	public int size() { 
		return size;
	}


	public void clear() {
		root = null;
		size = 0;
	}

	/* Insert makes a new node containing the given Key and Value and adds
	 * it to the current heap. The function returns a locator that holds a
	 * reference to the newly added node.
	*/
	public Locator insert(Key x, Value v) {
		//create and initialize new node
		Node newNode = new Node();
		newNode.key = x;
		newNode.value = v;
		newNode.parent = null;
		newNode.left = null;
		newNode.right = null;

		//create and assign new locator
		Locator newLoc = new Locator();
		newLoc.node = newNode;
		newNode.loc = newLoc;

		WtLeftHeap<Key, Value> newHeap = new WtLeftHeap<Key, Value>();

		newHeap.root = newNode;
		newHeap.size += 1;

		//make new node a single-node heap and merge it with current heap
		root = merge(root, newHeap.root);

		size += newHeap.size;
		return newNode.loc; 
	}

	/* mergeWith takes in a heap and returns a new heap that merges
	 * the calling heap and parameter heap.
	*/
	public void mergeWith(WtLeftHeap<Key, Value> h2) {
		if ((this == h2) || (h2 == null)) {

		} else {
			root = merge(root, h2.root);

			size += h2.size;
			h2.clear();
		}
	}

	/* extract removes the root of the heap and returns its value. After
	 * removing the root the heap is reorganized. If the heap does not have
	 * a root element, an error is thrown.
	*/
	public Value extract() throws Exception {
		if (root == null) {
			throw new Exception("Extract from empty heap");
		} 

		Value answer = root.value;
		
		// remove any pointers to the root
		if (root.right != null) {
			root.right.parent = null;
		}
		
		if (root.left != null) {
			root.left.parent = null;
		}
		
		//reorganize heap with merge
		root = merge(root.left, root.right);
		size -= 1;
		return answer;
	}

	/* updateKey takes in a locator referencing an existing node and a Key.
	 * The method updates the referenced node to contain the new Key,
	 * therefore requiring the heap to be reorganized. 
	 */
	public void updateKey(Locator loc, Key x) throws Exception {

		//update the nodes key
		loc.node.key = x;
		int endCheck = 0;

		//Check if node will be swapping with parents
		if (loc.node.parent != null) {
			//If key is greater than its parent key, swapping is needed
			if (x.compareTo(loc.node.parent.key) > 0) {
				Node swap = loc.node.parent;
				Node curr = loc.node;
				//loop until swappin is no longer needed
				while (swap != null && x.compareTo(swap.key) > 0) {
					//swap the nodes
					Key tempKey = swap.key;
					Value tempValue = swap.value;

					swap.key = curr.key;
					swap.value = curr.value;

					curr.key = tempKey;
					curr.value = tempValue;

					curr.loc.node = swap;
					swap.loc.node = curr;

					Locator tempLoc = curr.loc;
					curr.loc = swap.loc;
					swap.loc = tempLoc;

					curr = swap;
					swap = swap.parent;
				}
				/*endCheck prevents the method from going through children
				 * swapping loop since the swapping already occurred with 
				 * the parents
				*/
				endCheck = 1;
			} 
		}

		//if we did not swap with parents, then we check to swap with children

		Node curr = loc.node;
		int path = 0;
		while (endCheck == 0 && (curr.right != null || curr.left != null)) {
			path = 0;
			if (curr.right != null && curr.left != null) {
				if (x.compareTo(curr.right.key) < 0 && x.compareTo(curr.left.key) < 0) {
					//if both children exist and the key is smaller than both
					//keys, swap with the largest child
					if (curr.right.key.compareTo(curr.left.key) > 0) {
						path = 1;
					} else {
						path = 2;
					}
					//only the right child is larger
				} else if (x.compareTo(curr.right.key) < 0) {
					path = 1;
					//only the left child is larger
				} else if (x.compareTo(curr.left.key) < 0) {
					path = 2;
					//no children are smaller, so we are done swapping
				} else {
					endCheck = 1;
				}
				//when only the right child exists and is larger
			} else if (curr.right != null && x.compareTo(curr.right.key) < 0) {
				path = 1;
				//when only the left child exists and is larger
			} else if (curr.left != null && x.compareTo(curr.left.key) < 0) {
				path = 2;
			} else {
				endCheck = 1;
			}

			/* when path = 1 we are swapping with the right child
			 * when path = 2 we are swapping with the left child
			 * when path = 0 no swapping should take place
			 */
			if (path != 0) {
				Node swap = path == 1 ? curr.right : curr.left;
				
				Key tempKey = swap.key;
				Value tempValue = swap.value;

				swap.key = curr.key;
				swap.value = curr.value;

				curr.key = tempKey;
				curr.value = tempValue;

				curr.loc.node = swap;
				swap.loc.node = curr;

				Locator tempLoc = curr.loc;
				curr.loc = swap.loc;
				swap.loc = tempLoc;

				curr = swap;
			}
		}
	}
	
	/*Return the key of the root if it exists. If not return null*/
	public Key peekKey() {
		return root == null ? null: root.key;
	}

	/*Return the value of the root if it exists. If not return null*/
	public Value peekValue() {
		return root == null ? null: root.value;
	}

	/*Use right-to-left preorder traversal to convert heap into 
	 * a ArrayList<String>
	*/
	public ArrayList<String> list() {
		return listHelper(root, new ArrayList<String>());
	}	
}

