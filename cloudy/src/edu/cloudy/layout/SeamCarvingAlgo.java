package edu.cloudy.layout;

import edu.cloudy.geom.SWCPoint;
import edu.cloudy.geom.SWCRectangle;
import edu.cloudy.layout.overlaps.ForceDirectedOverlapRemoval;
import edu.cloudy.layout.overlaps.ForceDirectedUniformity;
import edu.cloudy.nlp.Word;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author spupyrev
 * May 12, 2013
 */
public class SeamCarvingAlgo extends BaseLayoutAlgo
{
    public SeamCarvingAlgo()
    {
    }

    @Override
    protected void run()
    {
        SWCRectangle[] initialWordPositions = initialPlacement();

        //compute the zones
        Zone[][] zones = createZones(initialWordPositions);

        //run seam carving
        wordPositions = removeSeams(zones, initialWordPositions);

        new ForceDirectedOverlapRemoval<SWCRectangle>().run(wordPositions);
        new ForceDirectedUniformity<SWCRectangle>().run(wordPositions);
    }

    private SWCRectangle[] initialPlacement()
    {
        //initial layout
        LayoutResult initialLayout = new MDSAlgo(false).layout(wordGraph);
        
        SWCRectangle[] wordPositions = new SWCRectangle[words.length];
        int i = 0;
        for (Word w : words)
        {
            wordPositions[i] = initialLayout.getWordPosition(w);
            i++;
        }
        return wordPositions;
    }

    /**
     * create n*m array of Zones
     */
    private Zone[][] createZones(SWCRectangle[] wordPositions)
    {
        //alignWords(wordPositions);

        Set<Double> xValues = new HashSet<Double>();
        Set<Double> yValues = new HashSet<Double>();

        //double minX = Double.POSITIVE_INFINITY, maxX = Double.NEGATIVE_INFINITY;
        //double minY = Double.POSITIVE_INFINITY, maxY = Double.NEGATIVE_INFINITY;
        for (SWCRectangle rect : wordPositions)
        {
            xValues.add(rect.getMinX());
            xValues.add(rect.getMaxX());
            yValues.add(rect.getMinY());
            yValues.add(rect.getMaxY());

            //minX = Math.min(minX, rect.getMinX());
            //maxX = Math.max(maxX, rect.getMaxX());
            //minY = Math.min(minY, rect.getMinY());
            //maxY = Math.max(maxY, rect.getMaxY());
        }

        //removing equal values
        double[] xx = new double[xValues.size()];
        double[] yy = new double[yValues.size()];
        int it = 0;
        for (Double d : xValues)
            xx[it++] = d;

        it = 0;
        for (Double d : yValues)
            yy[it++] = d;

        Arrays.sort(xx);
        Arrays.sort(yy);

        int n = xx.length - 1;
        int m = yy.length - 1;

        //assume that the first coordinate is x
        Zone[][] zones = new Zone[n][m];
        for (int i = 0; i < n; i++)
        {
            for (int j = 0; j < m; j++)
            {
                SWCRectangle r = new SWCRectangle(xx[i], yy[j], xx[i + 1] - xx[i], yy[j + 1] - yy[j]);
                zones[i][j] = new Zone(r, i, j);

                //zones[i][j].setOccupied(zoneIsOccupied(zones[i][j], wordPositions));
            }
        }

        //checkZoneConsistency(zones, wordPositions);

        return zones;
    }

    /**
     * make sure that no pairs of words have
     *  0 < rect1.getX - rect2.getX < eps
     *  TODO: unfortunately, this alignment may introduce overlaps
     */
    private void alignWords(SWCRectangle[] wordPositions)
    {
        double EPS = 0.1;

        List<Double> xValues = new ArrayList<Double>();
        List<Double> yValues = new ArrayList<Double>();

        for (SWCRectangle rect : wordPositions)
        {
            xValues.add(rect.getMinX());
            xValues.add(rect.getMaxX());
            yValues.add(rect.getMinY());
            yValues.add(rect.getMaxY());
        }

        Collections.sort(xValues);
        Collections.sort(yValues);

        Map<Double, Double> indX = new HashMap<Double, Double>();
        int top = 0;
        for (int i = 0; i < xValues.size(); i++)
            if (xValues.get(i) > xValues.get(top) + EPS)
            {
                top++;
                xValues.set(top, xValues.get(i));
                indX.put(xValues.get(i), xValues.get(top));
            }
            else
            {
                indX.put(xValues.get(i), xValues.get(top));
            }

        for (SWCRectangle rect : wordPositions)
        {
            double oldX = rect.getX();
            double newX = indX.get(rect.getX());
            if (oldX != newX)
            {
                rect.setRect(newX, rect.getY(), rect.getWidth(), rect.getHeight());
                //Logger.println("moved X from " + oldX + " to " + newX);
            }
        }

        Map<Double, Double> indY = new HashMap<Double, Double>();
        top = 0;
        for (int i = 0; i < yValues.size(); i++)
            if (yValues.get(i) > yValues.get(top) + EPS)
            {
                top++;
                yValues.set(top, yValues.get(i));
                indY.put(yValues.get(i), yValues.get(top));
            }
            else
            {
                indY.put(yValues.get(i), yValues.get(top));
            }

        for (SWCRectangle rect : wordPositions)
        {
            double oldY = rect.getY();
            double newY = indY.get(rect.getY());
            if (oldY != newY)
            {
                rect.setRect(rect.getX(), newY, rect.getWidth(), rect.getHeight());
                //Logger.println("moved Y from " + oldY + " to " + newY);
            }
        }

    }

