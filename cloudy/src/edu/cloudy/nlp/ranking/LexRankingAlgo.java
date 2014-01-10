package edu.cloudy.nlp.ranking;

import edu.cloudy.nlp.WCVDocument;
import edu.cloudy.nlp.Word;
import edu.cloudy.nlp.WordPair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author spupyrev
 * Aug 18, 2013
 */
public class LexRankingAlgo implements RankingAlgo {
	private static final double LEXRANK_THRESHOLD = 0.1;

	@Override
	public void buildWeights(WCVDocument wordifier) {
		Map<WordPair, Double> coocNumber = initCoocNumber(wordifier);

		List<Word> words = wordifier.getWords();
		Map<SimilarWord, Word> wordMap = new HashMap<SimilarWord, Word>();

		List<SimilarWord> similarWords = new ArrayList<SimilarWord>(words.size());
		for (Word w : wordifier.getWords()) {
			SimilarWord similarWord = new SimilarWord(w, coocNumber);
			similarWords.add(similarWord);
			wordMap.put(similarWord, w);
		}

		LexRankResults<SimilarWord> results = LexRanker.rank(similarWords, LEXRANK_THRESHOLD, true);

		double max = results.scores.get(results.rankedResults.get(0));

		if (Double.isInfinite(max)) {
			throw new IllegalArgumentException("Argument not suited for lexrank");
		}

		for (SimilarWord w : results.rankedResults) {
			Word word = wordMap.get(w);
			word.weight = results.scores.get(w) / max;
		}
	}

	private Map<WordPair, Double> initCoocNumber(WCVDocument wordifier) {
		int n = wordifier.getWords().size();
		double[][] sim = new double[n][n];

		Map<WordPair, Double> coocNumber = new HashMap<WordPair, Double>();
		for (int i = 0; i < n; i++)
			for (int j = i; j < n; j++) {
				Word w1 = wordifier.getWords().get(i);
				Word w2 = wordifier.getWords().get(j);

				Set<Integer> intersection = new HashSet<Integer>(w1.getSentences());
				intersection.retainAll(w2.getSentences());
				//Set<Integer> union = new HashSet<Integer>(w1.getSentences());
				//union.addAll(w2.getSentences());

				double res1 = intersection.size();
				double res2 = 1;//union.size();
				double res = res1 / res2;
				coocNumber.put(new WordPair(w1, w2), res);

				sim[i][j] = sim[j][i] = res;
			}

		/*for (int i = 0; i < n; i++) {
			for (int j = 0; j < n; j++) {
				System.out.printf(" %.2f,", sim[i][j]);
			}

			System.out.println("");
		}*/

		return coocNumber;
	}

}
