package edu.cloudy.nlp.similarity;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.cloudy.nlp.WCVDocument;
import edu.cloudy.nlp.Word;
import edu.cloudy.nlp.WordPair;

public class DiceCoefficientAlgo implements SimilarityAlgo {

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

	private Map<WordPair, Double> similarity;
	private WCVDocument wordifier;

	@Override
	public void initialize(WCVDocument wordifier) {
		this.wordifier = wordifier;
		this.similarity = null;
	}

	@Override
	public void run() {
		List<Word> words = wordifier.getWords();

		similarity = new HashMap<WordPair, Double>();

		for (Word x : words) {
			for (Word y : words) {
				if (x.stem.equals(y.stem))
					continue;

				Set<Integer> sharedSentences = new HashSet<Integer>(x.getSentences());
				sharedSentences.retainAll(y.getSentences());
				
				double totalSentences = x.getSentences().size()+y.getSentences().size(); 
				

				// just count how many times those two occur in the same sentence
				WordPair xyPair = new WordPair(x, y);
				double xySimilarity = ((double)2* sharedSentences.size()) / totalSentences;
				assert (0 <= xySimilarity && xySimilarity <= 1.0);
				similarity.put(xyPair, xySimilarity);
			}
		}

	}

	@Override
	public Map<WordPair, Double> getSimilarity() {
		return this.similarity;
	}

}
