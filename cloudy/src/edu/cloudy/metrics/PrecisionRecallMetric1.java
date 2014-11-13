package edu.cloudy.metrics;

import edu.cloudy.geom.SWCRectangle;
import edu.cloudy.layout.LayoutResult;
import edu.cloudy.layout.WordGraph;
import edu.cloudy.nlp.Word;
import edu.cloudy.nlp.WordPair;

import java.awt.geom.Ellipse2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PrecisionRecallMetric1 implements QualityMetric
{
    @Override
    public double getValue(WordGraph wordGraph, LayoutResult layout)
    {
        List<Word> words = wordGraph.getWords();
        Map<WordPair, Double> similarity = wordGraph.getSimilarity();
        
        double res = 0;
        for (Word word : words)
        {
            List<Word> closeWords = getCloseWords(word, words, layout);
            res += precisionRecall(word, layout, similarity, closeWords);
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

    private List<Word> getCloseWords(Word word, List<Word> words, LayoutResult algo)
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

    public double precisionRecall(Word w, LayoutResult algo, Map<WordPair, Double> similarity, List<Word> closeWords)
    {
        if (w == null)
            return 0;
        
        double precision = 0;
        for (Word close : closeWords)
        {
            precision += similarity.getOrDefault(new WordPair(w, close), 0.0);
        }
        return precision;
    }
}
