package edu.cloudy.layout;

import edu.cloudy.nlp.Word;
import edu.cloudy.nlp.WordPair;
import edu.cloudy.utils.BoundingBoxGenerator;
import edu.cloudy.utils.SWCRectangle;

import java.util.List;
import java.util.Map;

public interface LayoutAlgo
{
    public void setConstraints(BoundingBoxGenerator bbGenerator);

    public void setData(List<Word> words, Map<WordPair, Double> similarity);

    public void run();

    public SWCRectangle getWordRectangle(Word w);
}
