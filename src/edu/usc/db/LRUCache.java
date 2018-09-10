package edu.usc.db;

import java.util.HashMap;

public class LRUCache {
	class Node {
		long key;
		int value;
		int size;
		Node pre;
		Node next;

		public Node(long key, int value, int size) {
			this.key = key;
			this.value = value;
			this.size = size;
		}
	}

	long capacity;
	long currentCapacity;
	HashMap<Long, Node> map = new HashMap<Long, Node>();
	Node head = null;
	Node end = null;
	int index = 0;

	public LRUCache(int index, long capacity) {
		this.index = index;
		this.capacity = capacity;
		currentCapacity = 0;
	}

	public void clear() {
		map.clear();
		head = null;
		end = null;
		currentCapacity = 0;
	}

	public int get(long key) {
		if (map.containsKey(key)) {
			Node n = map.get(key);
			remove(n);
			setHead(n);
			return n.value;
		}
		return -1;
	}

	public void delete(long key) {
		Node node = map.remove(key);
		if (node != null) {
			remove(node);
			currentCapacity -= node.size;
		}
	}

	public void remove(Node n) {
		if (n.pre != null) {
			n.pre.next = n.next;
		} else {
			head = n.next;
		}

		if (n.next != null) {
			n.next.pre = n.pre;
		} else {
			end = n.pre;
		}
	}

	public void setHead(Node n) {
		n.next = head;
		n.pre = null;

		if (head != null)
			head.pre = n;

		head = n;

		if (end == null)
			end = head;
	}

	public void set(long key, int value, int size) {
		if (size >= capacity) {
			return;
		}
		if (map.containsKey(key)) {
			Node old = map.get(key);
			old.value = value;
			remove(old);
			setHead(old);
		} else {
			Node created = new Node(key, value, size);
			while (currentCapacity + size > capacity) {
				currentCapacity -= end.size;
				map.remove(end.key);
				remove(end);
			}
			setHead(created);
			map.put(key, created);
			currentCapacity += size;
		}
	}
}
