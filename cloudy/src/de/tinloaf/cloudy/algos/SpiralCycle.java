package de.tinloaf.cloudy.algos;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import de.tinloaf.cloudy.text.Word;
import de.tinloaf.cloudy.utils.BoundingBoxGenerator;
import de.tinloaf.cloudy.utils.SWCRectangle;
import de.tinloaf.cloudy.utils.WordPair;

/**
 * @author spupyrev
 * May 14, 2013
 * 
 * implemenation of Wordle algorithm
 * based on explanation in http://stackoverflow.com/questions/342687/algorithm-to-implement-a-word-cloud-like-wordle 
 */
public class SpiralCycle implements LayoutAlgo
{
    private List<Word> words;

    private BoundingBoxGenerator bbGenerator;

    private Map<Word, SWCRectangle> bb = new HashMap<Word, SWCRectangle>();

    private Map<Word, SWCRectangle> wordPositions = new HashMap<Word, SWCRectangle>();

    private static boolean ALLOW_VERTICAL_WORDS = false;

    private Random rnd = new Random(123);

    private double MAX_WIDTH;
    private double MAX_HEIGHT;

    public SpiralCycle(List<Word> wordList)
    {
        this.words = wordList;
    }

    @Override
    public void setConstraints(BoundingBoxGenerator bbGenerator)
    {
        this.bbGenerator = bbGenerator;
    }

    @Override
    public void setData(List<Word> words, Map<WordPair, Double> similarity)
    {
        this.words = words;
    }

    @Override
    public SWCRectangle getWordRectangle(Word w)
    {
        return wordPositions.get(w);
    }

    @Override
    public void run()
    {
        generateBoundingBoxes();

        //compute MAX_WIDTH and MAX_HEIGHT
        //computeCloudDimensions();

        //try to layout words
        SWCRectangle prev = null;
        for (Word w : words)
        {
            SWCRectangle rect = wordPositions.get(w);
            if (prev == null)
            {
                rect.setRect(0, 0, rect.getWidth(), rect.getHeight());
                prev = rect;
                continue;
            }
            else
                rect.setRect(0, 0, rect.getWidth(), rect.getHeight());

            int i = 0;
            List<Double> successXs = new ArrayList<Double>();
            List<Double> successYs = new ArrayList<Double>();
            while (intersects(w))
            {
                spiralOut(rect, i);
                i++;
            }

            int firstSuccess = i;
            successXs.add(rect.getCenterX());
            successYs.add(rect.getCenterY());
            while (i < firstSuccess + 2 * Math.PI)
            {
                spiralOut(rect, i);
                i++;
                if (!intersects(w))
                {
                    successXs.add(rect.getCenterX());
                    successYs.add(rect.getCenterY());
                }
            }

            double chosenX = 0.0;
            double chosenY = 0.0;
            double minDistance = Double.MAX_VALUE;
            for (int j = 0; j < successXs.size(); j++)
            {
                double currentX = successXs.get(j);
                double currentY = successYs.get(j);
                double currentDistance = euclideanDistance(currentX, currentY, prev.getCenterX(), prev.getCenterY());
                if (currentDistance < minDistance)
                {
                    minDistance = currentDistance;
                    chosenX = currentX;
                    chosenY = currentY;
                }
            }

            rect.setRect(chosenX, chosenY, rect.getWidth(), rect.getHeight());

            prev = rect;
        }

    }

    private double euclideanDistance(double currentX, double currentY, double centerX, double centerY)
    {
        return Math.sqrt(Math.abs((currentX - centerX) * (currentY - centerY)));
    }

    private void spiralOut(SWCRectangle r, int i)
    {
        Random rand = new Random();
        r.setRect(r.getX() + i * Math.cos(i + rand.nextDouble() * 0.5), r.getY() + i * Math.sin(i + rand.nextDouble() * 0.5), r.getWidth(), r.getHeight());
    }

    private void generateBoundingBoxes()
    {
        for (Word w : words)
            wordPositions.put(w, bbGenerator.getBoundingBox(w, w.weight));
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
        double angle = generateAngle();

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
    private double generateAngle()
    {
        if (ALLOW_VERTICAL_WORDS && rnd.nextDouble() < 0.5)
            return -Math.PI / 2;

        return 0;
    }

    public Map<Word, SWCRectangle> getWordPositions()
    {
        return wordPositions;
    }
}
