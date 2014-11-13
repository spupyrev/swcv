package edu.cloudy.colors;

import edu.cloudy.clustering.ClusterResult;
import edu.cloudy.clustering.GenericClusterAlgo;
import edu.cloudy.clustering.IClusterAlgo;
import edu.cloudy.layout.WordGraph;
import edu.cloudy.nlp.Word;
import edu.cloudy.utils.TimeMeasurer;

import java.awt.Color;
import java.util.List;

/**
 * @author spupyrev
 * Nov 28, 2013
 */
public class ClusterColorScheme extends ColorScheme
{
    private Color[] colorSequence;
    private int K;

    private int[] clusterIndex;
    private ClusterResult clustering = null;

    public ClusterColorScheme(String name, String cmdIndex, int K, Color[] colorSequence)
    {
        super(name, cmdIndex);

        this.K = K;
        this.colorSequence = colorSequence;
    }

    @Override
    public Color getColor(Word word)
    {
        if (clustering == null)
            throw new RuntimeException(ClusterColorScheme.class.getName() + " is not initialized");
        
        int c = clustering.getCluster(word);
        int res = clusterIndex[c] % colorSequence.length;
        return colorSequence[res];
    }

    @Override
    public void initialize(WordGraph wordGraph)
    {
        IClusterAlgo clusterAlgo = new GenericClusterAlgo(K);
        clustering = TimeMeasurer.execute("clustering", () -> clusterAlgo.run(wordGraph));
        
        sortClusters(wordGraph.getWords());
    }
    
    private void sortClusters(List<Word> words)
    {
        int K = clustering.getClusterCount();
        int[] cnt = new int[K];
        for (Word w : words)
            cnt[clustering.getCluster(w)]++;

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
}
