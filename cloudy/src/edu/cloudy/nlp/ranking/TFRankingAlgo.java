package edu.cloudy.nlp.ranking;

import java.util.List;

import edu.cloudy.nlp.WCVDocument;
import edu.cloudy.nlp.Word;

/**
 * @author spupyrev
 * Aug 18, 2013
 */
public class TFRankingAlgo implements RankingAlgo {

	@Override
	public void buildWeights(WCVDocument wordifier) {
		List<Word> words = wordifier.getWords();
		
		int maxCount = -1;
		for (Word w : words)
			maxCount = Math.max(maxCount, w.getSentences().size());

		for (Word w : words) {
			w.weight = (double) w.getSentences().size() / (double) maxCount;
		}
	}
}
