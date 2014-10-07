package edu.cloudy.metrics;

import edu.cloudy.geom.SWCRectangle;
import edu.cloudy.layout.LayoutAlgo;
import edu.cloudy.nlp.Word;
import edu.cloudy.nlp.WordPair;

import java.awt.geom.Ellipse2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PrecisionRecallMetric1 implements QualityMetric
{
    @Override
    public double getValue(List<Word> words, Map<WordPair, Double> similarity, LayoutAlgo algo)
    {
        double res = 0;
        for (Word word : words)
        {
            res += precisionRecall(word, algo, similarity, getCloseWords(word, words, algo));
        }
        return res / maximalPrecisionRecall(similarity);
    }

    private double maximalPrecisionRecall(Map<WordPair, Double> similarity)
    {
        double res = 0;
        for (WordPair wp : similarity.keySet())
        {
            if (wp.getFirst().equals(wp.getSecond()))
                continue;
            res += similarity.get(wp);
        }
        return res;
    }

    private List<Word> getCloseWords(Word word, List<Word> words, LayoutAlgo algo)
    {
        SWCRectangle rect = algo.getWordPosition(word);
        List<Word> closeWords = new ArrayList<Word>();
        Ellipse2D elip = new Ellipse2D.Double(rect.getX() - Math.abs((rect.getX() - rect.getCenterX())), rect.getY()
                + Math.abs(rect.getY() - rect.getCenterY()), rect.getWidth() * 2, rect.getHeight() * 2);
        
        for (Word w : words)
        {
            if (w.equals(word))
                continue;
            
            SWCRectangle rect2 = algo.getWordPosition(w);
            if (close(elip, rect2))
                closeWords.add(w);
        }
        return closeWords;
    }

    public boolean close(Ellipse2D elip, SWCRectangle rect2)
    {
        return elip.intersects(rect2.getX(), rect2.getY(), rect2.getWidth(), rect2.getHeight());
    }

    public double precisionRecall(Word w, LayoutAlgo algo, Map<WordPair, Double> similarity, List<Word> closeWords)
    {
        if (w == null)
            return 0;
        
        double precision = 0;
        for (Word close : closeWords)
        {
            precision += similarity.get(new WordPair(w, close));
        }
        return precision;
    }
}
