package edu.cloudy.metrics;

import edu.cloudy.layout.LayoutAlgo;
import edu.cloudy.nlp.Word;
import edu.cloudy.nlp.WordPair;
import edu.cloudy.utils.SWCRectangle;

import java.util.List;
import java.util.Map;

/**
 * @author spupyrev
 * May 3, 2013
 */
public class OverlapsMetric implements QualityMetric {
	//rectangles that are closer than EPS are considered as touching 
	private static double EPS = 0.005;

	@Override
	public double getValue(List<Word> words, Map<WordPair, Double> similarity, LayoutAlgo algo) {
		SWCRectangle bb = SpaceMetric.computeBoundingBox(words, algo);

		for (int i = 0; i < words.size(); i++)
			for (int j = i + 1; j < words.size(); j++) {
				if (overlap(algo, bb, words.get(i), words.get(j)))
					return 1;
			}

		return 0;
	}

	private boolean overlap(LayoutAlgo algo, SWCRectangle bb, Word first, Word second) {
		SWCRectangle rect1 = algo.getWordRectangle(first);
		SWCRectangle rect2 = algo.getWordRectangle(second);
		return overlap(bb, rect1, rect2);
	}

	public static boolean overlap(SWCRectangle bb, SWCRectangle rect1, SWCRectangle rect2) {
		//checking interections manually, since we want to use EPS
		if(rect1 == null || rect2 == null)
			return false;
		boolean xIntersect = intersect(bb.getWidth(), rect1.getMinX(), rect1.getMaxX(), rect2.getMinX(), rect2.getMaxX());
		boolean yIntersect = intersect(bb.getHeight(), rect1.getMinY(), rect1.getMaxY(), rect2.getMinY(), rect2.getMaxY());
		return xIntersect && yIntersect;
	}

	private static boolean intersect(double size, double m1, double M1, double m2, double M2) {
		if (M1 - size * EPS <= m2)
			return false;
		if (M2 - size * EPS <= m1)
			return false;

		return true;
	}

}
