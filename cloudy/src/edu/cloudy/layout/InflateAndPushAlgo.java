package edu.cloudy.layout;

import edu.cloudy.geom.BoundingBoxGenerator;
import edu.cloudy.geom.SWCRectangle;
import edu.cloudy.layout.overlaps.ForceDirectedOverlapRemoval;
import edu.cloudy.layout.overlaps.ForceDirectedUniformity;
import edu.cloudy.nlp.Word;
import edu.cloudy.nlp.WordPair;

import java.util.List;
import java.util.Map;

/**
 * @author spupyrev
 * May 13, 2013
 */
public class InflateAndPushAlgo extends BaseLayoutAlgo
{
    public InflateAndPushAlgo(List<Word> words, Map<WordPair, Double> similarity)
    {
        super(words, similarity);
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
            new ForceDirectedOverlapRemoval<SWCRectangle>().run(words, wordPositions);
        }

        new ForceDirectedOverlapRemoval<SWCRectangle>(5000).run(words, wordPositions);
        new ForceDirectedUniformity<SWCRectangle>().run(words, wordPositions);
    }

    private void initialPlacement(double scale)
    {
        //find initial placement by mds layout
        MDSAlgo algo = new MDSAlgo(words, similarity);
        algo.setBoundingBoxGenerator(new BoundingBoxGenerator(scale));
        LayoutResult initialLayout = algo.layout();
        
        words.forEach(w -> wordPositions.put(w, initialLayout.getWordPosition(w)));
    }

    private void inflateRectangles(double scaleFactor)
    {
        for (Word w : wordPositions.keySet())
        {
            SWCRectangle rect = wordPositions.get(w);
            double newWidth = rect.getWidth() * scaleFactor;
            double newHeight = rect.getHeight() * scaleFactor;
            SWCRectangle newRect = new SWCRectangle(rect.getX(), rect.getY(), newWidth, newHeight);
            newRect.setCenter(rect.getCenterX(), rect.getCenterY());

            wordPositions.get(w).setRect(newRect);
        }
    }
}
