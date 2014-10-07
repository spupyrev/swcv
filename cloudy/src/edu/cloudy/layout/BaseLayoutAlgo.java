package edu.cloudy.layout;

import edu.cloudy.geom.BoundingBoxGenerator;
import edu.cloudy.geom.SWCRectangle;
import edu.cloudy.nlp.Word;
import edu.cloudy.nlp.WordPair;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author spupyrev
 * Jun 21, 2014
 */
public abstract class BaseLayoutAlgo implements LayoutAlgo
{
    protected List<Word> words;
    protected Map<WordPair, Double> similarity;
    protected BoundingBoxGenerator bbGenerator;
    protected double aspectRatio;
    protected Map<Word, SWCRectangle> wordPositions;

    public BaseLayoutAlgo(List<Word> words, Map<WordPair, Double> similarity)
    {
        this.words = words;
        this.similarity = similarity;
        bbGenerator = new BoundingBoxGenerator();
        wordPositions = new HashMap<Word, SWCRectangle>();
        aspectRatio = 16.0 / 9.0;
    }

    @Override
    public void setBoundingBoxGenerator(BoundingBoxGenerator bbGenerator)
    {
        this.bbGenerator = bbGenerator;
    }

    @Override
    public void setAspectRatio(double aspectRatio)
    {
        this.aspectRatio = aspectRatio;
    }

    public SWCRectangle getBoundingBox(Word word)
    {
        return bbGenerator.getBoundingBox(word);
    }

    public SWCRectangle getWordPosition(Word w)
    {
        return wordPositions.get(w);
    }
}