    /**
     * Determines the order of removing horizontal and vertical seams
     * 
     * @return -- Map of words to rectangle2D's
     */
    private SWCRectangle[] removeSeams(Zone[][] zones, SWCRectangle[] wordPositions)
    {
        //calculate the largest word
        double maxWordSize = computeMaxWordSizes(wordPositions);
        //scaling factor is used to normalize energy 
        double scalingFactor = computeScalingFactor(wordPositions);

        int MAX_ITERATIONS = 500;
        int iter = 0;
        double minSeamSize = 10;

        while (iter++ < MAX_ITERATIONS)
        {
            if (iter % 30 == 0)
                alignWords(wordPositions);

            double[][] E = energy(zones, wordPositions, maxWordSize, scalingFactor);
            List<Zone> horizontalSeam = new ArrayList<Zone>();
            List<Zone> verticalSeam = new ArrayList<Zone>();

            double horizontalSeamCost = findOptimalSeam(true, zones, wordPositions, E, horizontalSeam, minSeamSize);
            double verticalSeamCost = findOptimalSeam(false, zones, wordPositions, E, verticalSeam, minSeamSize);

            //no more removals
            if (horizontalSeamCost >= Double.POSITIVE_INFINITY && verticalSeamCost >= Double.POSITIVE_INFINITY)
            {
                /*Logger.println("\n-------------------\nEnergy table");
                for (int i = 0; i < E.length; i++) {
                	for (int j = 0; j < E[i].length; j++) {
                		if (Double.isInfinite(E[i][j]))
                			Logger.printf(" -----");
                		else
                			Logger.printf(" %.2f", E[i][j]);
                		Logger.printf("(%.2f)", zones[i][j].getHeight());
                	}

                	Logger.println("");
                }*/

                if (minSeamSize <= 0.5)
                    break;
                minSeamSize /= 3.0;
                continue;
            }

            if (horizontalSeamCost < verticalSeamCost)
            {
                //Logger.println("horizontalSeamCost = " + horizontalSeamCost);
                removeHorizontalSeamByFullReconstruction(zones, wordPositions, horizontalSeam);
            }
            else
            {
                //Logger.println("verticalSeamCost = " + verticalSeamCost);
                removeVerticalSeamByFullReconstruction(zones, wordPositions, verticalSeam);
            }

            //removeHorizontalSeam(zones, wordPositions, zonePath);

            //this method is not slower, but much simplier
            zones = createZones(wordPositions);
            //checkZoneConsistency(zones, wordPositions);
        }

        //Logger.println("done " + iter + " iterations");
        return wordPositions;
    }

    private void removeHorizontalSeamByFullReconstruction(Zone[][] zones, SWCRectangle[] wordPositions, List<Zone> zonePath)
    {
        Map<Double, Double> zoneY = new HashMap<Double, Double>();
        double minHeight = Double.POSITIVE_INFINITY;
        for (Zone z : zonePath)
        {
            minHeight = Math.min(minHeight, z.getRectangle().getHeight());
            zoneY.put(z.getRectangle().getMinX(), z.getRectangle().getMinY());
        }

        //Logger.println("minHeight = " + minHeight);

        //remove minHeight
        for (SWCRectangle rect : wordPositions)
        {
            double removedZoneY = zoneY.get(rect.getMinX());
            if (removedZoneY < rect.getMinY())
                rect.setRect(rect.getMinX(), rect.getMinY() - minHeight, rect.getWidth(), rect.getHeight());
        }
    }

    private void removeVerticalSeamByFullReconstruction(Zone[][] zones, SWCRectangle[] wordPositions, List<Zone> zonePath)
    {
        Map<Double, Double> zoneX = new HashMap<Double, Double>();
        double minWidth = Double.POSITIVE_INFINITY;
        for (Zone z : zonePath)
        {
            minWidth = Math.min(minWidth, z.getRectangle().getWidth());
            zoneX.put(z.getRectangle().getMinY(), z.getRectangle().getMinX());
        }

        //Logger.println("minWidth = " + minWidth);

        //remove minWidth
        for (SWCRectangle rect : wordPositions)
        {
            double removedZoneX = zoneX.get(rect.getMinY());
            if (removedZoneX < rect.getMinX())
                rect.setRect(rect.getMinX() - minWidth, rect.getMinY(), rect.getWidth(), rect.getHeight());
        }
    }

