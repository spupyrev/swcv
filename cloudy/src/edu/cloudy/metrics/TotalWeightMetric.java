package edu.cloudy.metrics;

import edu.cloudy.graph.WordGraph;
import edu.cloudy.layout.LayoutResult;
import edu.cloudy.nlp.Word;
import edu.cloudy.nlp.WordPair;

import java.util.List;
import java.util.Map;

/**
 * @author spupyrev
 * May 3, 2013
 */
public class TotalWeightMetric implements QualityMetric
{

    @Override
    public double getValue(List<Word> words, Map<WordPair, Double> similarity, LayoutResult algo)
    {
        WordGraph wg = new WordGraph(words, similarity);
        return wg.totalWeight();
    }

}
