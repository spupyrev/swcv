package edu.cloudy.layout;

import edu.cloudy.geom.BoundingBoxGenerator;
import edu.cloudy.geom.SWCRectangle;
import edu.cloudy.layout.overlaps.ForceDirectedOverlapRemoval;
import edu.cloudy.layout.overlaps.ForceDirectedUniformity;

import java.util.stream.IntStream;

/**
 * @author spupyrev
 * May 13, 2013
 */
public class InflateAndPushAlgo extends BaseLayoutAlgo
{
    public InflateAndPushAlgo()
    {
    }

    @Override
    protected void run()
    {
        double scale = 0.01;
        initialPlacement(scale);

        while (scale < 1.0)
        {
            //grow by 5%
            double delta = 0.1;
            double newScale = Math.min(scale + delta, 1.0);
            inflateRectangles(newScale / scale);
            scale = newScale;

            //remove overlaps
            new ForceDirectedOverlapRemoval<SWCRectangle>().run(wordPositions);
        }

        new ForceDirectedOverlapRemoval<SWCRectangle>(5000).run(wordPositions);
        new ForceDirectedUniformity<SWCRectangle>().run(wordPositions);
    }

    private void initialPlacement(double scale)
    {
        //find initial placement by mds layout
        MDSAlgo algo = new MDSAlgo();
        algo.setBoundingBoxGenerator(new BoundingBoxGenerator(scale));
        LayoutResult initialLayout = algo.layout(wordGraph);

        IntStream.range(0, words.length).forEach(i -> wordPositions[i] = initialLayout.getWordPosition(words[i]));
    }

    private void inflateRectangles(double scaleFactor)
    {
        for (int i = 0; i < wordPositions.length; i++)
        {
            SWCRectangle rect = wordPositions[i];
            double newWidth = rect.getWidth() * scaleFactor;
            double newHeight = rect.getHeight() * scaleFactor;
            SWCRectangle newRect = new SWCRectangle(rect.getX(), rect.getY(), newWidth, newHeight);
            newRect.setCenter(rect.getCenterX(), rect.getCenterY());

            wordPositions[i].setRect(newRect);
        }
    }
}
