package edu.cloudy.colors;

import edu.cloudy.nlp.Word;

import java.awt.Color;

public class SentimentColorScheme extends ColorScheme
{
    private Color[] colorSet;

    public SentimentColorScheme(String name, Color[] colorSet)
    {
        super(name);
        this.colorSet = colorSet;
    }

    @Override
    public Color getColor(Word word)
    {
        double sentiValue = word.getSentimentValue();
        if (sentiValue < -.5)
        {
            return colorSet[4];
        }
        else if (sentiValue < 0)
        {
            return colorSet[3];
        }
        else if (sentiValue == 0)
        {
            return colorSet[2];
        }
        else if (sentiValue > 0.5)
        {
            return colorSet[0];
        }
        else
        {
            return colorSet[1];
        }
    }
}