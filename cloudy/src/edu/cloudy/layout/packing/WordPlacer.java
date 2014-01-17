package edu.cloudy.layout.packing;

import edu.cloudy.nlp.Word;
import edu.cloudy.utils.SWCRectangle;

public interface WordPlacer
{
    public SWCRectangle getRectangleForWord(Word w);

    public boolean contains(Word w);
}
