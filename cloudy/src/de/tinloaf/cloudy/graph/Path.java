package de.tinloaf.cloudy.graph;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;

import de.tinloaf.cloudy.text.Word;

public class Path extends LinkedList<Edge> {
	private static final long serialVersionUID = 1470061298056451458L;

	private WordGraph g;
	
	public Path(WordGraph g) {
		super();
		
		if (g == null) {
			throw new IllegalArgumentException("Null graph not allowed");
		}
		
		this.g = g;
	};
	
	private class WordIterator implements Iterator<Word> {
		private Iterator<Edge> edgeIt;
		private Path p;
		private Word last;
		
		public WordIterator(Path p) {
			this.edgeIt = p.iterator();
			this.p = p;
			this.last = null;
		}
		
		@Override
		public boolean hasNext() {
			return edgeIt.hasNext();
		}

		@Override
		public Word next() {
			if (this.last == null) {
				this.last = p.getStart();
				return p.getStart();
			}
			
			Edge e = this.edgeIt.next();
			if (Path.this.g.getEdgeSource(e) == this.last) {
				this.last = Path.this.g.getEdgeTarget(e);
				return Path.this.g.getEdgeTarget(e);
			} else {
				// TODO consistency
				this.last = Path.this.g.getEdgeSource(e);
				return Path.this.g.getEdgeSource(e);
			}
		}

		@Override
		public void remove() {
			this.edgeIt.remove();
		}
	}
	
	public Path(Path p1, Path p2) {
		super(p1);
		this.g = p1.g;
		
		assert(p1.g == p2.g);

		if (p1.getEnd() == p2.getStart()) {
			// p2 after p1
			this.addAll(p2);			
		} else if (p1.getStart() == p2.getEnd()) {
			// p1 after p2
			this.addAll(0, p2);
		} else if (p1.getStart() == p2.getStart()) {
			// p2 after p1'
			Collections.reverse(this);
			this.addAll(p2);
		} else if (p1.getEnd() == p2.getEnd()) {
			// p1' after p2
			Collections.reverse(this);
			this.addAll(0, p2);
		} else {	
			throw new IllegalArgumentException("Lists not conntected");
		}
	}
	
	public Path(WordGraph g, Edge e) {
		super();
		this.g = g;
		this.add(e);
	}
	
	public Iterator<Word> wordIterator() {
		return new WordIterator(this);
	}
	
	public void print() {
		StringBuilder sb = new StringBuilder();
		
		Iterator<Word> it = this.wordIterator();
		while (it.hasNext()) {
			if (sb.length() > 0) {
				sb.append("->");
			}
			
			sb.append(it.next().word);
		}
		
		System.out.println(sb.toString());
	}
	
	Word getStart() {
		Edge first = this.get(0);
		if (this.size() == 1) {
			return this.g.getEdgeSource(first);
		}
		
		Edge second = this.get(1);
		
		if ((this.g.getEdgeSource(first) != this.g.getEdgeSource(second)) 
				&& (this.g.getEdgeSource(first) != this.g.getEdgeTarget(second))) {
			return this.g.getEdgeSource(first);
		}
		
		return this.g.getEdgeTarget(first);
	}

	Word getEnd() {
		Edge first = this.getLast();
		if (this.size() == 1) {
			return this.g.getEdgeTarget(first);
		}
		
		// TODO speed up
		Edge second = this.get(this.size() - 2);
		
		if ((this.g.getEdgeSource(first) != this.g.getEdgeSource(second)) 
				&& (this.g.getEdgeSource(first) != this.g.getEdgeTarget(second))) {
			return this.g.getEdgeSource(first);
		}
		
		return this.g.getEdgeTarget(first);
	}

	public boolean endsIn(Word w) {
		return ((w == this.getEnd()) || (w == this.getStart()));
	}
}
