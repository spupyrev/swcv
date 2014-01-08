package de.tinloaf.cloudy.clustering;

import de.tinloaf.cloudy.text.Word;
import de.tinloaf.cloudy.utils.WordPair;

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
