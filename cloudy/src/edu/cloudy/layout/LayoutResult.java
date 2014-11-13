package edu.cloudy.layout;

import edu.cloudy.geom.SWCRectangle;
import edu.cloudy.nlp.Word;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.IntStream;

/**
 * @author spupyrev
 * Oct 11, 2014
 */
public class LayoutResult
{
    private Map<Word, SWCRectangle> wordPositions;

    public LayoutResult(Word[] words, SWCRectangle[] positions)
    {
        wordPositions = new HashMap();
        IntStream.range(0, words.length).forEach(i -> wordPositions.put(words[i], positions[i]));
    }

    public SWCRectangle getWordPosition(Word w)
    {
        return wordPositions.get(w);
    }
}
