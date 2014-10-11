package edu.cloudy.layout;

import edu.cloudy.geom.SWCRectangle;
import edu.cloudy.nlp.Word;

import java.util.Map;

/**
 * @author spupyrev
 * Oct 11, 2014
 */
public class LayoutResult
{
    private Map<Word, SWCRectangle> wordPositions;

    public LayoutResult(Map<Word, SWCRectangle> wordPositions)
    {
        this.wordPositions = wordPositions;
    }

    public SWCRectangle getWordPosition(Word w)
    {
        return wordPositions.get(w);
    }
}
