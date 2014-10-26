package edu.cloudy.nlp;

import java.util.ArrayList;
import java.util.List;

public class Word implements Comparable<Word>, Cloneable
{
    public String word;
    public String stem;
    public double weight;
    public double sentimentValue;

    private List<Integer> sentences;

    public Word(String word, double weight)
    {
        this.word = word;
        this.stem = null;
        this.weight = weight;
        this.sentimentValue = 0;
        this.sentences = new ArrayList<Integer>();
    }

    public void addSentence(int id)
    {
        sentences.add(id);
    }

    public List<Integer> getSentences()
    {
        return sentences;
    }

    @Override
    public int hashCode()
    {
        return word.hashCode();
    }

    @Override
    public boolean equals(Object o)
    {
        if (!(o instanceof Word))
        {
            return false;
        }

        return word.equals(((Word)o).word);
    }

    @Override
    public int compareTo(Word o)
    {
        return Double.compare(weight, o.weight);
    }

    public void setSentimentValue(double sentiValue)
    {
        this.sentimentValue = sentiValue;
    }

    public double getSentimentValue()
    {
        return sentimentValue;
    }

    public String toString()
    {
        return stem;
    }

    @Override
    public Object clone()
    {
        try
        {
            return super.clone();
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }
}
