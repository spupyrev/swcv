package de.tinloaf.cloudy.graph;

import java.util.Iterator;

public class DegreeWeightBuilder {
	private WordGraph original;
	private WordGraph g;
	private int curDegree;

	public DegreeWeightBuilder(WordGraph g) {
		this.original = g;
		//this.g = new WordGraph(new ArrayList<Vertex>(this.original.vertexSet()), new HashMap<UnorderedPair<Vertex, Vertex>, Double>());
		this.curDegree = 0;
		throw new RuntimeException("not implemented");
	}

	public void advance() {
		Iterator<Edge> it = this.original.weightOrderedEdgeIterator(true);

		this.curDegree++;

		while (it.hasNext()) {
			Edge e = it.next();

			if (!this.g.containsEdge(e)) {
				// cannot use vertex.getDegree here, because vertex is associated with other graph!
				if ((this.g.degreeOf(g.getEdgeTarget(e)) < this.curDegree) && (this.g.degreeOf(g.getEdgeSource(e)) < this.curDegree)) {
					this.g.addEdge(g.getEdgeSource(e), g.getEdgeTarget(e), e);
					this.g.setEdgeWeight(e, this.original.getEdgeWeight(e));
				}
			}
		}
	}

	public int getDegree() {
		return this.curDegree;
	}

	public Double getWeight() {
		return this.g.totalWeight();
	}
}
