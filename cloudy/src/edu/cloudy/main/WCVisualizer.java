package edu.cloudy.main;

import edu.cloudy.clustering.IClusterAlgo;
import edu.cloudy.clustering.KMeansPlusPlus;
import edu.cloudy.layout.LayoutAlgo;
import edu.cloudy.layout.MDSWithFDPackingAlgo;
import edu.cloudy.nlp.WCVDocument;
import edu.cloudy.nlp.Word;
import edu.cloudy.nlp.WordPair;
import edu.cloudy.nlp.ranking.TFRankingAlgo;
import edu.cloudy.nlp.similarity.CosineCoOccurenceAlgo;
import edu.cloudy.nlp.similarity.SimilarityAlgo;
import edu.cloudy.ui.WordCloudFrame;
import edu.cloudy.utils.Logger;
import edu.cloudy.utils.SWCPoint;
import edu.cloudy.utils.WikipediaXMLReader;
import edu.test.YoutubeCommentsReaderTest;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

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
        new WCVisualizer().run();
    }

    private void run()
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

    private WCVDocument readDocument()
    {
        //List<WCVDocument> alldocs = ALENEXPaperEvalulator.readDocuments(ALENEXPaperEvalulator.FILES_WIKI);

        //WikipediaXMLReader xmlReader = new WikipediaXMLReader("data/twitter");
        WikipediaXMLReader xmlReader = new WikipediaXMLReader("data/focs");
        xmlReader.read();
        Iterator<String> texts = xmlReader.getTexts();

        WCVDocument doc = null;
        while (texts.hasNext())
        {
            doc = new WCVDocument(texts.next());
            doc.parse();

            //alldocs.add(doc);
        }

        System.out.println("#words: " + doc.getWords().size());
        //doc.weightFilter(15, new TFIDFRankingAlgo());
        doc.weightFilter(30, new TFRankingAlgo());
        //doc.weightFilter(15, new LexRankingAlgo());

        return doc;
    }

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

    private WCVDocument readURL()
    {
        String url = "http://gama.cs.arizona.edu";
        Document document;
        try
        {
            document = Jsoup.connect(url).get();
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }

        List<Element> tn;
        tn = document.select("div");
        for (Element t : tn)
            t.append(".");

        tn = document.select("span");
        for (Element t : tn)
            t.append(".");

        tn = document.select("br");
        for (Element t : tn)
            t.append(".");

        tn = document.select("li");
        for (Element t : tn)
            t.append(".");

        String text = document.text();
        WCVDocument doc = new WCVDocument(text);
        System.out.println(text);
        doc.parse();

        System.out.println("#words: " + doc.getWords().size());
        doc.weightFilter(50, new TFRankingAlgo());

        return doc;
    }

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
        //SimilarityAlgo[] coOccurenceAlgo111 = {new LexicalSimilarityAlgo(),new CosineCoOccurenceAlgo(), new JaccardCoOccurenceAlgo()};
        //SimilarityAlgo coOccurenceAlgo = new LexicalSimilarityAlgo();
        //SimilarityAlgo coOccurenceAlgo = new DiceCoefficientAlgo();

        //SimilarityAlgo coOccurenceAlgo = new JaccardCoOccurenceAlgo();
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

    private void extractSimilaritiesTest(WCVDocument wordifier, List<Word> words, Map<WordPair, Double> similarity)
    {
        for (Word w : wordifier.getWords())
            words.add(w);

        SWCPoint[] p = new SWCPoint[words.size()];
        for (int i = 0; i < words.size(); i++)
        {
            p[i] = SWCPoint.random();
        }

        for (int i = 0; i < words.size(); i++)
        {
            for (int j = i + 1; j < words.size(); j++)
            {
                double len = p[i].distance(p[j]);
                similarity.put(new WordPair(words.get(i), words.get(j)), 1.0 - len);
            }
        }
    }

    private LayoutAlgo runLayout(List<Word> words, Map<WordPair, Double> similarity)
    {
        //LayoutAlgo algo = new ContextPreservingAlgo(words, similarity);
        //LayoutAlgo algo = new InflateAndPushAlgo();
        //LayoutAlgo algo = new MDSAlgo();
        //LayoutAlgo algo = new StarForestAlgo();
        //LayoutAlgo algo = new CycleCoverAlgo();
        //LayoutAlgo algo = new SeamCarvingAlgo();
        //LayoutAlgo algo = new WordleAlgo(words, similarity);
        LayoutAlgo algo = new MDSWithFDPackingAlgo(words, similarity);

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

        long startTime = System.currentTimeMillis();
        algo.run(words, similarity);
        //System.out.printf("clustering done in %.3f sec\n", (System.currentTimeMillis() - startTime) / 1000.0);
        //System.out.println("#clusters: " + algo.getClusterNumber());

        return algo;
    }

    private void visualize(List<Word> words, Map<WordPair, Double> similarity, LayoutAlgo algo, IClusterAlgo clusterAlgo)
    {
        new WordCloudFrame(words, similarity, algo, clusterAlgo);
    }

}
