package edu.usc.db;

import java.util.HashMap;

public class LRUCache implements Cache {
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
	long availableCapacity;
	HashMap<Long, Node> map = new HashMap<Long, Node>();
	Node head = null;
	Node end = null;
	int index = 0;

	public LRUCache(int index, long capacity) {
		this.index = index;
		this.capacity = capacity;
		availableCapacity = 0;
	}

	public void clear() {
		map.clear();
		head = null;
		end = null;
		availableCapacity = 0;
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
			availableCapacity -= node.size;
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

	public int set(long key, int value, int size) {
		int evictions = 0;
		if (size >= capacity) {
			return -1;
		}
		if (map.containsKey(key)) {
			Node old = map.get(key);
			old.value = value;
			remove(old);
			setHead(old);
		} else {
			Node created = new Node(key, value, size);
			while (availableCapacity + size > capacity) {
				availableCapacity -= end.size;
				map.remove(end.key);
				remove(end);
				evictions++;
			}
			setHead(created);
			map.put(key, created);
			availableCapacity += size;
		}
		return evictions;
	}

	@Override
	public int size() {
		return map.size();
	}
}
