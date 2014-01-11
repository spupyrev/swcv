package edu.cloudy.graph;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class MaxSpanningTreeBuilder {
	private WordGraph graph;

	public MaxSpanningTreeBuilder(WordGraph g) {
		this.graph = g;
	}

	public WordGraph getTree(Set<Edge> edges) {
		WordGraph tree = new WordGraph();

		int n = graph.vertexSet().size();
		Vertex[] v = new Vertex[n];
		Vertex[] parent = new Vertex[n];
		double[] dist = new double[n];
		boolean[] used = new boolean[n];

		Map<Vertex, Integer> vToIndex = new HashMap<Vertex, Integer>();
		int ind = 0;
		for (Vertex vv : graph.vertexSet()) {
			v[ind] = vv;
			vToIndex.put(vv, ind);
			ind++;
		}

		for (int i = 0; i < n; i++) {
			dist[i] = Double.POSITIVE_INFINITY;
			used[i] = false;
			parent[i] = null;
			tree.addVertex(v[i]);
		}

		dist[0] = 0;
		for (int i = 0; i < n; i++) {
			int tecmin = -1;
			for (int j = 0; j < n; j++)
				if (!used[j])
					if (tecmin == -1 || dist[tecmin] > dist[j])
						tecmin = j;

			used[tecmin] = true;
			if (Double.isInfinite(dist[i]))
				dist[i] = 0;

			for (Edge e : graph.edgesOf(v[tecmin])) {
				Vertex u = graph.getOtherSide(e, v[tecmin]);
				int ui = vToIndex.get(u);
				double d = -graph.getEdgeWeight(e);
				if (!used[ui] && dist[ui] > d) {
					dist[ui] = d;
					parent[ui] = v[tecmin];
				}
			}
		}

		edges.clear();
		for (int i = 1; i < n; i++) {
			if (parent[i] == null)
				continue;

			assert (parent[i] != null);
			Edge existingEdge = graph.getEdge(v[i], parent[i]);
			edges.add(existingEdge);
			
			Edge newEdge = tree.addEdge(v[i], parent[i]);
			tree.setEdgeWeight(newEdge, graph.getEdgeWeight(existingEdge));
		}

		return tree;
	}

}
