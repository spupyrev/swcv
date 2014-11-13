package edu.cloudy.graph;

import java.util.HashSet;

public class CycleDetector {
	private Graph g;
	private HashSet<Vertex> seen;

	public CycleDetector(Graph g) {
		this.g = g;
	}

	private boolean dfs(Vertex parent, Vertex v) {
		seen.add(v);

		for (Edge e : g.edgesOf(v)) {
			Vertex other = g.getOtherSide(e, v);

			if (other.equals(parent))
				continue;

			if (seen.contains(other))
				return true;

			if (dfs(v, other))
				return true;
		}

		return false;
	}

	public boolean hasCycle() {
		seen = new HashSet<Vertex>();
		for (Vertex v : g.vertexSet())
			if (!seen.contains(v))
				if (dfs(null, v))
					return true;
		
		return false;
	}
}
