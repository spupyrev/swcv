package edu.cloudy.graph;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * Apr 25, 2013
 * Extract heaviest cycle cover via Hungarian matching
 */
public class CycleCoverExtractor {
	private WordGraph graph;
	private List<Edge> matchedEdges;

	public CycleCoverExtractor(WordGraph g) {
		this.graph = g;
	}

	public List<Edge> getMatchedEdges() {
		return this.matchedEdges;
	}

	public void runDirected() {
		int n = graph.getWords().size();
		if (n % 2 == 0)
			n++;

		double[][] cost = new double[n][n];

		//fill weights
		double maxWeight = 1;
		for (Edge e : graph.edgeSet())
			maxWeight = Math.max(graph.getEdgeWeight(e), maxWeight);

		maxWeight *= 1000;
		List<Vertex> vertices = new ArrayList<Vertex>(graph.vertexSet());
		for (int i = 0; i < vertices.size(); i++)
			for (int j = 0; j < vertices.size(); j++) {
				if (!graph.containsEdge(vertices.get(i), vertices.get(j)))
					continue;

				if (j > i && j - i <= n / 2)
					cost[i][j] = graph.getEdgeWeight(graph.getEdge(vertices.get(i), vertices.get(j)));
				if (j < i && j - i + n <= n / 2)
					cost[i][j] = graph.getEdgeWeight(graph.getEdge(vertices.get(i), vertices.get(j)));
			}

		for (int i = 0; i < n; i++)
			for (int j = 0; j < n; j++) {
				cost[i][j] = maxWeight - cost[i][j];
			}

		int[] matching = runHungarian(n, cost);
		matchedEdges = new ArrayList<Edge>();
		for (int i = 0; i < n; i++) {
			if (i >= vertices.size() || matching[i] >= vertices.size())
				continue;

			Edge edge = graph.getEdge(vertices.get(i), vertices.get(matching[i]));
			if (edge == null)
				continue;

			matchedEdges.add(edge);
		}
	}

	public void runUndirected() {
		int n = graph.getWords().size();
		double[][] cost = new double[n][n];

		//fill weights
		double maxWeight = 1;
		for (Edge e : graph.edgeSet())
			maxWeight = Math.max(graph.getEdgeWeight(e), maxWeight);

		maxWeight *= 1000;
		List<Vertex> vertices = new ArrayList<Vertex>(graph.vertexSet());
		for (int i = 0; i < n; i++)
			for (int j = 0; j < n; j++) {
				cost[i][j] = maxWeight;
				if (!graph.containsEdge(vertices.get(i), vertices.get(j)))
					continue;

				cost[i][j] -= graph.getEdgeWeight(graph.getEdge(vertices.get(i), vertices.get(j)));
			}

		int[] matching = runHungarian(n, cost);
		matchedEdges = new ArrayList<Edge>();
		for (int i = 0; i < n; i++) {
			if (matching[matching[i]] == i && i >= matching[i])
				continue;

			Edge edge = graph.getEdge(vertices.get(i), vertices.get(matching[i]));
			if (edge == null)
				continue;

			matchedEdges.add(edge);
		}

		addUnusedEdges();
	}

	int[] runHungarian(int n, double[][] cost) {
		int[] X = new int[n];
		int[] Y = new int[n];
		Arrays.fill(X, -1);
		Arrays.fill(Y, -1);

		PreprocessCost(n, cost);

		while (true) {
			Queue<Integer> q = new LinkedList<Integer>();
			int[] parent = new int[2 * n];
			Arrays.fill(parent, -1);
			boolean[] used = new boolean[2 * n];
			Arrays.fill(used, false);

			for (int i = 0; i < n; i++)
				if (X[i] == -1) {
					q.add(i);
					used[i] = true;
				}

			if (q.isEmpty())
				break;

			int now = -1;
			int next = -1;
			boolean foundAlternate = false;
			while (!q.isEmpty()) {
				now = q.poll();
				if (now < n) {
					for (int i = 0; i < n; i++)
						if (cost[now][i] == 0 && !used[i + n]) {
							parent[i + n] = now;
							if (Y[i] == -1) {
								foundAlternate = true;
								next = i + n;
								break;
							}
							used[i + n] = true;
							q.add(i + n);
						}
				} else if (!used[Y[now - n]]) {
					used[Y[now - n]] = true;
					parent[Y[now - n]] = now;
					q.add(Y[now - n]);
				}

				if (foundAlternate)
					break;

				if (q.isEmpty()) {
					double dMin = Double.MAX_VALUE;
					for (int i = 0; i < n; i++)
						if (used[i])
							for (int j = 0; j < n; j++)
								if (!used[j + n])
									if (dMin > cost[i][j])
										dMin = cost[i][j];

					for (int i = 0; i < n; i++)
						for (int j = 0; j < n; j++)
							if (used[i] != used[j + n])
								if (used[i])
									cost[i][j] -= dMin;
								else
									cost[i][j] += dMin;

					Arrays.fill(used, false);
					Arrays.fill(parent, -1);
					for (int i = 0; i < n; i++)
						if (X[i] == -1) {
							used[i] = true;
							q.add(i);
						}
				}
			}

			while (now != -1) {
				X[now] = next - n;
				Y[next - n] = now;
				next = now;
				now = parent[now];
				if (now != -1) {
					next = now;
					now = parent[now];
				}
			}
		}

		for (int i = 0; i < n; i++)
			if (X[i] == -1)
				throw new RuntimeException("not a perfect matching");

		return X;
	}

	void PreprocessCost(int n, double[][] cost) {
		double dMin;
		//rows
		for (int i = 0; i < n; i++) {
			dMin = Double.MAX_VALUE;
			for (int j = 0; j < n; j++)
				if (dMin > cost[i][j])
					dMin = cost[i][j];

			for (int j = 0; j < n; j++)
				cost[i][j] -= dMin;
		}

		//columns
		for (int j = 0; j < n; j++) {
			dMin = Double.MAX_VALUE;
			for (int i = 0; i < n; i++)
				if (dMin > cost[i][j])
					dMin = cost[i][j];

			for (int i = 0; i < n; i++)
				cost[i][j] -= dMin;
		}

	}

	private void addUnusedEdges() {
		GreedyCycleCoverExtractor extractor = new GreedyCycleCoverExtractor(graph);
		extractor.run(matchedEdges);
		matchedEdges.addAll(extractor.getMatchedEdges());
	}

}
