package edu.cloudy.main;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import edu.cloudy.colors.ColorScheme;
import edu.cloudy.colors.ColorSchemeRegistry;
import edu.cloudy.layout.LayoutAlgo;
import edu.cloudy.layout.LayoutResult;
import edu.cloudy.layout.SeamCarvingAlgo;
import edu.cloudy.layout.WordGraph;
import edu.cloudy.nlp.ParseOptions;
import edu.cloudy.nlp.SWCDocument;
import edu.cloudy.nlp.Word;
import edu.cloudy.nlp.WordPair;
import edu.cloudy.nlp.ranking.TFRankingAlgo;
import edu.cloudy.nlp.similarity.CosineCoOccurenceAlgo;
import edu.cloudy.nlp.similarity.SimilarityAlgo;
import edu.cloudy.ui.WordCloudFrame;
import edu.cloudy.utils.Logger;
import edu.cloudy.utils.TimeMeasurer;

/**
 * @author spupyrev 
 * Apr 30, 2013
 *  
 * create and visualize a wordcloud for a document
 */
public class SWCVisualizer
{
    public static void main(String argc[])
    {
        Logger.doLogging = true;
        try
        {
            new SWCVisualizer().run();
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    private void run() throws FileNotFoundException
    {
        // 1. read a document
        SWCDocument document = readDocument();

        // 2. build similarities, words etc
        List<Word> words = new ArrayList<Word>();
        Map<WordPair, Double> similarity = extractSimilarities(document, words);
        WordGraph wordGraph = new WordGraph(words, similarity);

        // 3. run a layout algorithm
        LayoutResult algo = runLayout(wordGraph);

        // 4. visualize it
        visualize(wordGraph, algo);
        
        //new JFrame().setVisible(true);;
    }

    /**
     * @return
     * @throws FileNotFoundException
     */
    private SWCDocument readDocument() throws FileNotFoundException
    {
        
        
        
        
        Scanner scanner = new Scanner(new File("loremipsum.txt"), "UTF-8");
        StringBuilder sb = new StringBuilder();
        while (scanner.hasNextLine())
        {
            sb.append(scanner.nextLine() + "\n");
        }
        scanner.close();

        SWCDocument doc = new SWCDocument(sb.toString());
        doc.parse(new ParseOptions());

        System.out.println("#words: " + doc.getWords().size());

        //doc.weightFilter(15, new TFIDFRankingAlgo());
        doc.weightFilter(150, new TFRankingAlgo());
        //doc.weightFilter(15, new LexRankingAlgo());

        return doc;
    }

    private Map<WordPair, Double> extractSimilarities(SWCDocument document, List<Word> words)
    {
        SimilarityAlgo coOccurenceAlgo = new CosineCoOccurenceAlgo();
        Map<WordPair, Double> similarity = coOccurenceAlgo.computeSimilarity(document);

        for (Word w : document.getWords())
            words.add(w);

        List<WordPair> topPairs = new ArrayList<WordPair>();
        for (WordPair wp : similarity.keySet())
        {
            topPairs.add(wp);
        }

        Collections.sort(topPairs, (o1, o2) -> similarity.get(o2).compareTo(similarity.get(o1)));

        /*for (String s : doc.getSentences())
            System.out.println(s);
        
        System.out.println("top words:");
        for (int i = 0; i < words.size(); i++)
        {
           Word w = words.get(i);
            System.out.println(w.word + " (" + w.stem + ")  " + w.weight);
        }

        System.out.println("===================");
        System.out.println("top pairs:");
        for (int i = 0; i < 1000 && i < topPairs.size(); i++)
        {
            WordPair wp = topPairs.get(i);
            double simV = similarity.get(wp);
           double dist = LayoutUtils.idealDistanceConverter(simV);
            System.out.println(wp.getFirst().word + " " + wp.getSecond().word + "  sim: " + simV + "  dist: " + dist);
        }*/

        return similarity;
    }

    /**
     * @param wordGraph
     * @return
     */
    private LayoutResult runLayout(WordGraph wordGraph)
    {
        //LayoutAlgo algo = new ContextPreservingAlgo();
        //LayoutAlgo algo = new InflateAndPushAlgo();
        //LayoutAlgo algo = new MDSAlgo(false);
        //LayoutAlgo algo = new StarForestAlgo();
        //LayoutAlgo algo = new CycleCoverAlgo();
    	LayoutAlgo algo = new SeamCarvingAlgo();
        //LayoutAlgo algo = new WordleAlgo();
        //LayoutAlgo algo = new TagCloudAlphabeticalAlgo();
        //LayoutAlgo algo = new TagCloudAlphabeticalAlgo();
       //LayoutAlgo algo = new TagCloudRankAlgo();
        //LayoutAlgo algo = new ForceDirectedPackingAlgo();

        return TimeMeasurer.execute("layout", () -> algo.layout(wordGraph));
    }

    private void visualize(WordGraph wordGraph, LayoutResult layout)
    {
        ColorScheme colorScheme = ColorSchemeRegistry.getDefault();
        //ColorScheme colorScheme = ColorSchemeRegistry.getByName("TRISCHEME_2");
        colorScheme.initialize(wordGraph);
        new WordCloudFrame(wordGraph, layout, colorScheme);
    }
}
