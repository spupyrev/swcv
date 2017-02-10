package edu.test;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import edu.cloudy.clustering.ClusterResult;
import edu.cloudy.clustering.GenericClusterAlgo;
import edu.cloudy.clustering.IClusterAlgo;
import edu.cloudy.clustering.KMeansPlusPlus;
import edu.cloudy.layout.WordGraph;
import edu.cloudy.nlp.ItemPair;
import edu.cloudy.nlp.Word;

/**
 * @author spupyrev
 * Nov 11, 2014
 */
public class ClusterTest
{
    @Test
    public void testKMeans1()
    {
        WordGraph wordGraph = createClique(1, 1);
        checkClustering(wordGraph, runKMeans(wordGraph, 2), 1);
    }

    @Test
    public void testKMeans2()
    {
        WordGraph wordGraph = createClique(5, 1);
        checkClustering(wordGraph, runKMeans(wordGraph, 2), 1);
    }

    @Test
    public void testKMeans3()
    {
        WordGraph wordGraph = createClique(5, 3);
        checkClustering(wordGraph, runKMeans(wordGraph, 3), 3);
    }

    @Test
    public void testGeneric1()
    {
        WordGraph wordGraph = createClique(5, 3);
        checkClustering(wordGraph, runGeneric(wordGraph), 3);
    }

    @Test
    public void testGeneric2()
    {
        WordGraph wordGraph = createClique(4, 4);
        checkClustering(wordGraph, runGeneric(wordGraph), 4);
    }

    @Test
    public void testGeneric3()
    {
        WordGraph wordGraph = createClique(3, 5);
        checkClustering(wordGraph, runGeneric(wordGraph), 5);
    }

    @Test(timeout = 1000)
    public void testStress1()
    {
        WordGraph wordGraph = LayoutTest.createRandomGraph(100);
        ClusterResult result = runGeneric(wordGraph);
        Assert.assertTrue(0.0 <= result.getModularity() && result.getModularity() <= 1.0);
    }

    @Test(timeout = 3000)
    public void testStress2()
    {
        WordGraph wordGraph = LayoutTest.createRandomGraph(200);
        ClusterResult result = runGeneric(wordGraph);
        Assert.assertTrue(0.0 <= result.getModularity() && result.getModularity() <= 1.0);
    }

    private ClusterResult runKMeans(WordGraph wordGraph, int K)
    {
        IClusterAlgo clusterAlgo = new KMeansPlusPlus(K);
        return clusterAlgo.run(wordGraph);
    }

    private ClusterResult runGeneric(WordGraph wordGraph)
    {
        IClusterAlgo clusterAlgo = new GenericClusterAlgo(-1);
        return clusterAlgo.run(wordGraph);
    }

    private WordGraph createClique(int wordsPerClique, int cliqueCount)
    {
        WordGraph wordGraph = LayoutTest.createRandomGraph(wordsPerClique * cliqueCount);
        List<Word> words = wordGraph.getWords();

        for (int i = 0; i < words.size(); i++)
            for (int j = 0; j < words.size(); j++)
            {
                ItemPair<Word> wp = new ItemPair<Word>(words.get(i), words.get(j));

                if (i / wordsPerClique == j / wordsPerClique)
                    wordGraph.getSimilarity().put(wp, 1.0);
                else
                    wordGraph.getSimilarity().put(wp, 0.0);
            }

        return wordGraph;
    }

    private void checkClustering(WordGraph wordGraph, ClusterResult result, int expectedClusterCount)
    {
        Assert.assertEquals(result.getClusterCount(), expectedClusterCount);
        Assert.assertTrue(0.0 <= result.getModularity() && result.getModularity() <= 1.0);

        for (ItemPair<Word> wp : wordGraph.getSimilarity().keySet())
        {
            Word w1 = wp.getFirst();
            Word w2 = wp.getSecond();
            double sim = wordGraph.getSimilarity().get(wp);

            if (sim == 1.0)
                Assert.assertEquals(result.getCluster(w1), result.getCluster(w2));
            else
                Assert.assertTrue(result.getCluster(w1) != result.getCluster(w2));
        }
    }

}
