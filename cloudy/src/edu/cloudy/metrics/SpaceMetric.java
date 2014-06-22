package edu.cloudy.metrics;

import edu.cloudy.layout.LayoutAlgo;
import edu.cloudy.nlp.Word;
import edu.cloudy.nlp.WordPair;
import edu.cloudy.utils.GeometryUtils;
import edu.cloudy.utils.SWCPoint;
import edu.cloudy.utils.SWCRectangle;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * May 3, 2013
 * computes the ratio used_area/bounding_box_area
 */
public class SpaceMetric implements QualityMetric {
	//true => use convex hull
	//false => use bounding box
	boolean useConvexHull;

	public SpaceMetric(boolean useConvexHull) {
		this.useConvexHull = useConvexHull;
	}

	@Override
	public double getValue(List<Word> words, Map<WordPair, Double> similarity, LayoutAlgo algo) {
		double area = computeTotalArea(words, algo);

		double usedArea = computeUsedArea(words, algo);
		//assert (usedArea <= area);

		//System.out.println("area = " + area);
		//System.out.println("usedArea = " + usedArea);

		return Math.min(1, usedArea / area);
	}

	private double computeTotalArea(List<Word> words, LayoutAlgo algo) {
		if (!useConvexHull) {
			SWCRectangle boundingBox = computeBoundingBox(words, algo);
			return boundingBox.getHeight() * boundingBox.getWidth();
		} else {
			List<SWCPoint> allPoints = extractPoints(words, algo);
			List<SWCPoint> convexHull = GeometryUtils.computeConvexHull(allPoints);
			return GeometryUtils.computeArea(convexHull);
		}
	}

	private List<SWCPoint> extractPoints(List<Word> words, LayoutAlgo algo) {
		List<SWCPoint> points = new ArrayList<SWCPoint>();
		for (Word w : words) {
			SWCRectangle rect = algo.getWordPosition(w);
			points.add(new SWCPoint(rect.getMinX(), rect.getMinY()));
			points.add(new SWCPoint(rect.getMaxX(), rect.getMinY()));
			points.add(new SWCPoint(rect.getMinX(), rect.getMaxY()));
			points.add(new SWCPoint(rect.getMaxX(), rect.getMaxY()));
		}
		return points;
	}

	public static SWCRectangle computeBoundingBox(List<Word> words, LayoutAlgo algo) {
		double minX = Double.POSITIVE_INFINITY;
		double maxX = Double.NEGATIVE_INFINITY;
		double minY = Double.POSITIVE_INFINITY;
		double maxY = Double.NEGATIVE_INFINITY;
		
		for (Word w : words) {
			SWCRectangle rect = algo.getWordPosition(w);
			if(rect != null){
				minX = Math.min(minX, rect.getMinX());
				maxX = Math.max(maxX, rect.getMaxX());
				minY = Math.min(minY, rect.getMinY());
				maxY = Math.max(maxY, rect.getMaxY());
			}
		}
		
		return new SWCRectangle(minX, minY, maxX - minX, maxY - minY);
		
		/*SWCRectangle bb = null;
		for (Word w : words) {
			SWCRectangle rect = algo.getWordRectangle(w);
			if (bb == null)
				bb = new SWCRectangle(rect.getX(), rect.getY(), rect.getWidth(), rect.getHeight());
			else
				bb.add(rect);
		}

		return bb;*/
	}

	public static double computeUsedArea(List<Word> words, LayoutAlgo algo) {
		double res = 0;
		for (Word w : words) {
			SWCRectangle rect = algo.getWordPosition(w);
			res += rect.getHeight() * rect.getWidth();
		}

		return res;
	}

}
