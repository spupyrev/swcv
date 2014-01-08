package de.tinloaf.cloudy.similarity;

import de.tinloaf.cloudy.text.WCVDocument;

/**
 * @author spupyrev
 * Aug 18, 2013
 */
public interface RankingAlgo {
	public void buildWeights(WCVDocument document);
}
