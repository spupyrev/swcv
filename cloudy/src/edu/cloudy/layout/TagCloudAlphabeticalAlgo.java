package edu.cloudy.layout;

import java.util.Collections;

/**
 * @author spupyrev
 * Oct 18, 2014
 */
public class TagCloudAlphabeticalAlgo extends TagCloudAlgo
{
    protected void sortWords()
    {
        Collections.sort(words, (w1, w2) -> w1.word.compareToIgnoreCase(w2.word));
    }

}