    @SuppressWarnings("unused")
    private void checkZoneConsistency(Zone[][] zones, SWCRectangle[] wordPositions)
    {
        int n = zones.length;
        int m = zones[0].length;

        //check indices
        for (int i = 0; i < n; i++)
            for (int j = 0; j < m; j++)
            {
                assert (zones[i][j].getIndexI() == i);
                assert (zones[i][j].getIndexJ() == j);
            }

        //check zone coordinates
        for (int i = 0; i < n; i++)
            for (int j = 0; j < m; j++)
            {
                assert (j + 1 >= m || zones[i][j].getWidth() == zones[i][j + 1].getWidth());
                assert (i + 1 >= n || zones[i][j].getHeight() == zones[i + 1][j].getHeight());
            }

        //check word positions
        for (int i = 0; i < n; i++)
        {
            for (int j = 0; j < m; j++)
            {
                boolean occupy = zoneIsOccupied(zones[i][j], wordPositions);
                assert (zones[i][j].isOccupied == occupy);
            }
        }

    }

    private boolean zoneIsOccupied(Zone zone, SWCRectangle[] wordPositions)
    {
        SWCPoint center = new SWCPoint(zone.getRectangle().getCenterX(), zone.getRectangle().getCenterY());
        for (SWCRectangle rect : wordPositions)
        {
            if (rect.contains(center))
                return true;
        }

        return false;
    }

    //sets the value of maxSize of all the words
    private double computeMaxWordSizes(SWCRectangle[] wordPositions)
    {
        double maxWordSize = 0;
        for (SWCRectangle rect : wordPositions)
        {
            double renderedSize = rect.getWidth();
            maxWordSize = Math.max(maxWordSize, renderedSize);
        }

        return maxWordSize;
    }

    private double computeScalingFactor(SWCRectangle[] wordPositions)
    {
        double minX = Double.POSITIVE_INFINITY, maxX = Double.NEGATIVE_INFINITY;
        double minY = Double.POSITIVE_INFINITY, maxY = Double.NEGATIVE_INFINITY;

        for (SWCRectangle rect : wordPositions)
        {
            minX = Math.min(minX, rect.getMinX());
            maxX = Math.max(maxX, rect.getMaxX());
            minY = Math.min(minY, rect.getMinY());
            maxY = Math.max(maxY, rect.getMaxY());
        }

        return Math.max(maxX - minX, maxY - minY) / 2;
    }

