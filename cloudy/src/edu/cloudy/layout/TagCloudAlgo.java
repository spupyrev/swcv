package edu.cloudy.layout;

import edu.cloudy.geom.SWCRectangle;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author spupyrev
 * Oct 4, 2014
 */
public abstract class TagCloudAlgo extends BaseLayoutAlgo
{
    private double MAX_WIDTH;
    private double MAX_HEIGHT;

    @Override
    protected void run()
    {
        sortWords();
        
        generateBoundingBoxes();

        computeCloudDimensions();

        double scale = 1.05;
        //try to layout words
        while (!doLayout())
        {
            //increase cloud dimensions
            MAX_WIDTH *= scale;
            MAX_HEIGHT *= scale;
        }
    }

    protected abstract void sortWords();

    private void computeCloudDimensions()
    {
        double area = Arrays.stream(wordPositions).mapToDouble(w -> w.getArea()).sum();

        MAX_HEIGHT = Math.sqrt(1.25 * area / aspectRatio);
        MAX_WIDTH = MAX_HEIGHT * aspectRatio;
    }

    public boolean doLayout()
    {
        List<Integer> curWords = new ArrayList();
        double curX = 0, curY = 0;
        for (int i = 0; i < words.length; i++)
        {
            SWCRectangle rect = wordPositions[i];
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
                curWords.add(i);
                curX += rect.getWidth() * 1.05;
            }
        }

        //place remaining words
        curY += assignPositions(curWords, curY);

        return curY <= MAX_HEIGHT;
    }

    private double assignPositions(List<Integer> curWords, double curY)
    {
        double maxH = maxHeight();
        double h = Math.max(maxHeight(curWords), maxH / 2.5);
        double delta = (curWords.size() > 1 ? (MAX_WIDTH - sumWidth(curWords)) / (curWords.size() - 1) : 0);
        double curX = 0;
        for (int i : curWords)
        {
            SWCRectangle rect = wordPositions[i];
            rect.moveTo(curX, curY + h - rect.getHeight());
            curX += rect.getWidth() + delta;
        }
        return h + 1;
    }

    private double maxHeight(List<Integer> curWords)
    {
        return curWords.stream().mapToDouble(i -> wordPositions[i].getHeight()).max().orElse(0);
    }

    private double maxHeight()
    {
        return Arrays.stream(wordPositions).mapToDouble(w -> w.getHeight()).max().orElse(0);
    }

    private double sumWidth(List<Integer> curWords)
    {
        return curWords.stream().mapToDouble(i -> wordPositions[i].getWidth()).sum();
    }

}
