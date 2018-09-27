package edu.usc.cache;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * An LFU cache.
 * 
 * @author Haoyu Huang <haoyuhua@usc.edu> University of Southern California
 *
 */
public class LFUCache implements Cache {

	private class DoubleLinkedList {
		private int n = 0;
		private Node head;
		private Node tail;

		public void add(Node node) {
			if (head == null) {
				head = node;
			} else {
				tail.next = node;
				node.prev = tail;
			}
			tail = node;
			n++;
		}

		public void remove(Node node) {
			if (node.next == null) {
				tail = node.prev;
				if (tail != null) {
					tail.next = null;
				}
			} else {
				node.next.prev = node.prev;
			}
			if (head.key == node.key) {
				head = node.next;
				if (head != null) {
					head.prev = null;
				}
			} else {
				node.prev.next = node.next;
			}
			n--;
		}

		public Node head() {
			return head;
		}

		public int size() {
			return n;
		}
	}

	private Map<Long, Node> values = new HashMap<>();
	private Map<Long, Long> counts = new HashMap<>();
	private TreeMap<Long, DoubleLinkedList> frequencies = new TreeMap<>();
	private final long capacity;
	private long currentCapacity;

	public LFUCache(long capacity) {
		this.capacity = capacity;
		this.currentCapacity = 0;
	}

	public int get(long key) {
		if (!values.containsKey(key)) {
			return -1;
		}
		Node node = values.get(key);
		Long frequency = counts.get(key);
		DoubleLinkedList list = frequencies.get(frequency);
		list.remove(node);
		node.prev = null;
		node.next = null;
		removeIfListEmpty(frequency, list);
		frequencies.compute(frequency + 1, (k, dlist) -> {
			if (dlist == null) {
				dlist = new DoubleLinkedList();
			}
			dlist.add(node);
			return dlist;
		});
		counts.put(key, frequency + 1);
		return values.get(key).value;
	}

	@Override
	public void delete(long key) {
		Node removedNode = values.remove(key);
		Long frequency = counts.remove(key);
		if (removedNode != null) {
			DoubleLinkedList list = frequencies.get(frequency);
			list.remove(removedNode);
			removeIfListEmpty(frequency, list);
			currentCapacity -= removedNode.size;
		}
	}

	public int set(long key, int value, int size) {
		int evictions = 0;
		if (!values.containsKey(key)) {
			Node node = new Node(key, value, size);
			while (currentCapacity + size > capacity) {
				long lowestCount = frequencies.firstKey(); // smallest frequency
				DoubleLinkedList list = frequencies.get(lowestCount);
				Node nodeTodelete = list.head(); // first item (LRU)
				list.remove(nodeTodelete);

				long keyToDelete = nodeTodelete.key;
				removeIfListEmpty(lowestCount, list);
				values.remove(keyToDelete);
				counts.remove(keyToDelete);
				currentCapacity -= nodeTodelete.size;
				evictions++;
			}

			values.put(key, node);
			counts.put(key, 1l);
			currentCapacity += size;
			frequencies.compute(1l, (k, dlist) -> {
				if (dlist == null) {
					dlist = new DoubleLinkedList();
				}
				dlist.add(node);
				return dlist;
			});
		}
		return evictions;
	}

	private void removeIfListEmpty(long frequency, DoubleLinkedList list) {
		if (list.size() == 0) {
			frequencies.remove(frequency); // remove from map if list is empty
		}
	}

	private class Node {
		private final long key;
		private final int value;
		private final int size;
		private Node next;
		private Node prev;

		public Node(long key, int value, int size) {
			this.key = key;
			this.value = value;
			this.size = size;
		}
	}

	@Override
	public int size() {
		return 0;
	}
}
