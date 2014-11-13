package edu.cloudy.layout;

import edu.cloudy.geom.SWCRectangle;
import edu.cloudy.nlp.Word;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * @author spupyrev
 * May 14, 2013
 * 
 * implemenation of Wordle algorithm
 * based on explanation in http://stackoverflow.com/questions/342687/algorithm-to-implement-a-word-cloud-like-wordle 
 */
public class WordleAlgo extends BaseLayoutAlgo
{
    private Random rnd = new Random(123);

    private double MAX_WIDTH;
    private double MAX_HEIGHT;

    public WordleAlgo()
    {
        super();
    }

    @Override
    public void run()
    {
        generateBoundingBoxes();

        //compute MAX_WIDTH and MAX_HEIGHT
        computeCloudDimensions();

        double scale = 1.1;
        //try to layout words
        while (!doLayout())
        {
            //increase cloud dimensions
            MAX_WIDTH *= scale;
            MAX_HEIGHT *= scale;
        }
    }

    private void computeCloudDimensions()
    {
        double area = Arrays.stream(wordPositions).mapToDouble(w -> w.getArea()).sum();

        MAX_HEIGHT = Math.sqrt(2.25 * area / aspectRatio);
        MAX_WIDTH = MAX_HEIGHT * aspectRatio;
    }

    private boolean doLayout()
    {
        Map<Word, SWCRectangle> placedWords = new HashMap();
        Map<Word, SWCRectangle> bb = new HashMap();
        for (int i = 0; i < words.length; i++)
            bb.put(words[i], wordPositions[i]);

        //place the word where it wants to be
        //and remove intersections with any of the previously placed words

        List<Word> sortedWords = Arrays.asList(words);
        Collections.sort(sortedWords, Comparator.reverseOrder());

        for (Word w : sortedWords)
            if (!doLayout(w, bb.get(w), placedWords))
                return false;

        for (int i = 0; i < words.length; i++)
            wordPositions[i] = placedWords.get(words[i]);

        return true;
    }

    /**
     * Main entry to the layout engine.
     * User passes in a Word and Layout places it somewhere nice
     */
    public boolean doLayout(Word word, SWCRectangle bb, Map<Word, SWCRectangle> placedWords)
    {
        for (int att = 0; att < 1000; att++)
        {
            SWCRectangle rect = makeInitialPosition(bb);
            if (rect == null)
                return false;

            if (!intersects(rect, placedWords))
            {
                placedWords.put(word, rect);
                return true;
            }

            int r = 1;
            while (r < 4)
            {
                updatePosition(rect, r++);
                if (!intersects(rect, placedWords))
                {
                    placedWords.put(word, rect);
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Choose a random point on screen based on the Gaussian distribution.
     * Sets the Word's x and y position when a valid one is found.
     */
    private SWCRectangle makeInitialPosition(SWCRectangle rect)
    {
        int attempt = 0;
        double x, y;
        do
        {
            if (attempt++ >= 100)
                return null;

            x = rnd.nextDouble() * MAX_WIDTH / 8 + MAX_WIDTH / 2;
            y = rnd.nextDouble() * MAX_HEIGHT / 8 + MAX_HEIGHT / 2;
        }
        while (x > MAX_WIDTH - rect.getWidth() || x < rect.getWidth() || y < rect.getHeight() || y > MAX_HEIGHT - rect.getHeight());

        return new SWCRectangle(x, y, rect.getWidth(), rect.getHeight());
    }

    private void updatePosition(SWCRectangle rect, double radius)
    {
        radius *= rect.getWidth() / 16;

        // randomly spiral out
        double theta = rnd.nextDouble() * 2 * Math.PI;
        double x = (radius * Math.cos(theta));
        double y = (radius * Math.sin(theta));

        rect.setRect(rect.getX() + x, rect.getY() + y, rect.getWidth(), rect.getHeight());
    }

    private boolean intersects(SWCRectangle rect, Map<Word, SWCRectangle> placedWords)
    {
        for (Word w : placedWords.keySet())
            if (rect.intersects(placedWords.get(w)))
                return true;

        return false;
    }
}
