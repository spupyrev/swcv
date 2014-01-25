package edu.cloudy.layout;

import edu.cloudy.nlp.Word;
import edu.cloudy.nlp.WordPair;
import edu.cloudy.utils.BoundingBoxGenerator;
import edu.cloudy.utils.SWCRectangle;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author spupyrev
 * May 15, 2013
 */
public class SingleCycleAlgo implements LayoutAlgo
{
    private static boolean LAYOUT_VERTICAL = false;

    private List<Word> cycle;
    private Map<Word, SWCRectangle> wordPositions;

    private BoundingBoxGenerator bbGenerator;

    public SingleCycleAlgo(List<Word> cycle)
    {
        this.cycle = cycle;
    }

    @Override
    public void setConstraints(BoundingBoxGenerator bbGenerator)
    {
        this.bbGenerator = bbGenerator;
    }

    @Override
    public void setData(List<Word> words, Map<WordPair, Double> similarity)
    {
    }

    @Override
    public void run()
    {
        generateBoundingBoxes();
        if (cycle.size() == 1)
            return;

        if (LAYOUT_VERTICAL && cycle.size() <= 30)
            verticalLayout();
        else
            horizontalLayout();
    }

    private void horizontalLayout()
    {
        int lowerIndex = 0;
        SWCRectangle lowerRect = wordPositions.get(cycle.get(lowerIndex));
        //keep it unchanged
        double currentX = lowerRect.getMaxX();

        int upperIndex = cycle.size() - 1;
        SWCRectangle upperRect = wordPositions.get(cycle.get(upperIndex));
        //place it above
        upperRect.setRect(currentX, lowerRect.getY(), upperRect.getWidth(), upperRect.getHeight());

        while (lowerIndex + 1 != upperIndex)
        {
            //handle equality ()
            if (Math.abs(lowerRect.getMaxY() - upperRect.getMaxY()) < 1e-3)
                currentX += 1;

            if (lowerRect.getMaxY() > upperRect.getMaxY())
            {
                //placing a new rectangle on top
                SWCRectangle ur = wordPositions.get(cycle.get(upperIndex - 1));
                ur.setRect(currentX, upperRect.getMaxY(), ur.getWidth(), ur.getHeight());

                upperRect = ur;
                upperIndex--;
            }
            else
            {
                //placing a new rectangle on bottom
                SWCRectangle lr = wordPositions.get(cycle.get(lowerIndex + 1));
                lr.setRect(currentX - lr.getWidth(), lowerRect.getMaxY(), lr.getWidth(), lr.getHeight());

                lowerRect = lr;
                lowerIndex++;
            }
        }
    }

    private void verticalLayout()
    {
        int lowerIndex = 0;
        SWCRectangle lowerRect = wordPositions.get(cycle.get(lowerIndex));
        //keep it unchanged
        double currentY = lowerRect.getMaxY();

        int upperIndex = cycle.size() - 1;
        SWCRectangle upperRect = wordPositions.get(cycle.get(upperIndex));
        //place it above
        upperRect.setRect(lowerRect.getX(), currentY, upperRect.getWidth(), upperRect.getHeight());

        while (lowerIndex + 1 != upperIndex)
        {
            //handle equality ()
            if (Math.abs(lowerRect.getMaxX() - upperRect.getMaxX()) < 1e-3)
                currentY += 1;

            if (lowerRect.getMaxX() > upperRect.getMaxX())
            {
                //placing a new rectangle on top
                SWCRectangle ur = wordPositions.get(cycle.get(upperIndex - 1));
                ur.setRect(upperRect.getMaxX(), currentY, ur.getWidth(), ur.getHeight());

                upperRect = ur;
                upperIndex--;
            }
            else
            {
                //placing a new rectangle on bottom
                SWCRectangle lr = wordPositions.get(cycle.get(lowerIndex + 1));
                lr.setRect(lowerRect.getMaxX(), currentY - lr.getHeight(), lr.getWidth(), lr.getHeight());

                lowerRect = lr;
                lowerIndex++;
            }
        }
    }

    private void generateBoundingBoxes()
    {
        wordPositions = new HashMap<Word, SWCRectangle>();
        for (Word w : cycle)
            wordPositions.put(w, bbGenerator.getBoundingBox(w, w.weight));
    }

    @Override
    public SWCRectangle getWordRectangle(Word w)
    {
        return wordPositions.get(w);
    }

}
