package edu.cloudy.clustering;

import edu.cloudy.nlp.Word;
import edu.cloudy.nlp.WordPair;

import java.util.List;
import java.util.Map;

/**
 * @author spupyrev
 * Nov 28, 2013
 */
public interface IClusterAlgo {
	void run(List<Word> words, Map<WordPair, Double> similarity);
	int getCluster(Word word);
	int getClusterNumber();
}
