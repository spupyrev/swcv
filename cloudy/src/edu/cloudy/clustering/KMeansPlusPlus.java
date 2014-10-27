package edu.cloudy.clustering;

import edu.cloudy.layout.LayoutUtils;
import edu.cloudy.nlp.Word;
import edu.cloudy.nlp.WordPair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

/**
 * @author spupyrev
 * Nov 28, 2013
 */
public class KMeansPlusPlus implements IClusterAlgo
{
    private Random rnd = new Random(123);

    private int K;
    private Map<Word, Integer> clusters;

    private List<Word> words;
    private Map<WordPair, Double> similarities;
    private Map<WordPair, Double> distances;

    private ShortestPathCalculator spCalculator;

    public KMeansPlusPlus(int K)
    {
        this.K = K;
    }

    @Override
    public void run(List<Word> words, Map<WordPair, Double> similarity)
    {
        if (words.isEmpty())
            return;

        this.words = words;
        this.similarities = similarity;
        this.distances = extractDistances(similarity);

        spCalculator = new ShortestPathCalculator(words, distances);
        clusters = runInternal();
    }

    private Map<WordPair, Double> extractDistances(Map<WordPair, Double> similarity)
    {
        Map<WordPair, Double> res = new HashMap<WordPair, Double>();
        for (WordPair wp : similarity.keySet())
        {
            double sim = similarity.get(wp);
            double dist = LayoutUtils.idealDistanceConverter(sim);

            res.put(wp, dist);
        }

        return res;
    }

    private Map<Word, Integer> runInternal()
    {
        assert (K >= 1);

        Map<Word, Integer> res = null;
        double bestValue = -1;

        for (int attempt = 0; attempt < 10; attempt++)
        {
            List<Word> centers = chooseCenters(K);

            Map<Word, Integer> groups = groupPoints(centers);
            updateGroups(groups);

            double value = ClusterQuality.compute(words, similarities, groups);
            //System.out.println("cluster quality: " + value);
            if (bestValue == -1 || bestValue < value)
            {
                bestValue = value;
                res = groups;
            }
        }

        assert (res != null);
        //for (int i = 0; i < K; i++)
        //    System.out.println(i + ": " + getGroup(res, i).size());
        //System.out.println("final cluster quality: " + bestValue);
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
                double d = spCalculator.compute(words.get(i), means.get(j));
                minD = Math.min(minD, d);
            }

            minDist.add(minD * minD);
        }

        int p = chooseRandomWithProbability(minDist);
        if (minDist.get(p) < 1e-6)
            return null;

        return words.get(p);
    }

    private int chooseRandomWithProbability(List<Double> prob)
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
        double dmin = -1;
        int bestIndex = -1;
        for (int i = 0; i < group.size(); i++)
        {
            double mx = -1;
            for (int j = 0; j < group.size(); j++)
            {
                double d = spCalculator.compute(group.get(i), group.get(j));
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

    private Map<Word, Integer> groupPoints(List<Word> means)
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
                    double d = spCalculator.compute(words.get(i), means.get(j));
                    if (minDis > d)
                    {
                        minDis = d;
                        bestIndex = j;
                    }
                }

                if (bestIndex == -1)
                    bestIndex = 0;

                assert (bestIndex != -1);
                assert (minDis < 1234567.0);
                groups.put(words.get(i), bestIndex);
            }

            boolean progress = false;
            for (int i = 0; i < median.size(); i++)
            {
                Word newMedian = computeMedian(getGroup(groups, i));
                if (median.get(i).equals(newMedian))
                    progress = true;
                median.set(i, newMedian);
            }

            if (!progress)
                break;
        }

        return groups;
    }

    public static List<Word> getGroup(Map<Word, Integer> groups, int index)
    {
        List<Word> res = new ArrayList<Word>();
        for (Word w : groups.keySet())
            if (groups.get(w) == index)
                res.add(w);
        return res;
    }

    private void updateGroups(Map<Word, Integer> groups)
    {
        Set<Integer> clusters = new HashSet(groups.values());
        int[] clusterSize = new int[clusters.size()];
        for (Word w : groups.keySet())
            clusterSize[groups.get(w)]++;

        //reassign clusters
        boolean progress = true;
        for (int t = 0; t < 30 && progress; t++)
        {
            progress = false;
            for (int i = 0; i < words.size(); i++)
            {
                Word v = words.get(i);
                //weight in i-th cluster
                double[] wSum = new double[clusters.size()];
                for (int j = 0; j < words.size(); j++)
                    if (i != j)
                    {
                        Word u = words.get(j);
                        WordPair wp = new WordPair(v, u);
                        if (!similarities.containsKey(wp))
                            continue;

                        wSum[groups.get(u)] += similarities.get(wp);
                    }

                //find best cluster
                int bestNewCluster = groups.get(v);
                for (int j = 0; j < clusters.size(); j++)
                {
                    if (j == groups.get(v))
                        continue;
                    if (clusterSize[j] == 0)
                        continue;

                    //double wj = 1.0 / Math.sqrt(clusterSize[j]);
                    //double wb = 1.0 / Math.sqrt(clusterSize[bestNewCluster]);
                    double wj = 1.0 / clusterSize[j];
                    double wb = 1.0 / clusterSize[bestNewCluster];

                    if (wSum[j] * wj > wSum[bestNewCluster] * wb)
                        bestNewCluster = j;
                }

                if (bestNewCluster != groups.get(v))
                {
                    clusterSize[groups.get(v)]--;
                    clusterSize[bestNewCluster]++;

                    groups.put(v, bestNewCluster);
                    progress = true;
                }
            }
        }
    }

    @Override
    public int getCluster(Word word)
    {
        return clusters.get(word);
    }

    @Override
    public int getClusterNumber()
    {
        return K;
    }
}
