package edu.cloudy.layout;

import edu.cloudy.geom.SWCRectangle;
import edu.cloudy.layout.mds.DistanceScaling;
import edu.cloudy.layout.overlaps.ForceDirectedOverlapRemoval;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * @author spupyrev
 * May 12, 2013
 * 
 * 1. compute mds on dissimilarity matrix
 * 2. remove overlaps
 * 
 * (the algorithm is not supposed to compact the drawing!)
 */
public class MDSAlgo extends BaseLayoutAlgo
{
    private boolean useOverlapRemoval = true;

    public MDSAlgo(boolean useOverlapRemoval)
    {
        this.useOverlapRemoval = useOverlapRemoval;
    }

    public MDSAlgo()
    {
        this(true);
    }

    @Override
    protected void run()
    {
        //maps words to their bounding rectangle
        generateBoundingBoxes();

        //mds placement
        computeInitialPlacement();

        //force-directed overlap removal
        if (useOverlapRemoval)
            new ForceDirectedOverlapRemoval<SWCRectangle>().run(wordPositions);
    }

    private void computeInitialPlacement()
    {
        double[][] outputMDS = runMDS();

        // set coordinates
        for (int i = 0; i < words.length; i++)
        {
            SWCRectangle rect = wordPositions[i];
            double x = outputMDS[0][i];
            double y = outputMDS[1][i];
            x -= rect.getWidth() / 2.;
            y -= rect.getHeight() / 2.;
            rect.setRect(x, y, rect.getWidth(), rect.getHeight());
        }

        perturbOverlappingPoints();
    }

    private void perturbOverlappingPoints()
    {
        // Perturb coincident word positions
        double EPS = 0.1;
        boolean progress = false;
        while (progress)
        {
            progress = false;
            for (int i = 0; i < words.length; i++)
                for (int j = i + 1; j < words.length; j++)
                {
                    SWCRectangle r1 = wordPositions[i];
                    SWCRectangle r2 = wordPositions[j];

                    if ((Math.abs(r1.getX() - r2.getX())) < EPS && (Math.abs(r1.getY() - r2.getY()) < EPS))
                    {
                        Random r = new Random();
                        r1.setRect(r1.getX() + r.nextDouble(), r1.getY() + r.nextDouble(), r1.getWidth(), r1.getHeight());
                        progress = true;
                    }
                }
        }
    }

    private double[][] runMDS()
    {
        double scaling = computeScaling();

        double[][] desiredDistance = new double[words.length][words.length];
        for (int i = 0; i < words.length; i++)
            for (int j = 0; j < words.length; j++)
            {
                double dist = wordGraph.distance(words[i], words[j]);
                desiredDistance[i][j] = dist * scaling;
            }

        //aply MDS
        double[][] outputMDS = new DistanceScaling().mds(desiredDistance, 2);

        for (int i = 0; i < desiredDistance[0].length; i++)
        {
            assert (!Double.isNaN(outputMDS[0][i]));
            assert (!Double.isNaN(outputMDS[1][i]));
        }

        return outputMDS;
    }

    private double computeScaling()
    {
        double areaSum = Arrays.stream(wordPositions).mapToDouble(r -> r.getArea()).sum();

        List<Double> distances = new ArrayList();
        for (int i = 0; i < words.length; i++)
            for (int j = 0; j < words.length; j++)
            {
                double dist = wordGraph.distance(words[i], words[j]);
                distances.add(dist);
            }

        double avgDist = distances.stream().mapToDouble(w -> w).average().orElse(1.0);

        return Math.sqrt(areaSum) / avgDist;
    }

}
