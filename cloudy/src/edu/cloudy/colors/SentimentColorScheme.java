package edu.cloudy.colors;

import edu.cloudy.nlp.Word;

import java.awt.Color;

public class SentimentColorScheme implements IColorScheme
{
    private Color[] colorSet;

    public SentimentColorScheme(String colorSchemeName)
    {
        if (colorSchemeName.equals("SENTIMENT2"))
        {
            colorSet = sentiment2;
        }
        else
        {
            colorSet = sentiment;
        }
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