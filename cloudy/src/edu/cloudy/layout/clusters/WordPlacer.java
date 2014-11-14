package edu.cloudy.layout.clusters;

import edu.cloudy.geom.SWCRectangle;
import edu.cloudy.nlp.Word;

public interface WordPlacer
{
    public SWCRectangle getRectangleForWord(Word w);

    public boolean contains(Word w);
}
