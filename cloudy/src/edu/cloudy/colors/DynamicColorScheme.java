package edu.cloudy.colors;

import edu.cloudy.nlp.Word;
import edu.cloudy.nlp.Word.DocIndex;

import java.awt.Color;

public class DynamicColorScheme extends ColorScheme
{
    private Color[] colorSet;

    public DynamicColorScheme(String name, Color[] colorSet)
    {
        super(name);
        this.colorSet = colorSet;
    }

    @Override
    public Color getColor(Word word)
    {
        if (word.documentIndex == DocIndex.First)
        {
            return colorSet[0];
        }
        else if (word.documentIndex == DocIndex.Second)
        {
            return colorSet[1];
        }
        else
        {
            return colorSet[2];
        }
    }
}
