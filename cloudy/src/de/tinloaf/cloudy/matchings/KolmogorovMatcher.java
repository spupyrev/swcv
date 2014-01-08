package de.tinloaf.cloudy.matchings;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import de.tinloaf.cloudy.graph.Edge;
import de.tinloaf.cloudy.graph.Vertex;
import de.tinloaf.cloudy.graph.WordGraph;

public class KolmogorovMatcher {
	private long objRef;
	private WordGraph g;

	private Map<Vertex,Integer> vertexIdMap;
	private Map<Edge,Integer> edgeIdMap;
	
	private native long initialize(int vertexCt, int edgeCt);
	private native int addEdge(long ref, int v1, int v2, double weight);
	private native void solveEx(long ref);
	private native boolean isMatched(long ref, int edgeId);
	private native void free(long ref);
	
	public KolmogorovMatcher(WordGraph g) {
		System.out.println(System.getProperty("java.library.path"));
		System.loadLibrary("Kolmogorov");
		this.g = g;
		this.objRef = this.initialize(g.vertexSet().size(), g.edgeSet().size());
	}
	
	public void solve() {
		this.solveEx(this.objRef);
	}
	
	public void dispose() {
		this.free(this.objRef);
		this.objRef = 0;
	}
	
	public void init() {
		this.vertexIdMap = new HashMap<Vertex,Integer>();
		Iterator<Vertex> vIt = this.g.vertexSet().iterator();
		
		int i = 0;
		while (vIt.hasNext()) {
			Vertex v = vIt.next();
			this.vertexIdMap.put(v, i++);
		}
		
		Iterator<Edge> eIt = this.g.edgeSet().iterator();
		while (eIt.hasNext()) {
			Edge e = eIt.next();
			
			int edgeId = this.addEdge(this.objRef, this.vertexIdMap.get(this.g.getEdgeSource(e)),
					this.vertexIdMap.get(this.g.getEdgeTarget(e)), 
					this.g.getEdgeWeight(e));
			this.edgeIdMap.put(e, edgeId);
		}
	}
	
	public List<Edge> getMatchedEdges() {
		List<Edge> res = new LinkedList<Edge>();
		
		for (Edge e: this.edgeIdMap.keySet()) {
			if (this.isMatched(this.objRef, this.edgeIdMap.get(e))) {
				res.add(e);
			}
		}
		
		return res;
	}
}
