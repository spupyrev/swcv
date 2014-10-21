package edu.cloudy.colors;

import edu.cloudy.nlp.Word;

import java.awt.Color;

/**
 * @author spupyrev
 * Oct 11, 2014
 */
public class MonoColorScheme extends ColorScheme
{
    private Color color;

    public MonoColorScheme(String name, String cmdIndex, Color color)
    {
        super(name, cmdIndex);
        this.color = color;
    }

    @Override
    public Color getColor(Word word)
    {
        return color;
    }

    public int guessNumberOfClusters(int n)
    {
        return 1;
    }
}
