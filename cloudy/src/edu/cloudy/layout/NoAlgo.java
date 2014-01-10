package edu.cloudy.layout;

import edu.cloudy.nlp.Word;
import edu.cloudy.nlp.WordPair;
import edu.cloudy.utils.BoundingBoxGenerator;
import edu.cloudy.utils.Cluster;
import edu.cloudy.utils.SWCRectangle;

import java.util.List;
import java.util.Map;

public class NoAlgo implements LayoutAlgo
{

    private Map<Word, SWCRectangle> wordToRect;
    private BoundingBoxGenerator bbGenerator;
    private Map<WordPair, Double> similarity;

    public NoAlgo(Cluster cluster)
    {
        wordToRect = cluster.wordPositions;
    }

    public NoAlgo(Map<Word, SWCRectangle> wordPositions)
    {
        this.wordToRect = wordPositions;
    }

    @Override
    public void setConstraints(BoundingBoxGenerator bbGenerator)
    {
        this.bbGenerator = bbGenerator;
    }

    @Override
    public void setData(List<Word> words, Map<WordPair, Double> similarity)
    {
        this.similarity = similarity;
    }

    @Override
    public void run()
    {

    }

    @Override
    public SWCRectangle getWordRectangle(Word w)
    {
        return wordToRect.get(w);
    }

}
