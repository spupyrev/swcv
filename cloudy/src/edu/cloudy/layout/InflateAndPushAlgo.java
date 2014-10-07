package edu.cloudy.layout;

import edu.cloudy.geom.BoundingBoxGenerator;
import edu.cloudy.geom.SWCRectangle;
import edu.cloudy.layout.overlaps.ForceDirectedOverlapRemoval;
import edu.cloudy.layout.overlaps.ForceDirectedUniformity;
import edu.cloudy.nlp.Word;
import edu.cloudy.nlp.WordPair;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author spupyrev
 * May 13, 2013
 *
 * i added the "inflate" part to the algorithm and changed the overlap removal code
 */
public class InflateAndPushAlgo extends BaseLayoutAlgo
{

    public InflateAndPushAlgo(List<Word> words, Map<WordPair, Double> similarity)
    {
        super(words, similarity);
    }

    @Override
    public void run()
    {
        double scale = 0.01;
        wordPositions = initialPlacement(scale);

        while (scale < 1.0)
        {
            //grow by 5%
            double delta = 0.1;
            double newScale = Math.min(scale + delta, 1.0);
            inflateRectangles(newScale / scale);
            scale = newScale;

            //remove overlaps
            new ForceDirectedOverlapRemoval<SWCRectangle>().run(words, wordPositions);
            //new ForceDirectedOverlapRemovalFast<SWCRectangle>().run(words, wordPositions);
            //new InflateAndPushOverlapRemoval().run(words, wordPositions);
        }

        new ForceDirectedOverlapRemoval<SWCRectangle>(5000).run(words, wordPositions);
        new ForceDirectedUniformity<SWCRectangle>().run(words, wordPositions);
    }

    private Map<Word, SWCRectangle> initialPlacement(double scale)
    {
        //find initial placement by mds layout
        MDSAlgo algo = new MDSAlgo(words, similarity);
        algo.setBoundingBoxGenerator(new BoundingBoxGenerator(scale));
        algo.run();

        //run mds
        Map<Word, SWCRectangle> wordPositions = new HashMap<Word, SWCRectangle>();
        for (Word w : words)
        {
            SWCRectangle rect = new SWCRectangle(algo.getWordPosition(w));
            wordPositions.put(w, rect);
        }

        return wordPositions;
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
