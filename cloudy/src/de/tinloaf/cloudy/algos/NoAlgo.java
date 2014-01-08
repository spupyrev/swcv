package de.tinloaf.cloudy.algos;

import de.tinloaf.cloudy.text.Word;
import de.tinloaf.cloudy.utils.BoundingBoxGenerator;
import de.tinloaf.cloudy.utils.Cluster;
import de.tinloaf.cloudy.utils.SWCRectangle;
import de.tinloaf.cloudy.utils.WordPair;

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
