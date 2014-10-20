package edu.cloudy.nlp.ranking;

import edu.cloudy.nlp.SWCDocument;

/**
 * @author spupyrev
 * Aug 18, 2013
 */
public interface RankingAlgo
{
    public void buildWeights(SWCDocument document);
}
