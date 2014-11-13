package edu.cloudy.layout;

import edu.cloudy.geom.SWCRectangle;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * May 14, 2013
 */
public class SpiralCycleAlgo extends BaseLayoutAlgo
{
    public SpiralCycleAlgo()
    {
    }

    @Override
    public void run()
    {
        generateBoundingBoxes();

        //try to layout words
        SWCRectangle prev = null;
        for (int k = 0; k < words.length; k++)
        {
            SWCRectangle rect = wordPositions[k];
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
            while (intersects(k))
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
                if (!intersects(k))
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

    private boolean intersects(int wordIndex)
    {
        SWCRectangle rect = wordPositions[wordIndex];

        for (int i = 0; i < words.length; i++)
            if (i != wordIndex)
                if (rect.intersects(wordPositions[i]))
                    return true;

        return false;
    }
}
