package de.tinloaf.cloudy.graph;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.PriorityQueue;

import de.tinloaf.cloudy.text.Word;
import de.tinloaf.cloudy.utils.DisjointSet;

public class GreedyHamiltonianHeuristic {
	private WordGraph g;
	private DisjointSet<Word> unionFind;
	private HashMap<Word, Path> paths;
	private HashSet<Path> uniquePaths;
	
	public GreedyHamiltonianHeuristic(WordGraph g) {
		this.g = g;
	}
	
	private Path getWordPath(Word w) {
		return this.paths.get(unionFind.getRepresentant(w));
	}
	
	private void setWordPath(Word w, Path p) {
		this.paths.put(unionFind.getRepresentant(w), p);
	}
	
	public Collection<Path> getPaths() {
		return this.uniquePaths;
	}
	
	private void condensePaths() {
		this.uniquePaths = new HashSet<Path>();
		
		for (Word w: this.paths.keySet()) {
			this.uniquePaths.add(this.getWordPath(w));
		}
	}
	
	public void run() {
		this.paths = new HashMap<Word, Path>();
		this.unionFind = new DisjointSet<Word>();

		PriorityQueue<Edge> queue = new PriorityQueue<Edge>(g.edgeSet().size(), g.new EdgeComparator(true));
		queue.addAll(g.edgeSet());
		
		Edge next = queue.peek();
		while (next != null) {
			next = queue.poll();
			
			if (next == null) {
				break;
			}
			
			Word from = g.getEdgeSource(next);
			Word to = g.getEdgeTarget(next);
			
			if (this.unionFind.contains(from)) {
				if (!this.getWordPath(from).endsIn(from)) {
					// would form a claw
					continue;
				}
			}
			
			if (this.unionFind.contains(to)) {
				if (!this.getWordPath(to).endsIn(to)) {
					// would form a claw
					continue;
				}
			}

			if(this.unionFind.isJoined(to, from)) {
				// would close a cycle
				continue;
			}
			
			// build connected paths
			Path edgePath = new Path(g, next);
			Path startPlusEdge;
			if (this.unionFind.contains(from)) {
				startPlusEdge = new Path(this.getWordPath(from), edgePath);
			} else {
				startPlusEdge = edgePath;
				this.unionFind.add(from);
			}
			
			Path joinedPath;
			if (this.unionFind.contains(to)) {
				joinedPath = new Path(this.getWordPath(to), startPlusEdge);
			} else {
				this.unionFind.add(to);
				joinedPath = startPlusEdge;
			}
			
			
			// join stuff in the union find
			this.unionFind.union(from, to);
			
			// Set the path of the joined construct
			this.setWordPath(from, joinedPath);
			
			assert(this.getWordPath(from) == this.getWordPath(to));
		}
		
		this.condensePaths();
	}
}
