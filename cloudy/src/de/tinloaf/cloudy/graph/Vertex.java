package de.tinloaf.cloudy.graph;

import de.tinloaf.cloudy.text.Word;

public class Vertex extends Word
{
    private WordGraph g;

    public Vertex(String word, double weight, WordGraph g)
    {
        super(word, weight);
        this.g = g;
    }

    public int getDegree()
    {
        return this.g.degreeOf(this);
    }

    public String toString()
    {
        return word;
    }
}
