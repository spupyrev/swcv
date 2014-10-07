package edu.cloudy.layout;

import edu.cloudy.geom.SWCRectangle;
import edu.cloudy.nlp.Word;
import edu.cloudy.nlp.WordPair;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * May 14, 2013
 */
public class SpiralCycleAlgo extends BaseLayoutAlgo
{

    public SpiralCycleAlgo(List<Word> words, Map<WordPair, Double> similarity)
    {
        super(words, similarity);
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
            wordPositions.put(w, getBoundingBox(w));
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
}
