package edu.cloudy.layout;

import edu.cloudy.nlp.Word;

import java.util.List;

/**
 * @author spupyrev
 * Oct 18, 2014
 */
public class TagCloudAlphabeticalAlgo extends TagCloudAlgo
{
    protected void sortWords()
    {
        List<Word> lWords = wordGraph.getWords();
        lWords.sort((w1, w2) -> w1.word.compareToIgnoreCase(w2.word));

        words = wordGraph.convertWordsToArray();
        similarity = wordGraph.convertSimilarityToArray();
    }

}
