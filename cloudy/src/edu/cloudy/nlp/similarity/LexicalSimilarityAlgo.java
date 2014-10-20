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

public class LexicalSimilarityAlgo extends BaseSimilarityAlgo
{
    private Map<WordPair, Double> similarity;
    private SWCDocument wordifier;
    private static ILexicalDatabase db = new NictWordNet();
    private static RelatednessCalculator rc = new Lin(db);
    /*private static RelatednessCalculator[] rcs = {
            new HirstStOnge(db),
            new LeacockChodorow(db),
            new Lesk(db),
            new WuPalmer(db),
            new Resnik(db),
            new JiangConrath(db),
            new Lin(db),
            new Path(db) };*/

    @Override
    protected void initialize(SWCDocument wordifier)
    {
        this.wordifier = wordifier;
        this.similarity = null;
    }

    @Override
    protected void run()
    {
        List<Word> words = wordifier.getWords();
        similarity = new HashMap<WordPair, Double>();
        for (Word x : words)
        {
            for (Word y : words)
            {
                if (x.stem.equals(y.stem))
                    continue;
                WordPair xyPair = new WordPair(x, y);

                double xySimilarity = rc.calcRelatednessOfWords(x.word, y.word);
                if (xySimilarity <= 0)
                    xySimilarity = 0.0;
                similarity.put(xyPair, xySimilarity);

            }
        }
    }

    @Override
    protected Map<WordPair, Double> getSimilarity()
    {
        return this.similarity;

    }

}
