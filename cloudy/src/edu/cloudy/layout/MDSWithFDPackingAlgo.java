package edu.cloudy.layout;

import edu.cloudy.nlp.Word;
import edu.cloudy.nlp.WordPair;
import edu.cloudy.utils.SWCRectangle;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * June 21, 2014
 * 
 * spupyrev
 */
public class MDSWithFDPackingAlgo extends BaseLayoutAlgo
{
    private Map<Word, SWCRectangle> wordPositions = new HashMap<Word, SWCRectangle>();

    public MDSWithFDPackingAlgo(List<Word> words, Map<WordPair, Double> similarity)
    {
        super(words, similarity);
    }

    @Override
    public SWCRectangle getWordPosition(Word w)
    {
        return wordPositions.get(w);
    }

    @Override
    public void run()
    {
        MDSAlgo algo = new MDSAlgo(words, similarity);
        algo.run();
        
        wordPositions = new HashMap<Word, SWCRectangle>();
        for (Word w : words)
        {
            SWCRectangle rect = algo.getWordPosition(w);
            wordPositions.put(w, rect);
        }

        //new ForceDirectedOverlapRemoval<SWCRectangle>().run(words, wordPositions);
        //new ForceDirectedUniformity<SWCRectangle>().run(words, wordPositions);
    }

}