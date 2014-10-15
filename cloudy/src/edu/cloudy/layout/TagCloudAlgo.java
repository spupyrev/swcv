package edu.cloudy.layout;

import edu.cloudy.geom.SWCRectangle;
import edu.cloudy.nlp.Word;
import edu.cloudy.nlp.WordPair;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author spupyrev
 * Oct 4, 2014
 */
public class TagCloudAlgo extends BaseLayoutAlgo
{
    public enum TABLE_ORDER
    {
        RANK, ALPHABETICAL
    }

    private Map<Word, SWCRectangle> wordRectangles = new HashMap<Word, SWCRectangle>();

    private TABLE_ORDER order;

    private double MAX_WIDTH;
    private double MAX_HEIGHT;

    public TagCloudAlgo(List<Word> words, Map<WordPair, Double> similarity, TABLE_ORDER order)
    {
        super(words, similarity);
        this.order = order;
    }

    @Override
    protected void run()
    {
        generateBoundingBoxes();

        computeCloudDimensions();

        sortWords();

        double scale = 1.05;
        //try to layout words
        while (!doLayout())
        {
            //increase cloud dimensions
            MAX_WIDTH *= scale;
            MAX_HEIGHT *= scale;
        }
    }

    private void sortWords()
    {
        if (order == TABLE_ORDER.ALPHABETICAL)
        {
            Collections.sort(words, (w1, w2) -> w1.word.compareToIgnoreCase(w2.word));
        }
        else if (order == TABLE_ORDER.RANK)
        {
            Collections.sort(words, Comparator.reverseOrder());
        }
    }

    private void computeCloudDimensions()
    {
        double area = wordRectangles.values().stream().mapToDouble(r -> r.getArea()).sum();

        MAX_HEIGHT = Math.sqrt(1.25 * area / aspectRatio);
        MAX_WIDTH = MAX_HEIGHT * aspectRatio;
    }

    private void generateBoundingBoxes()
    {
        words.forEach(w -> wordRectangles.put(w, getBoundingBox(w)));
    }

    public boolean doLayout()
    {
        wordPositions.clear();

        List<Word> curWords = new ArrayList();
        double curX = 0, curY = 0;
        for (int i = 0; i < words.size(); i++)
        {
            Word w = words.get(i);
            SWCRectangle rect = wordRectangles.get(w);
            if (curX + rect.getWidth() > MAX_WIDTH)
            {
                if (curWords.isEmpty())
                    return false;

                //go to the next line
                curY += assignPositions(curWords, curY);
                curX = 0;
                curWords.clear();
                i--;
            }
            else
            {
                curWords.add(w);
                curX += rect.getWidth() * 1.05;
            }
        }

        //place remaining words
        curY += assignPositions(curWords, curY);

        return curY <= MAX_HEIGHT;
    }

    private double assignPositions(List<Word> curWords, double curY)
    {
        double maxH = maxHeight(words);
        double h = Math.max(maxHeight(curWords), maxH / 2.5);
        double delta = (curWords.size() > 1 ? (MAX_WIDTH - sumWidth(curWords)) / (curWords.size() - 1) : 0);
        double curX = 0;
        for (Word w : curWords)
        {
            SWCRectangle rect = wordRectangles.get(w);
            rect.moveTo(curX, curY + h - rect.getHeight());
            wordPositions.put(w, rect);
            curX += rect.getWidth() + delta;
        }
        return h + 1;
    }

    private double maxHeight(List<Word> curWords)
    {
        return curWords.stream().mapToDouble(w -> wordRectangles.get(w).getHeight()).max().orElse(0);
    }

    private double sumWidth(List<Word> curWords)
    {
        return curWords.stream().mapToDouble(w -> wordRectangles.get(w).getWidth()).sum();
    }

}
