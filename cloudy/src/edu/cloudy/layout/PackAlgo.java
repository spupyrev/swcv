package edu.cloudy.layout;

import edu.cloudy.layout.packing.WordPlacer;
import edu.cloudy.nlp.Word;
import edu.cloudy.nlp.WordPair;
import edu.cloudy.utils.BoundingBoxGenerator;
import edu.cloudy.utils.SWCRectangle;

import java.util.List;
import java.util.Map;

/**
 * May 14, 2013
 */
public class PackAlgo implements LayoutAlgo
{
    private WordPlacer placer;
    
    public PackAlgo(WordPlacer placer)
    {
        this.placer = placer;
    }

    @Override
    public void setConstraints(BoundingBoxGenerator bbGenerator)
    {
    }

    @Override
    public void setData(List<Word> words, Map<WordPair, Double> similarity)
    {
    }

    @Override
    public SWCRectangle getWordRectangle(Word w)
    {
        return placer.getRectangleForWord(w);
    }

    @Override
    public void run()
    {
    }
}
