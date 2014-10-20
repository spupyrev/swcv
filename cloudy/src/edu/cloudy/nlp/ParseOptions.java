package edu.cloudy.nlp;

/**
 * @author spupyrev
 * Oct 18, 2014
 * 
 * options used to parse a given text
 */
public class ParseOptions
{
    private boolean removeStopwords = true;
    private boolean stemWords = true;
    private boolean removeNumbers = true;
    private int minWordLength = 3;

    public int getMinWordLength()
    {
        return minWordLength;
    }

    public void setMinWordLength(int minWordLength)
    {
        this.minWordLength = minWordLength;
    }

    public boolean isRemoveStopwords()
    {
        return removeStopwords;
    }

    public void setRemoveStopwords(boolean removeStopwords)
    {
        this.removeStopwords = removeStopwords;
    }

    public boolean isStemWords()
    {
        return stemWords;
    }

    public void setStemWords(boolean stemWords)
    {
        this.stemWords = stemWords;
    }

    public boolean isRemoveNumbers()
    {
        return removeNumbers;
    }

    public void setRemoveNumbers(boolean removeNumbers)
    {
        this.removeNumbers = removeNumbers;
    }

}
