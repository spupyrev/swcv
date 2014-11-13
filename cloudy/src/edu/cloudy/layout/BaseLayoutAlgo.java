package edu.cloudy.layout;

import edu.cloudy.geom.BoundingBoxGenerator;
import edu.cloudy.geom.SWCRectangle;
import edu.cloudy.nlp.Word;

import java.util.stream.IntStream;

/**
 * @author spupyrev
 * Jun 21, 2014
 */
public abstract class BaseLayoutAlgo implements LayoutAlgo
{
    protected WordGraph wordGraph;
    protected Word[] words;
    protected double[][] similarity;
    protected SWCRectangle[] wordPositions;

    protected BoundingBoxGenerator bbGenerator;
    protected double aspectRatio;

    public BaseLayoutAlgo()
    {
        bbGenerator = new BoundingBoxGenerator();
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

    @Override
    public final LayoutResult layout(WordGraph wordGraph)
    {
        this.wordGraph = wordGraph;
        this.words = wordGraph.convertWordsToArray();
        this.similarity = wordGraph.convertSimilarityToArray();
        this.wordPositions = new SWCRectangle[words.length];

        run();

        return createResult();
    }

    protected abstract void run();

    protected LayoutResult createResult()
    {
        return new LayoutResult(words, wordPositions);
    }

    protected void generateBoundingBoxes()
    {
        IntStream.range(0, words.length).forEach(i -> wordPositions[i] = getBoundingBox(words[i]));
    }
    
    private SWCRectangle getBoundingBox(Word word)
    {
        return bbGenerator.getBoundingBox(word);
    }

}
