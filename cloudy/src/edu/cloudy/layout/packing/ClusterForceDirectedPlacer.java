package edu.cloudy.layout.packing;

import edu.cloudy.layout.LayoutAlgo;
import edu.cloudy.layout.mds.DistanceScaling;
import edu.cloudy.layout.overlaps.ForceDirectedOverlapRemoval;
import edu.cloudy.nlp.Word;
import edu.cloudy.nlp.WordPair;
import edu.cloudy.utils.BoundingBoxGenerator;
import edu.cloudy.utils.SWCPoint;
import edu.cloudy.utils.SWCRectangle;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author spupyrev
 * May 3, 2013
 */
public class ClusterForceDirectedPlacer implements WordPlacer
{
    private static final double EPS = 1e-6;
    private static final double KA = 15;
    private static final double KR = 500;
    private static final double TOTAL_ITERATIONS = 500;
    private double T = 1;

    private List<Word> words;
    private Map<WordPair, Double> similarities;
    private Map<Word, SWCRectangle> wordPositions = new HashMap<Word, SWCRectangle>();
    private List<? extends LayoutAlgo> singlePlacers;

    private BoundingBoxGenerator bbGenerator;

    public ClusterForceDirectedPlacer(List<Word> words, Map<WordPair, Double> similarities, List<? extends LayoutAlgo> singlePlacers, BoundingBoxGenerator bbGenerator)
    {
        this.words = words;
        this.similarities = similarities;
        this.singlePlacers = singlePlacers;
        this.bbGenerator = bbGenerator;

        run();
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

    @Override
    public Set<Word> getWords()
    {
        return wordPositions.keySet();
    }

    private List<Cluster> clusters;

    private void run()
    {
        //get the groups of words: stars, cycles etc
        clusters = createClusters();

        initialPlacement();

        runForceDirected();

        restoreWordPositions();
    }

    private List<Cluster> createClusters()
    {
        List<Cluster> result = new ArrayList<Cluster>();
        for (int i = 0; i < singlePlacers.size(); i++)
            result.add(new Cluster());

        for (Word w : words)
        {
            SWCRectangle rect = null;
            for (int i = 0; i < singlePlacers.size(); i++)
            {
                SWCRectangle tmp = singlePlacers.get(i).getWordRectangle(w);
                if (tmp != null)
                {
                    result.get(i).wordPositions.put(w, tmp);
                    rect = tmp;
                    break;
                }
            }

            //create its own cluster
            if (rect == null)
            {
                Cluster c = new Cluster();
                c.wordPositions.put(w, bbGenerator.getBoundingBox(w, w.weight));
                result.add(c);
            }
        }

        return result;
    }

    //run MDS
    private void initialPlacement()
    {
        double maxWordSize = 0;
        double[][] desiredDistance = new double[clusters.size()][clusters.size()];
        for (int i = 0; i < clusters.size(); i++)
        {
            desiredDistance[i][i] = 0;
            for (SWCRectangle rect : clusters.get(i).wordPositions.values())
                maxWordSize = Math.max(maxWordSize, rect.getWidth());
        }

        double SCALING = 5.0;
        for (int i = 0; i < clusters.size(); i++)
            for (int j = i + 1; j < clusters.size(); j++)
            {
                double avgSim = 0, cnt = 0;

                //computiong average similarity between clusters
                for (Word wi : clusters.get(i).wordPositions.keySet())
                    for (Word wj : clusters.get(j).wordPositions.keySet())
                    {
                        WordPair wp = new WordPair(wi, wj);
                        if (similarities.containsKey(wp))
                            avgSim += similarities.get(wp);
                        cnt++;
                    }
                avgSim /= cnt;

                desiredDistance[i][j] = desiredDistance[j][i] = (1 - avgSim) * maxWordSize * SCALING;
            }

        //aply MDS
        double[][] outputMDS = new DistanceScaling().mds(desiredDistance, 2);

        // set coordinates
        for (int i = 0; i < clusters.size(); i++)
        {
            double x = outputMDS[0][i];
            double y = outputMDS[1][i];
            clusters.get(i).center = new SWCPoint(x, y);
            assert (!Double.isNaN(x) && !Double.isNaN(y));
        }
    }

    private void runForceDirected()
    {
        int numIterations = 0;

        while (numIterations++ < TOTAL_ITERATIONS)
        {
            if (numIterations % 1000 == 0)
                System.out.println("Finished Iteration " + numIterations);

            if (!doIteration())
                break;

            //cooling down the temperature (max allowed step is decreased)
            if (numIterations % 5 == 0)
                T *= 0.95;
        }
    }

    int ni = 0;

    /**
     * perform several iterations
     * returns 'true' iff the last iteration moves rectangles 'alot'
     */
    public boolean doIteration(int iters)
    {
        int i = 0;
        while (i++ < iters)
        {
            ni++;
            if (!doIteration())
                return false;

            if (ni % 5 == 0)
                T *= 0.95;
        }

        restoreWordPositions();
        return true;
    }

    /**
     * perform one iteration
     * returns 'true' iff the iteration moves rectangles 'alot'
     */
    private boolean doIteration()
    {
        SWCRectangle bb = computeBoundingBox(clusters);

        double avgStep = 0;
        // compute the displacement for the word in this time step
        for (int i = 0; i < clusters.size(); i++)
        {
            SWCPoint dxy = new SWCPoint(0, 0);

            if (!clusters.get(i).overlap(clusters))
            {
                //attractive force (compact principle)
                dxy.add(computeAttractiveForce(bb, clusters, clusters.get(i)));
                assert (!Double.isNaN(dxy.x()));
            }
            else
            {
                //repulsion force (removing overlaps)
                dxy.add(computeRepulsiveForce(bb, clusters, clusters.get(i)));
            }

            // move the rectangle
            dxy = normalize(dxy, bb);
            clusters.get(i).center.add(dxy);

            assert (!Double.isNaN(clusters.get(i).center.x()));
            assert (!Double.isNaN(clusters.get(i).center.y()));
            double len = dxy.length() / Math.max(bb.getWidth(), bb.getHeight());
            avgStep = Math.max(avgStep, len);
        }

        //avgStep /= clusters.size();
        //System.out.println("avgStep = " + avgStep);
        return avgStep > 0.0001;
    }

    private SWCPoint computeAttractiveForce(SWCRectangle bb, List<Cluster> clusters, Cluster cluster)
    {
        SWCPoint dxy = new SWCPoint(0, 0);

        double cnt = 0;
        for (int j = 0; j < clusters.size(); j++)
        {
            if (cluster.equals(clusters.get(j)))
                continue;

            dxy.add(cluster.computeAttractiveForce(bb, clusters.get(j)));
            cnt++;
        }

        if (cnt > 0)
            dxy.scale(1.0 / cnt);
        return dxy;
    }

    private SWCPoint computeRepulsiveForce(SWCRectangle bb, List<Cluster> clusters, Cluster cluster)
    {
        SWCPoint dxy = new SWCPoint(0, 0);

        double cnt = 0;
        for (int j = 0; j < clusters.size(); j++)
        {
            if (cluster.equals(clusters.get(j)))
                continue;

            dxy.subtract(cluster.computeRepulsiveForce(bb, clusters.get(j)));
            cnt++;
        }

        if (cnt > 0)
            dxy.scale(1.0 / cnt);
        dxy.scale(1.0 / cnt);
        return dxy;
    }

    private SWCRectangle computeBoundingBox(List<Cluster> clusters)
    {
        double minX = Double.POSITIVE_INFINITY;
        double maxX = Double.NEGATIVE_INFINITY;
        double minY = Double.POSITIVE_INFINITY;
        double maxY = Double.NEGATIVE_INFINITY;

        for (Cluster c : clusters)
        {
            SWCRectangle rect = c.getBoundingBox();

            minX = Math.min(minX, rect.getMinX());
            maxX = Math.max(maxX, rect.getMaxX());
            minY = Math.min(minY, rect.getMinY());
            maxY = Math.max(maxY, rect.getMaxY());
        }

        return new SWCRectangle(minX, minY, maxX - minX, maxY - minY);
    }

    private SWCPoint normalize(SWCPoint force, SWCRectangle bb)
    {
        //maximum allowed movement is 2% of the screen width/height
        double mx = Math.min(bb.getWidth(), bb.getHeight());
        double len = force.length();
        if (len < 1e-3)
            return force;

        double maxLen = Math.min(len, T * mx / 50.0);
        force.scale(maxLen / len);
        return force;
    }

    private void restoreWordPositions()
    {
        for (Cluster c : clusters)
            for (Word w : c.wordPositions.keySet())
                wordPositions.put(w, c.actualWordPosition(w));

        new ForceDirectedOverlapRemoval<SWCRectangle>().run(words, wordPositions);
    }

    private class Cluster
    {
        private Map<Word, SWCRectangle> wordPositions = new HashMap<Word, SWCRectangle>();
        private SWCPoint center;

        public SWCRectangle getBoundingBox()
        {
            SWCRectangle bb = new SWCRectangle();
            for (Word w : wordPositions.keySet())
            {
                SWCRectangle r = actualWordPosition(w);
                bb.add(r);
            }

            return bb;
        }

        public String toString()
        {
            StringBuffer sb = new StringBuffer();
            for (Word w : wordPositions.keySet())
                sb.append(" " + w.word);
            return sb.toString();
        }

        boolean overlap(List<Cluster> list)
        {
            for (Cluster c : list)
            {
                if (c.equals(this))
                    continue;

                if (overlap(c))
                    return true;
            }

            return false;
        }

        boolean overlap(Cluster other)
        {
            for (Word w1 : wordPositions.keySet())
                for (Word w2 : other.wordPositions.keySet())
                {
                    SWCRectangle rect1 = actualWordPosition(w1);
                    SWCRectangle rect2 = other.actualWordPosition(w2);

                    if (overlap(rect1, rect2))
                        return true;
                }

            return false;
        }

        SWCRectangle actualWordPosition(Word word)
        {
            SWCRectangle r1 = wordPositions.get(word);
            return new SWCRectangle(r1.getX() + center.x(), r1.getY() + center.y(), r1.getWidth(), r1.getHeight());
        }

        //TODO: change to overlaps metric!!!
        private boolean overlap(SWCRectangle rect1, SWCRectangle rect2)
        {
            if (rect1.intersects(rect2))
            {
                double hix = Math.min(rect1.getMaxX(), rect2.getMaxX());
                double lox = Math.max(rect1.getMinX(), rect2.getMinX());
                double hiy = Math.min(rect1.getMaxY(), rect2.getMaxY());
                double loy = Math.max(rect1.getMinY(), rect2.getMinY());
                double dx = hix - lox; // hi > lo
                double dy = hiy - loy;
                if (Math.min(dx, dy) > 1)
                    return true;
            }

            return false;
        }

        SWCPoint computeRepulsiveForce(SWCRectangle bb, Cluster other)
        {
            if (!overlap(other))
                return new SWCPoint();

            // compute the displacement due to the overlap repulsive force
            SWCRectangle rectI = getBoundingBox();
            SWCRectangle rectJ = other.getBoundingBox();

            assert (rectI.intersects(rectJ));

            double hix = Math.min(rectI.getMaxX(), rectJ.getMaxX());
            double lox = Math.max(rectI.getMinX(), rectJ.getMinX());
            double hiy = Math.min(rectI.getMaxY(), rectJ.getMaxY());
            double loy = Math.max(rectI.getMinY(), rectJ.getMinY());
            double dx = hix - lox; // hi > lo
            double dy = hiy - loy;
            assert (dx >= -EPS);
            assert (dy >= -EPS);

            double force = KR * Math.min(dx, dy);

            SWCPoint dir = new SWCPoint(other.center.x() - center.x(), other.center.y() - center.y());
            double len = dir.length();
            if (len < EPS)
                dir = SWCPoint.random();

            dir.normalize();
            dir.scale(force);

            return dir;
        }

        SWCPoint computeAttractiveForce(SWCRectangle bb, Cluster other)
        {
            double force = center.distance(other.center);
            if (T < 0.5)
                force *= T;

            SWCPoint dir = new SWCPoint(other.center.x() - center.x(), other.center.y() - center.y());
            dir.normalize();
            dir.scale(force);

            return dir;
        }
    }

}
