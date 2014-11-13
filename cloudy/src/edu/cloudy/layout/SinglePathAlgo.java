package edu.cloudy.layout;

import edu.cloudy.geom.SWCRectangle;
import edu.cloudy.utils.Logger;

import java.util.stream.IntStream;

/**
 * @author spupyrev
 * May 15, 2013
 */
public class SinglePathAlgo extends BaseLayoutAlgo
{
    public SinglePathAlgo()
    {
    }

    @Override
    public void run()
    {
        //remove the lightest edge
        generatePath();

        //create rectangles
        generateBoundingBoxes();

        wordPositions[0].move(0, 0);

        // -> 0
        // down 1
        // <- 2
        // up 3
        int prevDir = 3;
        for (int i = 1; i < words.length; i++)
        {
            int dir = prevDir + 1;
            while (true)
            {
                if (tryLayout((dir + 4) % 4, i))
                    break;
                dir--;

                //if smth goes wrong..
                //do an old (safe) strategy
                if (dir < 4)
                {
                    Logger.log("can't layout the path with length = " + words.length);

                    LayoutResult singleCycleLayout = new SingleCycleAlgo().layout(wordGraph);
                    IntStream.range(0, words.length).forEach(index -> wordPositions[index] = singleCycleLayout.getWordPosition(words[index]));

                    return;
                }
            }
            prevDir = dir;
        }
    }

    private boolean tryLayout(int dir, int now)
    {
        int MAX = 10;

        SWCRectangle prevRec = wordPositions[now - 1];

        if (dir == 0)
        {
            for (int i = 0; i < MAX; i++)
            {
                double x = prevRec.getMaxX();
                double y = prevRec.getMinY() + i * prevRec.getHeight() / MAX;
                if (canPlace(now, x, y))
                {
                    wordPositions[now].move(x, y);
                    return true;
                }
            }

            for (int i = MAX - 1; i >= 0; i--)
            {
                double x = prevRec.getMaxX();
                double y = prevRec.getMinY() + i * prevRec.getHeight() / MAX - wordPositions[now].getHeight();
                if (canPlace(now, x, y))
                {
                    wordPositions[now].move(x, y);
                    return true;
                }
            }
        }
        else if (dir == 1)
        {
            for (int i = 0; i < MAX; i++)
            {
                double x = prevRec.getMinX() + i * prevRec.getWidth() / MAX;
                double y = prevRec.getMaxY();
                if (canPlace(now, x, y))
                {
                    wordPositions[now].move(x, y);
                    return true;
                }
            }

            for (int i = MAX - 1; i >= 0; i--)
            {
                double x = prevRec.getMinX() + i * prevRec.getWidth() / MAX - wordPositions[now].getWidth();
                double y = prevRec.getMaxY();
                if (canPlace(now, x, y))
                {
                    wordPositions[now].move(x, y);
                    return true;
                }
            }
        }
        else if (dir == 2)
        {
            for (int i = 0; i < MAX; i++)
            {
                double x = prevRec.getMinX() - wordPositions[now].getWidth();
                double y = prevRec.getMinY() + i * prevRec.getHeight() / MAX;
                if (canPlace(now, x, y))
                {
                    wordPositions[now].move(x, y);
                    return true;
                }
            }

            for (int i = MAX - 1; i >= 0; i--)
            {
                double x = prevRec.getMinX() - wordPositions[now].getWidth();
                double y = prevRec.getMinY() + i * prevRec.getHeight() / MAX - wordPositions[now].getHeight();
                if (canPlace(now, x, y))
                {
                    wordPositions[now].move(x, y);
                    return true;
                }
            }
        }
        else if (dir == 3)
        {
            for (int i = 0; i < MAX; i++)
            {
                double x = prevRec.getMinX() + i * prevRec.getWidth() / MAX;
                double y = prevRec.getMinY() - wordPositions[now].getHeight();
                if (canPlace(now, x, y))
                {
                    wordPositions[now].move(x, y);
                    return true;
                }
            }

            for (int i = MAX - 1; i >= 0; i--)
            {
                double x = prevRec.getMinX() + i * prevRec.getWidth() / MAX - wordPositions[now].getWidth();
                double y = prevRec.getMinY() - wordPositions[now].getHeight();
                if (canPlace(now, x, y))
                {
                    wordPositions[now].move(x, y);
                    return true;
                }
            }
        }

        return false;
    }

    private boolean canPlace(int now, double x, double y)
    {
        SWCRectangle r = new SWCRectangle(x, y, wordPositions[now].getWidth(), wordPositions[now].getHeight());
        for (int i = 0; i < now; i++)
            if (wordPositions[i].intersects(r))
                return false;
        return true;
    }

    private void generatePath()
    {
        int bestIndex = -1;
        double minWeight = Double.MAX_VALUE;

        int n = words.length;
        if (n <= 2)
            return;

        for (int i = 0; i < n; i++)
        {
            double weight = similarity[i][(i + 1) % n];
            if (bestIndex == -1 || weight < minWeight)
            {
                minWeight = weight;
                bestIndex = i;
            }
        }

        assert (bestIndex != -1);

        wordGraph.reorderWords(bestIndex);
        words = wordGraph.convertWordsToArray();
        similarity = wordGraph.convertSimilarityToArray();
    }

}
