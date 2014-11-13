package edu.cloudy.colors;

import edu.cloudy.layout.WordGraph;
import edu.cloudy.nlp.Word;

import java.awt.Color;

/**
 * @author spupyrev
 * Oct 11, 2014
 */
public abstract class ColorScheme
{
    private String name;
    private String cmdIndex;

    public ColorScheme(String name, String cmdIndex)
    {
        this.name = name;
        this.cmdIndex = cmdIndex;
    }

    public String getName()
    {
        return name;
    }

    public String getCmdIndex()
    {
        return cmdIndex;
    }

    public abstract Color getColor(Word word);

    public void initialize(WordGraph wordGraph)
    {
    }
}
