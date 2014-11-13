package edu.cloudy.layout;

import edu.cloudy.geom.SWCRectangle;

/**
 * @author spupyrev
 * May 15, 2013
 */
public class SingleCycleAlgo extends BaseLayoutAlgo
{
    private static boolean LAYOUT_VERTICAL = false;

    public SingleCycleAlgo()
    {
    }

    @Override
    protected void run()
    {
        generateBoundingBoxes();
        if (words.length == 1)
            return;

        if (LAYOUT_VERTICAL && words.length <= 30)
            verticalLayout();
        else
            horizontalLayout();
    }

    private void horizontalLayout()
    {
        int lowerIndex = 0;
        SWCRectangle lowerRect = wordPositions[lowerIndex];
        //keep it unchanged
        double currentX = lowerRect.getMaxX();

        int upperIndex = words.length - 1;
        SWCRectangle upperRect = wordPositions[upperIndex];
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
                SWCRectangle ur = wordPositions[upperIndex - 1];
                ur.setRect(currentX, upperRect.getMaxY(), ur.getWidth(), ur.getHeight());

                upperRect = ur;
                upperIndex--;
            }
            else
            {
                //placing a new rectangle on bottom
                SWCRectangle lr = wordPositions[lowerIndex + 1];
                lr.setRect(currentX - lr.getWidth(), lowerRect.getMaxY(), lr.getWidth(), lr.getHeight());

                lowerRect = lr;
                lowerIndex++;
            }
        }
    }

    private void verticalLayout()
    {
        int lowerIndex = 0;
        SWCRectangle lowerRect = wordPositions[lowerIndex];
        //keep it unchanged
        double currentY = lowerRect.getMaxY();

        int upperIndex = words.length - 1;
        SWCRectangle upperRect = wordPositions[upperIndex];
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
                SWCRectangle ur = wordPositions[upperIndex - 1];
                ur.setRect(upperRect.getMaxX(), currentY, ur.getWidth(), ur.getHeight());

                upperRect = ur;
                upperIndex--;
            }
            else
            {
                //placing a new rectangle on bottom
                SWCRectangle lr = wordPositions[lowerIndex + 1];
                lr.setRect(lowerRect.getMaxX(), currentY - lr.getHeight(), lr.getWidth(), lr.getHeight());

                lowerRect = lr;
                lowerIndex++;
            }
        }
    }
}
