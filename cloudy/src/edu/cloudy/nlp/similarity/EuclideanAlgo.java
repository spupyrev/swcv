package edu.cloudy.nlp.similarity;

import edu.cloudy.nlp.WCVDocument;
import edu.cloudy.nlp.Word;
import edu.cloudy.nlp.WordPair;

import java.awt.Point;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class EuclideanAlgo implements SimilarityAlgo
{
    private Map<WordPair, Double> similarity;
    private WCVDocument wordifier;

    @Override
    public void initialize(WCVDocument wordifier)
    {

        this.wordifier = wordifier;
        this.similarity = null;
    }

    @Override
    public void run()
    {

        List<Word> words = wordifier.getWords();
        similarity = new HashMap<WordPair, Double>();

        for (Word x : words)
        {
            Set<Point> coorSetofX = x.getCoordinates();
            for (Word y : words)
            {
                if (x.stem.equals(y.stem))
                    continue;
                Set<Point> coorSetofY = y.getCoordinates();
                double distance = Double.MAX_VALUE;
                for (Point coory : coorSetofY)
                {
                    for (Point coorx : coorSetofX)
                    {
                        if (distance > coory.distance(coorx))
                            distance = coory.distance(coorx);
                    }
                }
                //distance = distance/(coorSetofX.size()*coorSetofY.size());
                WordPair xyPair = new WordPair(x, y);
                double relateness = Math.pow(Math.E, -Math.pow(distance, 2));
                assert (0 <= relateness && relateness <= 1.0);
                similarity.put(xyPair, relateness);
            }
        }
    }

    @Override
    public Map<WordPair, Double> getSimilarity()
    {
        return this.similarity;

    }

}
