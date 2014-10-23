package edu.cloudy.nlp.ranking;

import edu.cloudy.nlp.SWCDocument;
import edu.cloudy.nlp.Word;
import edu.cloudy.utils.CommonUtils;

import java.io.ObjectInputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author spupyrev
 * Aug 18, 2013
 */
public class TFIDFRankingAlgo implements RankingAlgo
{
    public TFIDFRankingAlgo()
    {
    }

    @Override
    public void buildWeights(SWCDocument document)
    {
        Map<String, Double> idfMap = computeIDF();

        List<Word> words = document.getWords();

        for (Word w : words)
        {
            double df = w.getSentences().size();
            double idf;
            if (idfMap.containsKey(w.stem))
            {
                idf = idfMap.get(w.stem);
            }
            else
            {
                idf = 7;
            }
            w.weight = df * idf + 1;
        }
        double maxCount = -1;
        for (Word w : words)
        {
            maxCount = Math.max(w.weight, maxCount);
        }
        for (Word w : words)
        {
            w.weight = w.weight / maxCount;
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Double> computeIDF()
    {
        Map<String, Double> cnt = new HashMap<String, Double>();
        try
        {
            ObjectInputStream ois = new ObjectInputStream(CommonUtils.getResourceAsStream("corpus/brown.bin"));
            cnt = (HashMap<String, Double>)ois.readObject();
            ois.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            throw new RuntimeException(e);
        }

        Map<String, Double> idf = new HashMap<String, Double>();
        for (String w : cnt.keySet())
        {
            double id = cnt.get(w);
            idf.put(w, Math.log(500 / id));
        }
        return idf;
    }
}
