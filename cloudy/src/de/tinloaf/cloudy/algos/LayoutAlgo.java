package de.tinloaf.cloudy.algos;

import de.tinloaf.cloudy.text.Word;
import de.tinloaf.cloudy.utils.BoundingBoxGenerator;
import de.tinloaf.cloudy.utils.SWCRectangle;
import de.tinloaf.cloudy.utils.WordPair;

import java.util.List;
import java.util.Map;

public interface LayoutAlgo
{
    public void setConstraints(BoundingBoxGenerator bbGenerator);

    public void setData(List<Word> words, Map<WordPair, Double> similarity);

    public void run();

    public SWCRectangle getWordRectangle(Word w);
}
