package edu.cloudy.nlp.ranking;


import java.util.ArrayList;
import java.util.List;

/**
 * A quick little test of LexRank.
 * I manually constructed a similarity matrix from the graphs in the LexRank
 * paper, and just used dummy items to test out how well it works.
 * We end up with slightly different numbers than given in the paper, but the
 * ranking is the same or almost the same, so I think it works well enough.
 */
public class LexRankTest {
    public static void main(String[] args) {
        /*double[][] similarity =
            {{1.00, 0.45, 0.02, 0.17, 0.03, 0.22, 0.03, 0.28, 0.06, 0.06, 0.00},
             {0.45, 1.00, 0.16, 0.27, 0.03, 0.19, 0.03, 0.21, 0.03, 0.15, 0.00},
             {0.02, 0.16, 1.00, 0.03, 0.00, 0.01, 0.03, 0.04, 0.00, 0.01, 0.00},
             {0.17, 0.27, 0.03, 1.00, 0.01, 0.16, 0.28, 0.17, 0.00, 0.09, 0.01},
             {0.03, 0.03, 0.00, 0.01, 1.00, 0.29, 0.05, 0.15, 0.20, 0.04, 0.18},
             {0.22, 0.19, 0.01, 0.16, 0.29, 1.00, 0.05, 0.29, 0.04, 0.20, 0.03},
             {0.03, 0.03, 0.03, 0.28, 0.05, 0.05, 1.00, 0.06, 0.00, 0.00, 0.01},
             {0.28, 0.21, 0.04, 0.17, 0.15, 0.29, 0.06, 1.00, 0.25, 0.20, 0.17},
             {0.06, 0.03, 0.00, 0.00, 0.20, 0.04, 0.00, 0.25, 1.00, 0.26, 0.38},
             {0.06, 0.15, 0.01, 0.09, 0.04, 0.20, 0.00, 0.20, 0.26, 1.00, 0.12},
             {0.00, 0.00, 0.00, 0.01, 0.18, 0.03, 0.01, 0.17, 0.38, 0.12, 1.00},
            };*/
    	
        double[][] similarity =
        	{
        {1.00, 0.00, 1.00, 0.00, 1.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00},
        {0.00, 1.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00},
        {1.00, 0.00, 1.00, 0.00, 1.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00},
        {0.00, 0.00, 0.00, 1.00, 0.00, 0.50, 0.50, 0.50, 1.00, 0.50, 0.50, 0.50},
        {1.00, 0.00, 1.00, 0.00, 1.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00},
        {0.00, 0.00, 0.00, 0.50, 0.00, 1.00, 0.00, 0.00, 0.50, 0.00, 1.00, 0.00},
        {0.00, 0.00, 0.00, 0.50, 0.00, 0.00, 1.00, 1.00, 0.50, 1.00, 0.00, 1.00},
        {0.00, 0.00, 0.00, 0.50, 0.00, 0.00, 1.00, 1.00, 0.50, 1.00, 0.00, 1.00},
        {0.00, 0.00, 0.00, 1.00, 0.00, 0.50, 0.50, 0.50, 1.00, 0.50, 0.50, 0.50},
        {0.00, 0.00, 0.00, 0.50, 0.00, 0.00, 1.00, 1.00, 0.50, 1.00, 0.00, 1.00},
        {0.00, 0.00, 0.00, 0.50, 0.00, 1.00, 0.00, 0.00, 0.50, 0.00, 1.00, 0.00},
        {0.00, 0.00, 0.00, 0.50, 0.00, 0.00, 1.00, 1.00, 0.50, 1.00, 0.00, 1.00},
        	};
        
        for (int i = 0; i < similarity.length; ++i) 
        	for (int j = 0; j < similarity[i].length; j++)
        		if (i != j)similarity[i][j] /= 1.0;
        
        List<DummyItem> items = new ArrayList<DummyItem>();
        for (int i = 0; i < similarity.length; ++i) {
            items.add(new DummyItem(i, similarity));
        }
        LexRankResults<DummyItem> results = LexRanker.rank(items, 0.2, false);
        String[] names = {"d1s1", "d2s1", "d2s2", "d2s3", "d3s1", "d3s2",
                          "d3s3", "d4s1", "d5s1", "d5s2", "d5s3", "d5s4"};
        double max = results.scores.get(results.rankedResults.get(0));
        for (int i = 0; i < similarity.length; ++i) {
            System.out.println(names[i] + ": " + (results.scores.get(items.get(i)) / max));
        }
    }
    
    static class DummyItem implements Similar<DummyItem> {
        int id;
        double[][] similarityMatrix;
        public DummyItem(int id, double[][] similarityMatrix) {
            this.id = id;
            this.similarityMatrix = similarityMatrix;
        }
        public double similarity(DummyItem other) {
            return similarityMatrix[id][other.id];
        }
    }
    
}
