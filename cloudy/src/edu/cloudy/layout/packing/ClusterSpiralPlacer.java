package edu.cloudy.layout.packing;

import edu.cloudy.nlp.Word;
import edu.cloudy.utils.BoundingBoxGenerator;
import edu.cloudy.utils.SWCPoint;
import edu.cloudy.utils.SWCRectangle;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Jan 17, 2014
 */
public class ClusterSpiralPlacer implements WordPlacer
{
    private List<Cluster> clusters;
    private Map<Word, SWCRectangle> wordPositions = new HashMap<Word, SWCRectangle>();

    public ClusterSpiralPlacer(List<Cluster> clusters)
    {
        this.clusters = clusters;
    }

    public ClusterSpiralPlacer(List<Word> words, BoundingBoxGenerator bbGenerator)
    {
        this.clusters = createClusters(words, bbGenerator);
    }

    private List<Cluster> createClusters(List<Word> words, BoundingBoxGenerator bbGenerator)
    {
        List<Cluster> result = new ArrayList<Cluster>();
        for (Word w : words)
        {
            Cluster c = new Cluster();
            c.wordPositions.put(w, bbGenerator.getBoundingBox(w));
            result.add(c);
        }

        return result;
    }

    public void run()
    {
        // make the largest cluster come first
        Collections.sort(clusters, new Comparator<Cluster>()
        {
            public int compare(Cluster c1, Cluster c2)
            {
                return c2.wordPositions.size() - c1.wordPositions.size();
            }
        });

        boolean isFirstCluster = true;
        double scale = 1000;
        List<Cluster> placedClusters = new LinkedList<Cluster>();
        for (Cluster c : clusters)
        {
            if (isFirstCluster)
            {
                c.center = new SWCPoint(0, 0);
                placedClusters.add(c);
                isFirstCluster = false;
            }
            else
            {
                c.center = new SWCPoint(0, 0);

                int spiralPosition = 0;

                SWCRectangle rect = c.wordPositions.values().iterator().next();
                while (c.overlap(placedClusters))
                {
                    spiralOut(c, spiralPosition, scale);
                    spiralPosition++;
                }
                scale += 3.0;
                placedClusters.add(c);
            }
        }

        restoreWordPositions();
    }

    private static void spiralOut(Cluster c, int spiralValue, double scale)
    {
        SWCPoint p = c.center;

        SWCPoint newCenter;
        newCenter = new SWCPoint(p.x() + scale * Math.sqrt(spiralValue) * Math.cos(spiralValue), p.y() + scale * Math.sqrt(spiralValue)
                * Math.sin(spiralValue));

        c.center = newCenter;

    }

    private void restoreWordPositions()
    {
        for (Cluster c : clusters)
            for (Word w : c.wordPositions.keySet())
                wordPositions.put(w, c.actualWordPosition(w));
    }

    @Override
    public SWCRectangle getRectangleForWord(Word w)
    {
        assert (wordPositions.containsKey(w));
        return wordPositions.get(w);
    }

    @Override
    public boolean contains(Word w)
    {
        // TODO Auto-generated method stub
        return false;
    }

}
