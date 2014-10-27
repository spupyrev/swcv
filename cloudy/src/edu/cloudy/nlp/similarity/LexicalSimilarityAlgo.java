package edu.cloudy.nlp.similarity;

import edu.cloudy.nlp.SWCDocument;
import edu.cloudy.nlp.Word;
import edu.cloudy.nlp.WordPair;
import edu.cmu.lti.lexical_db.ILexicalDatabase;
import edu.cmu.lti.lexical_db.NictWordNet;
import edu.cmu.lti.ws4j.RelatednessCalculator;
import edu.cmu.lti.ws4j.impl.Lin;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Lin's similarity algorithm
 * see D. Lin, "An information-theoretic definition of similarity"
 */
public class LexicalSimilarityAlgo extends BaseSimilarityAlgo
{
    private Map<WordPair, Double> similarity;
    private static ILexicalDatabase db = new NictWordNet();
    private static RelatednessCalculator rc = new Lin(db);

    @Override
    protected void run(SWCDocument wordifier)
    {
        List<Word> words = wordifier.getWords();
        similarity = new HashMap<WordPair, Double>();
        
        for (int i = 0; i < words.size(); i++)
            for (int j = i + 1; j < words.size(); j++)
            {
                Word x = words.get(i);
                Word y = words.get(j);

                WordPair xyPair = new WordPair(x, y);

                double sim = rc.calcRelatednessOfWords(x.word, y.word);
                sim = Math.max(0, sim);
                similarity.put(xyPair, sim);

            }
    }

    @Override
    protected Map<WordPair, Double> getSimilarity()
    {
        return similarity;

    }

}
