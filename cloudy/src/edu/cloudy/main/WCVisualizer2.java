package edu.cloudy.main;

import edu.cloudy.layout.LayoutAlgo;
import edu.cloudy.layout.PlacerType;
import edu.cloudy.layout.StarForestAlgo2;
import edu.cloudy.nlp.WCVDocument;
import edu.cloudy.nlp.Word;
import edu.cloudy.nlp.WordPair;
import edu.cloudy.nlp.ranking.TFRankingAlgo;
import edu.cloudy.nlp.similarity.RandomSimilarityAlgo;
import edu.cloudy.nlp.similarity.SimilarityAlgo;
import edu.cloudy.utils.BoundingBoxGenerator;
import edu.cloudy.utils.Logger;
import edu.cloudy.utils.WikipediaXMLReader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Apr 30, 2013 
 * create and visualize a wordcloud for a document
 */
public class WCVisualizer2
{

    boolean suppressGraphics = false;
    public static final boolean animated = false;

    public static void main(String[] args)
    {
        Logger.doLogging = false;
        //		String[] data_locations = {"data/test_wiki", 
        //				"data/test_wiki1", "data/test_wiki3", "data/wiki_00", "data/wiki_01", "data/wiki_02",
        //				"data/wiki_03", "data/wiki_05", "data/turing", "data/alenex_papers",
        //				"data/soda_paper.xml"};

        List<LayoutAlgo> algorithms;

        int numWords;
        if (args.length > 0)
        {
            numWords = Integer.parseInt(args[0]);
        }
        else
            numWords = 26;

        //		for(int i=0; i< data_locations.length; i++){
        //			String data_location = data_locations[i];
        String data_location = "data/test_wiki";

        List<WCVDocument> documents = getDocumentsFromFile(data_location);

        for (int i = documents.size() - 1; i < documents.size(); i++)
        {
            //		for (int i = 0; i < documents.size(); i++) {

            WCVDocument doc = documents.get(i);
            doc.weightFilter(numWords, new TFRankingAlgo());

            algorithms = new ArrayList<LayoutAlgo>(13);
            //						algorithms.add(new StarForestAlgoNew(PlacerType.SINGLE_SPIRAL, animated));
            //						algorithms.add(new StarForestAlgoNew(PlacerType.RECURSIVE_SPIRAL, animated));
            //						algorithms.add(new StarForestAlgoNew(PlacerType.FORCE_DIRECTED, animated));
            //  WARNING: EXHAUSTED_FORCE_DIRECTED doesn't work for all numbers of words (due to a bug in SingleStarAlgo)
            algorithms.add(new StarForestAlgo2(PlacerType.EXHAUSTIVE_FORCE_DIRECTED, animated));
            //						algorithms.add(new RecursiveSpiralCluster());
            //						algorithms.add(new CycleCoverAlgo(PlacerType.SINGLE_SPIRAL, CycleType.REGULAR, animated));
            //						algorithms.add(new CycleCoverAlgo(PlacerType.SINGLE_SPIRAL, CycleType.WRAPPED, animated));
            //						algorithms.add(new CycleCoverAlgo(PlacerType.RECURSIVE_SPIRAL, CycleType.REGULAR, animated));
            //						algorithms.add(new CycleCoverAlgo(PlacerType.RECURSIVE_SPIRAL, CycleType.WRAPPED, animated));
            //						algorithms.add(new CycleCoverAlgo(PlacerType.FORCE_DIRECTED, CycleType.REGULAR, animated));
            //						algorithms.add(new CycleCoverAlgo(PlacerType.FORCE_DIRECTED, CycleType.WRAPPED, animated));
            //						algorithms.add(new WordleAlgo());

            System.out.println(data_location + "_" + i);
            System.out.println(numWords);
            System.out.println(algorithms.size());
            for (LayoutAlgo algo : algorithms)
            {
                System.out.println();
                System.out.println(algo);
                new WCVisualizer2().run(doc, algo, numWords, i);
                System.gc();
            }

            if (i != documents.size() - 1)
            {
                System.out.println();
                System.out.println();
            }
        }

    }

    private void run(WCVDocument document, LayoutAlgo algoToRun, int numWords, int dataSetNum)
    {

        // 2. build similarities, words etc
        List<Word> words = new ArrayList<Word>();
        Map<WordPair, Double> similarity = new HashMap<WordPair, Double>();
        extractSimilarities(document, words, similarity);

        // 3. run a layout algorithm
        LayoutAlgo algo = runLayout(algoToRun, words, similarity);

    }

    private static List<WCVDocument> getDocumentsFromFile(String document)
    {
        WikipediaXMLReader xmlReader = new WikipediaXMLReader(document);
        xmlReader.read();
        Iterator<String> texts = xmlReader.getTexts();

        List<WCVDocument> docs = new ArrayList<WCVDocument>();
        while (texts.hasNext())
        {
            WCVDocument doc = new WCVDocument(texts.next());
            doc.parse();
            docs.add(doc);
        }

        return docs;
    }

    private void extractSimilarities(WCVDocument wordifier, List<Word> words, Map<WordPair, Double> similarity)
    {
        //SimilarityAlgo coOccurenceAlgo = new CosineCoOccurenceAlgo();
        SimilarityAlgo coOccurenceAlgo = new RandomSimilarityAlgo();
        coOccurenceAlgo.initialize(wordifier);
        coOccurenceAlgo.run();
        Map<WordPair, Double> sim = coOccurenceAlgo.getSimilarity();

        for (Word w : wordifier.getWords())
            words.add(w);

        for (WordPair wp : sim.keySet())
            similarity.put(wp, sim.get(wp));
    }

    private LayoutAlgo runLayout(LayoutAlgo algo, List<Word> words, Map<WordPair, Double> similarity)
    {

        algo.setData(words, similarity);
        algo.setConstraints(new BoundingBoxGenerator(25000.0));

        long startTime = System.currentTimeMillis();
        algo.run();
        System.out.printf("time, %.3f \n", (double)(System.currentTimeMillis() - startTime) / 1000.0);

        return algo;
    }

}
