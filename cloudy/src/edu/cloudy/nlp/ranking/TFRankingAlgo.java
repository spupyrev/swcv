package edu.cloudy.nlp.ranking;

import edu.cloudy.nlp.SWCDocument;
import edu.cloudy.nlp.Word;

import java.util.List;

/**
 * @author spupyrev
 * Aug 18, 2013
 */
public class TFRankingAlgo implements RankingAlgo
{
    @Override
    public void buildWeights(SWCDocument wordifier)
    {
        List<Word> words = wordifier.getWords();

        double maxCount = words.stream().mapToDouble(w -> w.getSentences().size()).max().orElse(1);

        for (Word w : words)
            w.weight = w.getSentences().size() / maxCount;
    }
}
