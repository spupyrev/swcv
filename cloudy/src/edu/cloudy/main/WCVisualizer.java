package edu.cloudy.main;

import edu.cloudy.clustering.IClusterAlgo;
import edu.cloudy.clustering.KMeansPlusPlus;
import edu.cloudy.layout.LayoutAlgo;
import edu.cloudy.layout.WordleAlgo;
import edu.cloudy.nlp.WCVDocument;
import edu.cloudy.nlp.Word;
import edu.cloudy.nlp.WordPair;
import edu.cloudy.nlp.ranking.TFRankingAlgo;
import edu.cloudy.nlp.similarity.CosineCoOccurenceAlgo;
import edu.cloudy.nlp.similarity.SimilarityAlgo;
import edu.cloudy.ui.WordCloudFrame;
import edu.cloudy.utils.Logger;
import edu.test.YoutubeCommentsReaderTest;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 * @author spupyrev 
 * Apr 30, 2013 
 * create and visualize a wordcloud for a document
 */
public class WCVisualizer
{

    public static void main(String argc[])
    {
        Logger.doLogging = false;

        try
        {
            new WCVisualizer().run();
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    private void run() throws FileNotFoundException
    {
        // 1. read a document
        WCVDocument document = readDocument();
        //WCVDocument document = readURL();
        //WCVDocument document = readYoutube();

        // 2. build similarities, words etc
        List<Word> words = new ArrayList<Word>();
        Map<WordPair, Double> similarity = new HashMap<WordPair, Double>();
        extractSimilarities(document, words, similarity);

        // 3. run a layout algorithm
        LayoutAlgo algo = runLayout(words, similarity);
        IClusterAlgo clusterAlgo = runClustering(words, similarity);

        // 4. visualize it
        visualize(words, similarity, algo, clusterAlgo);
        //visualize(words, similarity, algo, null);
    }

    private WCVDocument readDocument() throws FileNotFoundException
    {
        //List<WCVDocument> alldocs = ALENEXPaperEvalulator.readDocuments(ALENEXPaperEvalulator.FILES_WIKI);
        Scanner scanner = new Scanner(new File("data/papers"));
        StringBuilder sb = new StringBuilder();
        while (scanner.hasNextLine())
        {
            sb.append(scanner.nextLine());
        }
        scanner.close();

        WCVDocument doc = new WCVDocument(sb.toString());
        doc.parse();

        System.out.println("#words: " + doc.getWords().size());
        //doc.weightFilter(15, new TFIDFRankingAlgo());
        doc.weightFilter(10, new TFRankingAlgo());
        //doc.weightFilter(15, new LexRankingAlgo());

        return doc;
    }

    @SuppressWarnings("unused")
    private WCVDocument readPDFDocument()
    {
        /*PDFReader reader = new PDFReader("file:///E:/Research/Arizona/wordle/tex-apprx/clouds.pdf");
        assert (reader.isConnected());
        WCVDocument doc = new WCVDocument(reader.getText());
        doc.parse();

        System.out.println("#words: " + doc.getWords().size());
        doc.weightFilter(85, new LexRankingAlgo());

        return doc;*/
        return null;
    }

    @SuppressWarnings("unused")
    private WCVDocument readYoutube()
    {
        WCVDocument wdoc = new WCVDocument(YoutubeCommentsReaderTest.getComments("5guMumPFBag"));
        wdoc.parse();

        System.out.println("#words: " + wdoc.getWords().size());
        wdoc.weightFilter(50, new TFRankingAlgo());

        return wdoc;
    }

    private void extractSimilarities(WCVDocument wordifier, List<Word> words, final Map<WordPair, Double> similarity)
    {
        SimilarityAlgo coOccurenceAlgo = new CosineCoOccurenceAlgo();
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

        /*System.out.println("top words:");
        for (int i = 0; i < words.size(); i++) {
        	Word w = words.get(i);
        	System.out.println(w.word + " (" + w.stem + ")  " + w.weight);
        }

        System.out.println("===================");
        System.out.println("top pairs:");
        for (int i = 0; i < 20; i++) {
        	WordPair wp = topPairs.get(i);
        	System.out.println(wp.getFirst().word + " " + wp.getSecond().word + "  " + similarity.get(wp));
        }*/
    }

    private LayoutAlgo runLayout(List<Word> words, Map<WordPair, Double> similarity)
    {
        //LayoutAlgo algo = new ContextPreservingAlgo(words, similarity);
        //LayoutAlgo algo = new InflateAndPushAlgo();
        //LayoutAlgo algo = new MDSAlgo(words, similarity);
        //LayoutAlgo algo = new StarForestAlgo();
        //LayoutAlgo algo = new CycleCoverAlgo(words, similarity);
        //LayoutAlgo algo = new SeamCarvingAlgo(words, similarity);
        LayoutAlgo algo = new WordleAlgo(words, similarity);
        //LayoutAlgo algo = new MDSWithFDPackingAlgo(words, similarity);

        long startTime = System.currentTimeMillis();
        algo.run();
        System.out.printf("layout done in %.3f sec\n", (System.currentTimeMillis() - startTime) / 1000.0);

        return algo;
    }

    private IClusterAlgo runClustering(List<Word> words, Map<WordPair, Double> similarity)
    {
        int K = (int)Math.sqrt((double)words.size() / 2);

        //IClusterAlgo algo = new KMeans(K);
        IClusterAlgo algo = new KMeansPlusPlus(K);

        //long startTime = System.currentTimeMillis();
        algo.run(words, similarity);
        //System.out.printf("clustering done in %.3f sec\n", (System.currentTimeMillis() - startTime) / 1000.0);
        //System.out.println("#clusters: " + algo.getClusterNumber());

        return algo;
    }

    private void visualize(List<Word> words, Map<WordPair, Double> similarity, LayoutAlgo algo, IClusterAlgo clusterAlgo)
    {
        new WordCloudFrame(words, similarity, algo, clusterAlgo);

        renderSVG("test.svg", words, algo);
    }

    private void renderSVG(String filename, List<Word> words, LayoutAlgo algo)
    {
    }

}
