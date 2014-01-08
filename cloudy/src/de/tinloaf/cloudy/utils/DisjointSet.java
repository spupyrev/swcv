package de.tinloaf.cloudy.utils;

import java.util.HashMap;

public class DisjointSet<T> {
	private HashMap<T,Node<T>> elements;
	
	private class Node<T> {
		private Node<T> parent;
		private T element;
		
		public Node(T element) {
			this.parent = null;
			this.element = element;
		}
		
		public Node<T> getParent() {
			if (this.parent == null) {
				return this;
			}
			
			Node<T> parent = this.parent.getParent();
			this.parent = parent;
			
			return parent;
		}
		
		public void putInto(Node<T> other) {
			this.getParent().parent = other.getParent();
		}
	}
	
	public DisjointSet () {
		this.elements = new HashMap<T,Node<T>>();
	}
	
	public boolean contains(T o) {
		return this.elements.containsKey(o);
	}
	
	public void add(T o) {
		this.elements.put(o, new Node<T>(o));
	}
	
	public void addTwo(T o1, T o2) {
		Node<T> rep1 = new Node<T>(o1);
		this.elements.put(o1, rep1);
		
		Node<T> rep2 = new Node<T>(o2);
		rep2.parent = rep1;
		
		this.elements.put(o2, rep2);
	}
	
	public T getRepresentant(T o) {
		return this.elements.get(o).getParent().element;
	}
	
	public void union(T o1, T o2) {
		this.elements.get(o1).putInto(this.elements.get(o2));
	}
	
	public boolean isJoined(T o1, T o2) {
		if (!this.contains(o1) || !this.contains(o2)) {
			return false;
		}
		
		return this.elements.get(o1).getParent() == this.elements.get(o2).getParent();
	}
}
