package edu.cloudy.metrics;

import edu.cloudy.layout.LayoutAlgo;
import edu.cloudy.nlp.Word;
import edu.cloudy.nlp.WordPair;

import java.util.List;

/**
 * @author mgaut72
 * May 3, 2013
 */
public interface AdjacentMetric
{
    List<WordPair> getCloseWords(List<Word> words, LayoutAlgo algo);
}
