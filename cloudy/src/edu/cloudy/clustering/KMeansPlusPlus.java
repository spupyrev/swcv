package edu.cloudy.clustering;

import edu.cloudy.layout.WordGraph;
import edu.cloudy.nlp.Word;
import edu.cloudy.nlp.WordPair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * @author spupyrev
 * Nov 28, 2013
 */
public class KMeansPlusPlus implements IClusterAlgo
{
    private Random rnd = new Random(123);

    private int K;

    private List<Word> words;
    private Map<WordPair, Double> similarities;

    private WordGraph wordGraph;

    public KMeansPlusPlus(int K)
    {
        this.K = K;
    }

    @Override
    public ClusterResult run(WordGraph wordGraph)
    {
        this.wordGraph = wordGraph;
        this.words = wordGraph.getWords();
        this.similarities = wordGraph.getSimilarity();

        if (words.isEmpty())
            return null;

        return runInternal();
    }

    private ClusterResult runInternal()
    {
        assert (K >= 1);

        ClusterResult res = null;
        double bestValue = -1;

        int maxAttempts = 600 / words.size();
        maxAttempts = Math.min(maxAttempts, 10);
        maxAttempts = Math.max(maxAttempts, 2);
        for (int attempt = 0; attempt < maxAttempts; attempt++)
        {
            List<Word> centers = chooseCenters(K);

            ClusterResult clusterInfo = groupPoints(centers);
            updateClusters(clusterInfo);

            if (bestValue == -1 || bestValue < clusterInfo.getModularity())
            {
                bestValue = clusterInfo.getModularity();
                res = clusterInfo;
            }
        }

        assert (res != null);
        //System.out.println("final cluster quality: " + res.getModularity());
        //System.out.println("K=" + K + ";  #clusters=" + res.getClusterCount());
        res.compress();
        return res;
    }

    private List<Word> chooseCenters(int K)
    {
        List<Word> centers = new ArrayList<Word>();

        int first = rnd.nextInt(words.size());
        centers.add(words.get(first));

        for (int i = 1; i < K; i++)
        {
            Word p = getNextMean(centers);
            if (p != null)
                centers.add(p);
        }

        return centers;
    }

    private Word getNextMean(List<Word> means)
    {
        List<Double> minDist = new ArrayList<Double>();
        for (int i = 0; i < words.size(); i++)
        {
            double minD = 123456789.0;
            for (int j = 0; j < means.size(); j++)
            {
                double d = wordGraph.distance(words.get(i), means.get(j));
                minD = Math.min(minD, d);
            }

            minDist.add(minD * minD);
        }

        int p = randomWithProbability(minDist);
        if (minDist.get(p) < 1e-6)
            return null;

        return words.get(p);
    }

    private int randomWithProbability(List<Double> prob)
    {
        double sum = 0;
        for (double d : prob)
            sum += d;

        double P = rnd.nextDouble();
        double cur = 0;
        for (int i = 0; i < prob.size(); i++)
        {
            double p = prob.get(i) / sum;
            if (p <= 1e-6)
                continue;
            if (cur <= P && P < cur + p)
                return i;
            cur += p;
        }

        return prob.size() - 1;
    }

    private Word computeMedian(List<Word> group)
    {
        assert (group.size() > 0);

        double dmin = -1;
        int bestIndex = -1;
        for (int i = 0; i < group.size(); i++)
        {
            double mx = -1;
            for (int j = 0; j < group.size(); j++)
            {
                double d = wordGraph.distance(group.get(i), group.get(j));
                if (mx == -1 || mx < d)
                    mx = d;
            }

            if (dmin == -1 || dmin > mx)
            {
                dmin = mx;
                bestIndex = i;
            }
        }

        return group.get(bestIndex);
    }

    private ClusterResult groupPoints(List<Word> means)
    {
        Map<Word, Integer> groups = new HashMap();
        List<Word> median = new ArrayList<Word>();
        for (int i = 0; i < means.size(); i++)
            median.add(means.get(i));

        for (int it = 0; it < 10; it++)
        {
            groups.clear();

            for (int i = 0; i < words.size(); i++)
            {
                double minDis = 123456789.0;
                int bestIndex = -1;

                for (int j = 0; j < means.size(); j++)
                {
                    double d = wordGraph.distance(words.get(i), means.get(j));
                    if (minDis > d)
                    {
                        minDis = d;
                        bestIndex = j;
                    }
                }

                assert (bestIndex != -1);
                assert (minDis < 1234567.0);
                groups.put(words.get(i), bestIndex);
            }

            boolean progress = false;
            for (int i = 0; i < median.size(); i++)
            {
                Word newMedian = computeMedian(getGroup(groups, i));
                if (!median.get(i).equals(newMedian))
                    progress = true;
                median.set(i, newMedian);
            }

            if (!progress)
                break;
        }

        return new ClusterResult(words, similarities, groups, wordGraph);
    }

    private List<Word> getGroup(Map<Word, Integer> groups, int index)
    {
        List<Word> res = new ArrayList<Word>();
        for (Word w : groups.keySet())
            if (groups.get(w) == index)
                res.add(w);
        return res;
    }

    private void updateClusters(ClusterResult ci)
    {
        //reassign clusters
        boolean progress = true;
        for (int t = 0; t < 30 && progress; t++)
        {
            progress = false;
            for (int i = 0; i < words.size(); i++)
            {
                Word v = words.get(i);
                double oldModularity = ci.getModularity();

                //try to find the best new cluster
                int bestCluster = ci.getCluster(v);
                double bestModularity = oldModularity;
                for (int j = 0; j < ci.getClusterCount(); j++)
                {
                    if (j == ci.getCluster(v))
                        continue;

                    ci.moveVertex(v, j);
                    double newModularity = ci.getModularity();

                    if (newModularity > bestModularity)
                    {
                        bestModularity = newModularity;
                        bestCluster = j;
                    }
                }

                if (bestCluster != ci.getCluster(v))
                {
                    ci.moveVertex(v, bestCluster);
                }

                if (bestModularity > oldModularity)
                    progress = true;
            }
        }
    }
}
