package edu.cloudy.clustering;

import edu.cloudy.layout.WordGraph;
import edu.cloudy.nlp.Word;
import edu.cloudy.nlp.ItemPair;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author spupyrev
 * Nov 8, 2014
 */
public class ClusterResult
{
    private Word[] words;
    private Map<Word, Integer> wordIndex;

    private double[][] similarity;
    private WordGraph wordGraph;

    //index of node cluster, or -1 if the node is not present in G
    private int[] cluster;
    //private Map<Word, Integer> cluster;
    private int clusterCount;
    private double modularity;

    //twice the sum of all edge weights
    private double m2;

    //sum of weights of all edges incident to a node in the cluster; inner edges are counted twice
    private double[] sumTot;

    //sum of weights of all edges inside the cluster
    private double[] sumIn;

    public ClusterResult(List<Word> words, Map<ItemPair<Word>, Double> similarities, Map<Word, Integer> cluster, WordGraph wgInfo)
    {
        this.words = words.toArray(new Word[words.size()]);
        this.wordGraph = wgInfo;

        initialize(words, similarities, cluster, wgInfo);
    }

    public int getClusterCount()
    {
        return clusterCount;
    }

    public int getCluster(Word w)
    {
        int index = wordIndex.get(w);
        return cluster[index];
    }

    public double getModularity()
    {
        return modularity;
    }

    /**
     * A real number from [0..1] indicating how good is the clustering
     */
    public double quality()
    {
        return modularity;
    }

    /**
     * Recomputes the number of clusters
     */
    public void compress()
    {
        Map<Integer, List<Word>> groups = new HashMap();
        for (int i = 0; i < words.length; i++)
        {
            groups.putIfAbsent(cluster[i], new ArrayList());
            groups.get(cluster[i]).add(words[i]);
        }

        //init count
        clusterCount = groups.keySet().size();

        //recompute indices
        int clusterIndex = 0;
        for (int key : groups.keySet())
        {
            for (Word w : groups.get(key))
            {
                cluster[wordIndex.get(w)] = clusterIndex;
            }
            clusterIndex++;
        }
    }

    private void initialize(List<Word> words, Map<ItemPair<Word>, Double> similarityMap, Map<Word, Integer> clusterMap, WordGraph wgInfo)
    {
        //init clusters
        wordIndex = new HashMap();
        cluster = new int[words.size()];
        Map<Integer, List<Word>> groups = new HashMap();
        for (int i = 0; i < words.size(); i++)
        {
            Word w = words.get(i);

            cluster[i] = clusterMap.get(w);
            wordIndex.put(w, i);
            groups.putIfAbsent(cluster[i], new ArrayList());
            groups.get(cluster[i]).add(w);
        }

        //caching similarities
        similarity = new double[words.size()][words.size()];
        for (int i = 0; i < words.size(); i++)
            for (int j = 0; j < words.size(); j++)
            {
                ItemPair<Word> wp = new ItemPair<Word>(words.get(i), words.get(j));
                similarity[i][j] = similarityMap.getOrDefault(wp, 0.0);
            }

        //init count
        clusterCount = groups.keySet().size();

        //modularity
        modularity = getModularitySlow();

        //cache
        m2 = 0;
        for (Word w : words)
            m2 += wgInfo.weightedDegree(w);

        sumTot = new double[clusterCount];
        Arrays.fill(sumTot, 0);
        for (int i : groups.keySet())
            for (Word w : groups.get(i))
                sumTot[i] += wgInfo.weightedDegree(w);

        sumIn = new double[clusterCount];
        Arrays.fill(sumIn, 0);
        for (int i = 0; i < words.size(); i++)
            for (int j = i + 1; j < words.size(); j++)
            {
                if (cluster[i] == cluster[j])
                    sumIn[cluster[i]] += similarity[i][j];
            }

        //checkConsistency();
    }

    public void moveVertex(Word w, int newCluster)
    {
        int wIndex = wordIndex.get(w);

        int curCluster = cluster[wIndex];
        assert (curCluster != -1 && curCluster != newCluster);

        double wd = wordGraph.weightedDegree(w);

        //move node from the current to an isolated community
        double ki = wd;
        double ki_in = computeKiIn(wIndex, curCluster);
        double sum_in = sumIn[curCluster] - ki_in;
        double sum_tot = sumTot[curCluster] - wd;

        double delta = ((sum_in + 2 * ki_in) / m2 - sqr2((sum_tot + ki) / m2)) - (sum_in / m2 - sqr2(sum_tot / m2) - sqr2(ki / m2));

        //updating cache
        sumTot[curCluster] -= wd;
        sumIn[curCluster] -= ki_in;

        //move node from the isolated to the new community
        ki = wd;
        ki_in = computeKiIn(wIndex, newCluster);
        sum_in = sumIn[newCluster];
        sum_tot = sumTot[newCluster];

        double delta2 = ((sum_in + 2 * ki_in) / m2 - sqr2((sum_tot + ki) / m2)) - (sum_in / m2 - sqr2(sum_tot / m2) - sqr2(ki / m2));

        //updating cache
        sumTot[newCluster] += wd;
        sumIn[newCluster] += ki_in;

        //actual move
        cluster[wIndex] = newCluster;
        modularity += delta2 - delta;

        //checkConsistency();
    }

    private double sqr2(double d)
    {
        return d * d;
    }

    @SuppressWarnings("unused")
    private void checkConsistency()
    {
        assert (Math.abs(modularity - getModularitySlow()) < 1e-4);

        double[] sumTot2 = new double[clusterCount];
        double[] sumIn2 = new double[clusterCount];

        for (int i = 0; i < words.length; i++)
        {
            sumTot2[cluster[i]] += wordGraph.weightedDegree(words[i]);
        }

        for (int i = 0; i < words.length; i++)
            for (int j = i + 1; j < words.length; j++)
            {
                if (cluster[i] == cluster[j])
                    sumIn2[cluster[i]] += similarity[i][j];
            }

        for (int i = 0; i < clusterCount; i++)
        {
            assert (Math.abs(sumTot[i] - sumTot2[i]) < 1e-4);
            assert (Math.abs(sumIn[i] - sumIn2[i]) < 1e-4);
        }
    }

    private double computeKiIn(int wIndex, int clusterId)
    {
        Integer[] adj = wordGraph.nonZeroAdjacency(words[wIndex]);

        double ki_in = 0;
        for (int i = 0; i < adj.length; i++)
        {
            int adjIndex = adj[i];
            assert (wIndex != adjIndex);

            if (cluster[adjIndex] != clusterId)
                continue;

            ki_in += similarity[adjIndex][wIndex];
        }

        return ki_in;
    }

    private double getModularitySlow()
    {
        return ModularityCalculator.compute(words, similarity, cluster, wordGraph);
    }

}
