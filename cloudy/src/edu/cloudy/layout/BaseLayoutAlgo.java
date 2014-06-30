package edu.cloudy.layout;

import edu.cloudy.nlp.Word;
import edu.cloudy.nlp.WordPair;
import edu.cloudy.utils.BoundingBoxGenerator;
import edu.cloudy.utils.SWCRectangle;

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
    protected Map<Word, SWCRectangle> wordPositions;

    public BaseLayoutAlgo(List<Word> words, Map<WordPair, Double> similarity)
    {
        this.words = words;
        this.similarity = similarity;
        bbGenerator = new BoundingBoxGenerator();
        wordPositions = new HashMap<Word, SWCRectangle>();
    }

    @Override
    public void setBoundingBoxGenerator(BoundingBoxGenerator bbGenerator)
    {
        this.bbGenerator = bbGenerator;
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
