package edu.cloudy.nlp.similarity;

import edu.cloudy.nlp.WCVDocument;
import edu.cloudy.nlp.Word;
import edu.cloudy.nlp.WordPair;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class RandomSimilarityAlgo implements SimilarityAlgo {
	private Map<WordPair, Double> similarity;
	private WCVDocument document;
	private static Random rnd = new Random(123);

	@Override
	public void initialize(WCVDocument wordifier) {
		this.document = wordifier;
		this.similarity = null;
	}

	@Override
	public void run() {
		List<Word> words = document.getWords();

		similarity = new HashMap<WordPair, Double>();
		// compute the similarity matrix

		for (int x = 0; x < words.size(); x++)
			for (int y = (x + 1); y < words.size(); y++) {
				WordPair xyPair = new WordPair(words.get(x), words.get(y));
				double weight = rnd.nextDouble();// / 10.0;
				similarity.put(xyPair, weight);
			}

		for (int x = 0; x < words.size(); x++) {
			WordPair pair = new WordPair(words.get(x), words.get(x));
			similarity.put(pair, 1.0);
		}
	}

	@Override
	public Map<WordPair, Double> getSimilarity() {
		return similarity;
	}

}
