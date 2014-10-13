package edu.cloudy.colors;

import edu.cloudy.nlp.Word;
import edu.cloudy.nlp.WordPair;

import java.awt.Color;
import java.util.List;
import java.util.Map;

/**
 * @author spupyrev
 * Oct 11, 2014
 */
public abstract class ColorScheme
{
    private String name;

    public ColorScheme(String name)
    {
        this.name = name;
    }

    public String getName()
    {
        return name;
    }

    public abstract Color getColor(Word word);
    
    public void initialize(List<Word> words, Map<WordPair, Double> similarity)
    {
    }
    
}
