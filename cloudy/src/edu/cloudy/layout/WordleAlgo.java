package edu.cloudy.layout;

import edu.cloudy.geom.SWCRectangle;
import edu.cloudy.nlp.Word;

import java.util.ArrayList;
import java.util.Collections;
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
    private Map<Word, SWCRectangle> bb = new HashMap<Word, SWCRectangle>();

    //private static boolean ALLOW_VERTICAL_WORDS = false;

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

    private boolean doLayout()
    {
        wordPositions.clear();

        //place the word where it wants to be
        //and remove intersects with any of the previously placed words

        List<Word> sortedWords = new ArrayList<Word>(words);
        Collections.sort(sortedWords);
        Collections.reverse(sortedWords);

        for (Word w : sortedWords)
            if (!doLayout(w))
                return false;

        return true;
    }

    private void computeCloudDimensions()
    {
        double area = 0;
        for (SWCRectangle r : bb.values())
            area += r.getHeight() * r.getWidth();

        //double aspectRatio = (1.0 + Math.sqrt(5.0)) / 2;
        MAX_HEIGHT = Math.sqrt(2.5 * area / aspectRatio);
        MAX_WIDTH = MAX_HEIGHT * aspectRatio;
    }

    private void generateBoundingBoxes()
    {
        for (Word w : words)
            bb.put(w, getBoundingBox(w));
    }

    /**
     * Main entry to the layout engine.
     * User passes in a Word and Layout places it somewhere nice.
     */
    public boolean doLayout(Word word)
    {
        for (int att = 0; att < 1000; att++)
        {
            if (!makeInitialPosition(word))
                return false;

            if (!intersects(word))
                return true;

            int r = 1;
            while (r < 4)
            {
                updatePosition(word, r++);
                if (!intersects(word))
                    return true;
            }
            wordPositions.remove(word);
        }

        return false;
    }

    /**
     * Choose a random point on screen based on the Gaussian distribution.
     * Sets the Word's x and y position when a valid one is found.
     */
    private boolean makeInitialPosition(Word word)
    {
        //double angle = generateAngle();

        // get width & height of word
        SWCRectangle rect = bb.get(word);

        int attempt = 0;
        double x, y;
        do
        {
            if (attempt++ >= 100)
                return false;

            x = rnd.nextDouble() * MAX_WIDTH / 8 + MAX_WIDTH / 2;
            y = rnd.nextDouble() * MAX_HEIGHT / 8 + MAX_HEIGHT / 2;
        }
        while (x > MAX_WIDTH - rect.getWidth() || x < rect.getWidth() || y < rect.getHeight() || y > MAX_HEIGHT - rect.getHeight());

        SWCRectangle r = new SWCRectangle(x, y, rect.getWidth(), rect.getHeight());
        wordPositions.put(word, r);
        return true;
    }

    private void updatePosition(Word word, double radius)
    {
        SWCRectangle rect = wordPositions.get(word);
        radius *= rect.getWidth() / 16;

        // randomly spiral out
        double theta = rnd.nextDouble() * 2 * Math.PI;
        double x = (radius * Math.cos(theta));
        double y = (radius * Math.sin(theta));

        rect.setRect(rect.getX() + x, rect.getY() + y, rect.getWidth(), rect.getHeight());
        // TODO: check bounds
    }

    private boolean intersects(Word word)
    {
        SWCRectangle rect = wordPositions.get(word);

        for (Word w : wordPositions.keySet())
            if (!w.equals(word))
                if (rect.intersects(wordPositions.get(w)))
                    return true;

        return false;
    }

    /**
     * Generates a word angle
     */
    /*private double generateAngle()
    {
        if (ALLOW_VERTICAL_WORDS && rnd.nextDouble() < 0.5)
            return -Math.PI / 2;

        return 0;
    }*/
}
