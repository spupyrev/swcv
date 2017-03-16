package edu.test.misc;

import edu.cloudy.layout.ContextPreservingAlgo;
import edu.cloudy.layout.LayoutResult;
import edu.cloudy.layout.WordGraph;
import edu.cloudy.nlp.SWCDocument;
import edu.cloudy.nlp.Word;
import edu.cloudy.nlp.ItemPair;
import edu.cloudy.nlp.ranking.TFRankingAlgo;
import edu.cloudy.nlp.similarity.RandomSimilarityAlgo;
import edu.cloudy.nlp.similarity.SimilarityAlgo;
import edu.cloudy.utils.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@SuppressWarnings("all")
public class DegreeWeightAnalyzer
{
    /**
     * @param args
     */
    public static void main(String[] args)
    {
        Logger.doLogging = false;

        analyze();
    }

    private static void analyze()
    {
        List<SWCDocument> documents = ALENEXPaperEvalulator.readDocuments(ALENEXPaperEvalulator.FILES_WIKI);

        List<Double> weights = new ArrayList<Double>();
        for (SWCDocument doc : documents)
        {
            doc.weightFilter(150, new TFRankingAlgo());
            //SimilarityAlgo coOccurenceAlgo = new CosineCoOccurenceAlgo();
            SimilarityAlgo coOccurenceAlgo = new RandomSimilarityAlgo();
            Map<ItemPair<Word>, Double> similarity = coOccurenceAlgo.computeSimilarity(doc);
            filter(doc.getWords(), similarity);

            //weights.addAll(similarity.values());
            weights.addAll(realizedWeights(doc.getWords(), similarity));
        }

        analyzeWeightDistribution(weights);

        Collections.sort(weights);
        double totalWeight = 0;
        for (double w : weights)
            totalWeight += w;

        System.out.println("total weight: " + totalWeight);

        int numHalf = 0;
        double sumHalf = 0;
        for (int i = weights.size() - 1; i >= 0; i--)
        {
            numHalf++;
            sumHalf += weights.get(i);
            if (sumHalf >= totalWeight * 0.5)
                break;
        }

        System.out.println("perc half: " + (double)numHalf / weights.size());
    }

    private static void filter(List<Word> words, Map<ItemPair<Word>, Double> similarity)
    {
        for (Word w : words)
        {
            ItemPair<Word> wp = new ItemPair<Word>(w, w);
            if (similarity.containsKey(wp))
                similarity.remove(wp);
        }
    }

    private static List<Double> realizedWeights(List<Word> words, Map<ItemPair<Word>, Double> similarity)
    {
        LayoutResult algo = new ContextPreservingAlgo().layout(new WordGraph(words, similarity));

        //List<ItemPair<Word>> wpl = new AdjacenciesMetric().realizedPairs(words, similarity, algo);
        List<ItemPair<Word>> wpl = null;//new AdjacenciesMetric().realizedPairs(words, similarity, algo);
        List<Double> res = new ArrayList<Double>();
        for (ItemPair<Word> wp : wpl)
            res.add(similarity.get(wp));

        return res;
    }

    private static void analyzeWeightDistribution(List<Double> weights)
    {
        double l = 0, u = 1;
        double step = 0.005;
        double now = l;
        while (now <= u)
        {
            int cnt = numberOfPairs(weights, now, now + step);
            System.out.printf("%.3f -- %d\n", now + step / 2, cnt);

            now += step;
        }

    }

    private static int numberOfPairs(List<Double> weights, double l, double r)
    {
        int res = 0;
        for (double w : weights)
            if (l <= w && w <= r)
                res++;

        return res;
    }
}
