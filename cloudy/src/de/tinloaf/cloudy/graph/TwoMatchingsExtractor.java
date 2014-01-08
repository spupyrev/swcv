package de.tinloaf.cloudy.graph;

import java.util.List;

import de.tinloaf.cloudy.matchings.KolmogorovMatcher;

public class TwoMatchingsExtractor {
	private WordGraph g;
	private List<Edge> matchedEdges;
	
	public TwoMatchingsExtractor(WordGraph g) {
		this.g = g;
	}
	
	public void run() {
		List<Edge> matching1;
		List<Edge> matching2;
		
		KolmogorovMatcher km = new KolmogorovMatcher(this.g);
		km.init();
		km.solve();
		matching1 = km.getMatchedEdges();
		
		WordGraph g2 = (WordGraph)this.g.clone();
		g2.removeAllEdges(matching1);
		km.dispose();
		
		km = new KolmogorovMatcher(g2);
		km.init();
		km.solve();

		matching2 = km.getMatchedEdges();
		km.dispose();
		
		this.matchedEdges = matching1;
		this.matchedEdges.addAll(matching2);
	}
	
	public List<Edge> getMatchedEdges() {
		return this.matchedEdges;
	}
}
