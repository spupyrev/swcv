package edu.cloudy.metrics;

import edu.cloudy.layout.LayoutResult;
import edu.cloudy.nlp.Word;
import edu.cloudy.nlp.WordPair;

import java.util.List;

/**
 * @author mgaut72
 * May 3, 2013
 */
public interface AdjacentMetric
{
    List<WordPair> getCloseWords(List<Word> words, LayoutResult layout);
}
