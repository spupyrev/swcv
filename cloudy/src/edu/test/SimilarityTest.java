package edu.test;

import edu.cloudy.main.ALENEXPaperEvalulator;
import edu.cloudy.nlp.WCVDocument;
import edu.cloudy.nlp.Word;
import edu.cloudy.nlp.WordPair;
import edu.cloudy.nlp.ranking.TFIDFRankingAlgo;
import edu.cloudy.nlp.similarity.CosineCoOccurenceAlgo;
import edu.cloudy.nlp.similarity.EuclideanAlgo;
import edu.cloudy.nlp.similarity.JaccardCoOccurenceAlgo;
import edu.cloudy.nlp.similarity.LexicalSimilarityAlgo;
import edu.cloudy.nlp.similarity.SimilarityAlgo;
import edu.cloudy.utils.WikipediaXMLReader;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class SimilarityTest
{

    public static void main(String[] args)
    {

        WCVDocument document = readDoc();

        // 2. build similarities, words etc
        List<Word> words = new ArrayList<Word>();
        Map<WordPair, Double> similarity = new HashMap<WordPair, Double>();
        extractSimilarities(document, words, similarity);
    }

    public static WCVDocument readDoc()
    {
        List<WCVDocument> alldocs = ALENEXPaperEvalulator.readDocuments(ALENEXPaperEvalulator.FILES_WIKI);

        WikipediaXMLReader xmlReader = new WikipediaXMLReader("data/turing");
        xmlReader.read();
        Iterator<String> texts = xmlReader.getTexts();

        WCVDocument doc = null;
        while (texts.hasNext())
        {
            doc = new WCVDocument(texts.next());
            doc.parse();

            alldocs.add(doc);
        }
        System.out.println("#words: " + doc.getWords().size());
        doc.weightFilter(30, new TFIDFRankingAlgo());
        return doc;
    }

    private static void extractSimilarities(WCVDocument wordifier, List<Word> words, final Map<WordPair, Double> similarity)
    {
        SimilarityAlgo[] coOccurenceAlgoArray = {
                new LexicalSimilarityAlgo(),
                new CosineCoOccurenceAlgo(),
                new JaccardCoOccurenceAlgo(),
                new EuclideanAlgo() };
        //SimilarityAlgo CosineCoOccurenceAlgo2 = new LexicalSimilarityAlgo();

        //SimilarityAlgo coOccurenceAlgo3 = new JaccardCoOccurenceAlgo();
        //SimilarityAlgo coOccurenceAlgo4 = new RandomSimilarityAlgo();
        for (SimilarityAlgo coOccurenceAlgo : coOccurenceAlgoArray)
        {
            coOccurenceAlgo.initialize(wordifier);
            coOccurenceAlgo.run();
            Map<WordPair, Double> sim = coOccurenceAlgo.getSimilarity();

            for (Word w : wordifier.getWords())
                words.add(w);

            List<WordPair> topPairs = new ArrayList<WordPair>();
            for (WordPair wp : sim.keySet())
            {
                similarity.put(wp, sim.get(wp));
                topPairs.add(wp);
            }

            Collections.sort(topPairs, new Comparator<WordPair>()
            {
                @Override
                public int compare(WordPair o1, WordPair o2)
                {
                    return similarity.get(o2).compareTo(similarity.get(o1));
                }
            });

            System.out.println("top pairs of " + coOccurenceAlgo.getClass().getName() + ":");
            for (int i = 0; i < topPairs.size(); i++)
            {
                WordPair wp = topPairs.get(i);
                System.out.println(wp.getFirst().word + " " + wp.getSecond().word + "  " + similarity.get(wp));
            }
            System.out.println("\n\n\n\n");
        }
    }

}