    /**
     * computes the dynamic programming table for horizontal seams
     */
    private double findOptimalSeam(boolean horizontal, Zone[][] zones, SWCRectangle[] wordPositions, double[][] E, List<Zone> zonePath, double minSeamSize)
    {
        int n = zones.length;
        int m = zones[0].length;

        double[][] Ec = new double[n][m];
        int[][] parent = new int[n][m];

        // Fill in the table
        for (int cell = 0; cell < n * m; cell++)
        {
            int i, j;
            if (horizontal)
            {
                i = cell / m;
                j = cell % m;
            }
            else
            {
                i = cell % n;
                j = cell / n;
            }

            parent[i][j] = -1;
            Ec[i][j] = 0;

            if (zones[i][j].isOccupied)
            {
                Ec[i][j] = Double.POSITIVE_INFINITY;
                continue;
            }

            int dt = 5;
            //find optimum on previous row
            if (horizontal && i - 1 >= 0)
            {
                Ec[i][j] = Double.POSITIVE_INFINITY;
                for (int t = -dt; t <= dt; t++)
                    if (j + t >= 0 && j + t < m)
                        if (Ec[i][j] > Ec[i - 1][j + t])
                        {
                            Ec[i][j] = Ec[i - 1][j + t];
                            parent[i][j] = j + t;
                        }
            }
            else if (!horizontal && j - 1 >= 0)
            {
                Ec[i][j] = Double.POSITIVE_INFINITY;
                for (int t = -dt; t <= dt; t++)
                    if (i + t >= 0 && i + t < n)
                        if (Ec[i][j] > Ec[i + t][j - 1])
                        {
                            Ec[i][j] = Ec[i + t][j - 1];
                            parent[i][j] = i + t;
                        }
            }

            //add current energy
            //make sure that is is beneficial to choose larger zones 
            double size = (horizontal ? zones[i][j].getHeight() : zones[i][j].getWidth());
            if (size < minSeamSize)
                Ec[i][j] = Double.POSITIVE_INFINITY;
            else if (size < 1.0)
                Ec[i][j] += E[i][j] / size;
            else
                Ec[i][j] += E[i][j] * size;
        }

        /*System.out.println("Seam table");
        for (int i = 0; i < Ec.length; i++) {
        	for (int j = 0; j < Ec[i].length; j++) {
        		if (Double.isInfinite(Ec[i][j]))
        			System.out.printf(" ----");
        		else
        			System.out.printf(" %.2f", Ec[i][j]);
        	}
        	System.out.println();
        }
        
        System.out.println("\n-------------------\nEnergy table");
        
        for (int i = 0; i < E.length; i++) {
        	for (int j = 0; j < E[i].length; j++) {
        		if (Double.isInfinite(E[i][j]))
        			System.out.printf(" ----");
        		else
        			System.out.printf(" %.2f", E[i][j]);
        	}
        	System.out.println();
        }*/

        //find optimal column
        int minIndex = -1;
        double minSum = Double.POSITIVE_INFINITY;

        if (horizontal)
        {
            for (int i = 0; i < m; i++)
            {
                if (minSum > Ec[n - 1][i])
                {
                    minSum = Ec[n - 1][i];
                    minIndex = i;
                }
            }
        }
        else
        {
            for (int i = 0; i < n; i++)
            {
                if (minSum > Ec[i][m - 1])
                {
                    minSum = Ec[i][m - 1];
                    minIndex = i;
                }
            }
        }

        //System.out.println("minSum");
        if (minIndex == -1)
            return Double.POSITIVE_INFINITY;

        //System.out.println("\nMin index is: " + minIndex);
        //System.out.println("Min sum is: " + minSum);

        int curI = n - 1;
        int curJ = m - 1;
        if (horizontal)
            curJ = minIndex;
        else
            curI = minIndex;

        while (curI >= 0 && curJ >= 0)
        {
            if (zones[curI][curJ].isOccupied)
                throw new RuntimeException("smth wrong with dp");

            zonePath.add(zones[curI][curJ]);
            if (horizontal)
            {
                curJ = parent[curI][curJ];
                curI--;
            }
            else
            {
                curI = parent[curI][curJ];
                curJ--;
            }
        }

        if (curI != -1 || curJ != -1)
            throw new RuntimeException("smth wrong with dp");

        return minSum;
    }

    /**
     * computes the energy for all zones
     */
    private double[][] energy(Zone[][] zones, SWCRectangle[] wordPositions, double maxWordSize, double scalingFactor)
    {
        int n = zones.length;
        int m = zones[0].length;

        double[][] E = new double[n][m];

        for (int i = 0; i < n; i++)
            for (int j = 0; j < m; j++)
                E[i][j] = energy(zones[i][j], wordPositions, maxWordSize, scalingFactor);

        return E;
    }

    /**
     * compute energy for the specified zone
     */
    private double energy(Zone zone, SWCRectangle[] wordPositions, double maxWordSize, double scalingFactor)
    {
        if (zone.isOccupied)
            return Double.POSITIVE_INFINITY;

        double result = 0;

        for (SWCRectangle rect : wordPositions)
        {
            if (rect.contains(zone.getRectangle().getCenterX(), zone.getRectangle().getCenterY()))
            {
                zone.isOccupied = true;
                return Double.POSITIVE_INFINITY;
            }

            double wordSize = rect.getWidth();

            double mux = rect.getCenterX();
            double muy = rect.getCenterY();
            double x = zone.getRectangle().getCenterX();
            double y = zone.getRectangle().getCenterY();

            double diffX = (x - mux) / scalingFactor;
            double diffY = (y - muy) / scalingFactor;

            double exponent = (diffX * diffX + diffY * diffY) / 2;

            //this works a way faster than Math.exp
            result += (wordSize / maxWordSize) * (1.0 / 2 * Math.PI) * (fastexp(-exponent));
            //result += (wordSize / maxWordSize) * (1.0 / 2 * Math.PI) * (Math.exp(-exponent));
        }

        assert (result < Double.POSITIVE_INFINITY);
        return result;
    }

    public static double fastexp(double val)
    {
        final long tmp = (long)(1512775 * val + 1072632447);
        return Double.longBitsToDouble(tmp << 32);
    }

    private class Zone
    {
        private SWCRectangle rect;
        boolean isOccupied = false;
        private int indexI;
        private int indexJ;

        public Zone(SWCRectangle rect, int i, int j)
        {
            this.rect = rect;
            indexI = i;
            indexJ = j;
        }

        public SWCRectangle getRectangle()
        {
            return rect;
        }

        public double getHeight()
        {
            return rect.getHeight();
        }

        public double getWidth()
        {
            return rect.getWidth();
        }

        public int getIndexI()
        {
            return indexI;
        }

        public int getIndexJ()
        {
            return indexJ;
        }
    }
}
