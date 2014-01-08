package de.tinloaf.cloudy.graph;

import java.util.List;

/**
 * @author spupyrev Apr 26, 2013
 */
public class StarForest {
	private List<WordGraph> stars;

	public StarForest(List<WordGraph> stars) {
		this.stars = stars;
	}

	public List<WordGraph> getStars() {
		return stars;
	}

	public void setStars(List<WordGraph> stars) {
		this.stars = stars;
	}

}
