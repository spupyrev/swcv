package edu.cloudy.layout;

import java.util.Arrays;
import java.util.Comparator;

/**
 * @author spupyrev
 * Oct 18, 2014
 */
public class TagCloudRankAlgo extends TagCloudAlgo
{
    public TagCloudRankAlgo()
    {
    }

    protected void sortWords()
    {
        Arrays.sort(words, Comparator.reverseOrder());
    }

}
