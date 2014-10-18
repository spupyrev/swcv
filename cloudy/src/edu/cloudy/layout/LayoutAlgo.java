package edu.cloudy.layout;

import java.util.List;
import java.util.Map;

import edu.cloudy.geom.BoundingBoxGenerator;
import edu.cloudy.nlp.Word;
import edu.cloudy.nlp.WordPair;

/**
 * Algorithm for embedding words (rectangles) in the plane
 */
public interface LayoutAlgo
{
    public void setBoundingBoxGenerator(BoundingBoxGenerator bbGenerator);

    public void setAspectRatio(double aspectRatio);

    public LayoutResult layout(List<Word> words, Map<WordPair, Double> similarity);
}
