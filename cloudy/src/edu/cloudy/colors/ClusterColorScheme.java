package edu.cloudy.colors;

import edu.cloudy.clustering.IClusterAlgo;
import edu.cloudy.clustering.KMeansPlusPlus;
import edu.cloudy.nlp.Word;
import edu.cloudy.nlp.WordPair;

import java.awt.Color;
import java.util.List;
import java.util.Map;

/**
 * @author spupyrev
 * Nov 28, 2013
 */
public class ClusterColorScheme extends ColorScheme
{
    private Color[] colorSequence;
    private int K;

    private int[] clusterIndex;
    private IClusterAlgo clusterAlgo = null;

    public ClusterColorScheme(String name, int K, Color[] colorSequence)
    {
        super(name);

        this.K = K;
        this.colorSequence = colorSequence;
    }

    @Override
    public Color getColor(Word word)
    {
        if (clusterAlgo == null)
            throw new RuntimeException(ClusterColorScheme.class.getName() + " is not initialized");
        
        int c = clusterAlgo.getCluster(word);
        int res = clusterIndex[c] % colorSequence.length;
        return colorSequence[res];
    }

    @Override
    public void initialize(List<Word> words, Map<WordPair, Double> similarity)
    {
        clusterAlgo = new KMeansPlusPlus(guessNumberOfClusters(words.size()));
        clusterAlgo.run(words, similarity);
        sortClusters(words);
    }
    
    private void sortClusters(List<Word> words)
    {
        int K = clusterAlgo.getClusterNumber();
        int[] cnt = new int[K];
        for (Word w : words)
            cnt[clusterAlgo.getCluster(w)]++;

        clusterIndex = new int[K];
        for (int i = 0; i < K; i++)
            clusterIndex[i] = i;

        for (int i = 0; i < K; i++)
            for (int j = i + 1; j < K; j++)
                if (cnt[clusterIndex[i]] < cnt[clusterIndex[j]])
                {
                    int tmp = clusterIndex[i];
                    clusterIndex[i] = clusterIndex[j];
                    clusterIndex[j] = tmp;
                }

        int[] clusterIndexRev = new int[K];
        for (int i = 0; i < K; i++)
            clusterIndexRev[clusterIndex[i]] = i;

        clusterIndex = clusterIndexRev;
    }

    private int guessNumberOfClusters(int n)
    {
        if (K != -1)
            return K;
        return Math.max((int)Math.sqrt((double)n / 2), 1);
    }

}
