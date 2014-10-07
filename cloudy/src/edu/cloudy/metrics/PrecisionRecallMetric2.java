package edu.cloudy.metrics;

import edu.cloudy.geom.SWCRectangle;
import edu.cloudy.layout.LayoutAlgo;
import edu.cloudy.nlp.Word;
import edu.cloudy.nlp.WordPair;

import java.awt.geom.Ellipse2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class PrecisionRecallMetric2 implements QualityMetric
{
    private double threshold;

    @Override
    public double getValue(List<Word> words, Map<WordPair, Double> similarity, LayoutAlgo algo)
    {
        threshold = calculateThreshold(similarity);
        double res = 0;
        for (Word word : words)
        {
            res += precisionRecall(word, algo, similarity, getCloseWords(word, words, algo));
        }
        return res / maximalPrecisionRecall(similarity);
    }

    private double calculateThreshold(Map<WordPair, Double> similarity)
    {
        List<Double> vals = new ArrayList<Double>(similarity.values());
        Collections.sort(vals);
        int i = vals.size() - vals.size() / 10;

        if (i >= vals.size())
            return 0;
        return vals.get(i);
    }

    private double maximalPrecisionRecall(Map<WordPair, Double> similarity)
    {
        double res = 0;
        for (WordPair wp : similarity.keySet())
        {
            if (wp.getFirst().equals(wp.getSecond()))
                continue;
            if (shouldBeClose(wp, similarity))
                res++;
        }
        return res;
    }

    private List<Word> getCloseWords(Word word, List<Word> words, LayoutAlgo algo)
    {
        SWCRectangle rect = algo.getWordPosition(word);
        LinkedList<Word> closeWords = new LinkedList<Word>();
        Ellipse2D elip = new Ellipse2D.Double(rect.getX() - Math.abs((rect.getX() - rect.getCenterX())), rect.getY()
                + Math.abs(rect.getY() - rect.getCenterY()), rect.getWidth() * 2, rect.getHeight() * 2);

        for (Word temp : words)
        {
            if (temp.equals(word))
                continue;

            SWCRectangle rect2 = algo.getWordPosition(temp);
            if (close(elip, rect2))
                closeWords.add(temp);
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
        double recall = 1;
        for (Word close : closeWords)
        {
            if (shouldBeClose(w, close, similarity))
                precision++;
            else
                recall++;
        }
        return precision / (precision + recall);
    }

    private boolean shouldBeClose(Word one, Word two, Map<WordPair, Double> similarity)
    {
        WordPair temp = new WordPair(one, two);
        if (similarity.get(temp) > threshold)
            return true;
        return false;
    }

    private boolean shouldBeClose(WordPair wp, Map<WordPair, Double> similarity)
    {
        if (similarity.get(wp) > threshold)
            return true;
        return false;
    }

}
