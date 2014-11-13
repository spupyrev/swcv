package edu.cloudy.graph;

import java.util.List;

/**
 * @author spupyrev Apr 26, 2013
 */
public class StarForest {
	private List<Graph> stars;

	public StarForest(List<Graph> stars) {
		this.stars = stars;
	}

	public List<Graph> getStars() {
		return stars;
	}

	public void setStars(List<Graph> stars) {
		this.stars = stars;
	}

}
