package de.tinloaf.cloudy.similarity;

import java.util.List;

import de.tinloaf.cloudy.text.Word;
import de.tinloaf.cloudy.text.WCVDocument;

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
