package edu.cloudy.metrics;

import edu.cloudy.layout.LayoutAlgo;
import edu.cloudy.nlp.Word;
import edu.cloudy.nlp.WordPair;
import edu.cloudy.utils.SWCRectangle;

import java.util.List;
import java.util.Map;

/**
 * May 3, 2013
 * computes aspect ration
 */
public class AspectRatioMetric implements QualityMetric {

	@Override
	public double getValue(List<Word> words, Map<WordPair, Double> similarity, LayoutAlgo algo) {
		if (words.isEmpty())
			return 0;

		SWCRectangle bb = SpaceMetric.computeBoundingBox(words, algo);
		double ratio = bb.getWidth() / bb.getHeight();
		double golden = (1.0 + Math.sqrt(5)) / 2.0;

		double diff = Math.abs(ratio - golden);
		return ratio;
	}
}
