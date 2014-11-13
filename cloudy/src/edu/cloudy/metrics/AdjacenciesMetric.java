package edu.cloudy.metrics;

import edu.cloudy.geom.SWCRectangle;
import edu.cloudy.layout.LayoutResult;
import edu.cloudy.layout.WordGraph;
import edu.cloudy.nlp.Word;
import edu.cloudy.nlp.WordPair;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author spupyrev
 * May 3, 2013
 */
public class AdjacenciesMetric implements QualityMetric, AdjacentMetric
{
	//rectangles that are closer than EPS are considered as touching 
	private static double EPS = 0.01;

    @Override
    public double getValue(WordGraph wordGraph, LayoutResult layout)
    {
        Map<WordPair, Double> similarity = wordGraph.getSimilarity();

		double res = 0;
		for (WordPair wp : similarity.keySet())
		{
			if (wp.getFirst().equals(wp.getSecond()))
				continue;

			if (close(layout, wp.getFirst(), wp.getSecond()))
			{
				res += similarity.get(wp);
			}
		}

		return res;
	}

	private boolean close(LayoutResult algo, Word first, Word second)
	{
		SWCRectangle rect1 = algo.getWordPosition(first);
		SWCRectangle rect2 = algo.getWordPosition(second);

		return close(rect1, rect2);
	}

	public static boolean close(SWCRectangle rect1, SWCRectangle rect2)
	{
		//checking interections manually, since we want to use EPS
		if (rect1 == null || rect2 == null)
			return false;
		boolean xIntersect = intersect(Math.min(rect1.getWidth(), rect2.getWidth()), rect1.getMinX(), rect1.getMaxX(), rect2.getMinX(), rect2.getMaxX());
		boolean yIntersect = intersect(Math.min(rect1.getHeight(), rect2.getHeight()), rect1.getMinY(), rect1.getMaxY(), rect2.getMinY(), rect2.getMaxY());
		return xIntersect && yIntersect;
	}

	private static boolean intersect(double size, double m1, double M1, double m2, double M2)
	{
		if (M1 + size * EPS <= m2)
			return false;
		if (M2 + size * EPS <= m1)
			return false;

		return true;
	}

	public List<WordPair> getCloseWords(List<Word> words, LayoutResult algo)
	{
		List<WordPair> res = new ArrayList<WordPair>();
		
		for (int i = 0; i < words.size(); i++)
			for (int j = i + 1; j < words.size(); j++)
			{
				WordPair wp = new WordPair(words.get(i), words.get(j));

				if (close(algo, wp.getFirst(), wp.getSecond()))
					res.add(wp);
			}

		return res;
	}

}
