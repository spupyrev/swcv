package edu.cloudy.nlp.ranking;

import edu.cloudy.nlp.WCVDocument;

/**
 * @author spupyrev
 * Aug 18, 2013
 */
public interface RankingAlgo {
	public void buildWeights(WCVDocument document);
}
