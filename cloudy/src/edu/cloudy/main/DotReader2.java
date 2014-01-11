package edu.cloudy.main;

import edu.cloudy.layout.CycleCoverAlgo2;
import edu.cloudy.layout.CycleType;
import edu.cloudy.layout.LayoutAlgo;
import edu.cloudy.layout.PlacerType;
import edu.cloudy.layout.StarForestAlgo2;
import edu.cloudy.layout.WordleAlgo;
import edu.cloudy.layout.packing.RecursiveSpiralCluster;
import edu.cloudy.nlp.WCVDocument;
import edu.cloudy.nlp.Word;
import edu.cloudy.nlp.WordPair;
import edu.cloudy.utils.BoundingBoxGenerator;
import edu.cloudy.utils.Logger;
import edu.cloudy.utils.WikipediaXMLReader;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 * @author spupyrev 
 */
public class DotReader2
{

    boolean animated = false;
    boolean suppressGraphics = false;

    public static void main(String argc[])
    {
        Logger.doLogging = false;
        new DotReader2().run();
    }

    private void run()
    {
        // 2. build similarities, words etc
        List<Word> words = new ArrayList<Word>();
        Map<WordPair, Double> similarity = new HashMap<WordPair, Double>();
        readDotFile("data/colors_50-lab.gv", words, similarity);

        String data_location = "data/test_wiki";
        List<WCVDocument> documents = getDocumentsFromFile(data_location);

        List<LayoutAlgo> algorithms;

        algorithms = new ArrayList<LayoutAlgo>(13);
        algorithms.add(new StarForestAlgo2(PlacerType.SINGLE_SPIRAL, animated));
        algorithms.add(new StarForestAlgo2(PlacerType.RECURSIVE_SPIRAL, animated));
        algorithms.add(new StarForestAlgo2(PlacerType.FORCE_DIRECTED, animated));
        algorithms.add(new RecursiveSpiralCluster());
        algorithms.add(new CycleCoverAlgo2(PlacerType.SINGLE_SPIRAL, CycleType.REGULAR, animated));
        algorithms.add(new CycleCoverAlgo2(PlacerType.SINGLE_SPIRAL, CycleType.WRAPPED, animated));
        algorithms.add(new CycleCoverAlgo2(PlacerType.RECURSIVE_SPIRAL, CycleType.REGULAR, animated));
        algorithms.add(new CycleCoverAlgo2(PlacerType.RECURSIVE_SPIRAL, CycleType.WRAPPED, animated));
        algorithms.add(new CycleCoverAlgo2(PlacerType.FORCE_DIRECTED, CycleType.REGULAR, animated));
        algorithms.add(new CycleCoverAlgo2(PlacerType.FORCE_DIRECTED, CycleType.WRAPPED, animated));
        algorithms.add(new WordleAlgo());

        words = filterWords(words, similarity, 50);
        similarity = filterSimilarities(words, similarity);

        System.out.println(similarity);

        for (LayoutAlgo algo : algorithms)
        {

            // 3. run a layout algorithm
            LayoutAlgo ran = runLayout(algo, words, similarity);

            // 4. visualize it
            visualize(words, similarity, ran, 1);
        }
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

    private List<Word> filterWords(List<Word> words, Map<WordPair, Double> similarity, int maxWords)
    {
        Collections.sort(words);
        Collections.reverse(words);

        if (maxWords > words.size())
            return words;

        return words.subList(0, maxWords);
    }

    private Map<WordPair, Double> filterSimilarities(List<Word> words, Map<WordPair, Double> similarity)
    {
        Map<WordPair, Double> similarityNew = new HashMap();
        for (WordPair wp : similarity.keySet())
        {
            if (words.contains(wp.getFirst()) && words.contains(wp.getSecond()))
            {
                similarityNew.put(wp, similarity.get(wp));
            }
        }
        return similarityNew;
    }

    private void visualize(List<Word> words, Map<WordPair, Double> similarity, LayoutAlgo algo, int dataSetNum)
    {
        //        if (algo instanceof ClusterLayoutAlgo && animated)
        //            new AnimatedWordCloudFrame(words, similarity, (ClusterLayoutAlgo)algo, suppressGraphics, null);
        //        else
        //            new WordCloudFrame(words, similarity, algo, suppressGraphics, null);
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

    private void readDotFile(String filename, List<Word> words, Map<WordPair, Double> similarity)
    {
        Map<String, Word> allWords = new HashMap();

        try
        {
            Scanner sc = new Scanner(new File(filename));
            System.out.println(sc.nextLine());
            System.out.println(sc.nextLine());

            while (sc.hasNext())
            {
                String line = sc.nextLine();
                if (line.contains("imp"))
                {
                    Word w = parseNode(line);
                    words.add(w);
                    allWords.put(w.word, w);

                    if (words.size() % 100 == 0)
                        System.out.println("read " + words.size() + " words");
                }
                else if (line.contains("sim"))
                {
                    parseEdge(line, allWords, similarity);

                    if (similarity.size() % 100 == 0)
                        System.out.println("read " + similarity.size() + " edges");
                }
                else
                    break;
            }

        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    private void parseEdge(String line, Map<String, Word> allWords, Map<WordPair, Double> similarity)
    {
        String s1 = "";
        int i = 0;
        while (line.charAt(i) != '"')
            i++;
        i++;
        while (line.charAt(i) != '"')
        {
            s1 += line.charAt(i);
            i++;
        }
        i++;

        String s2 = "";
        while (line.charAt(i) != '"')
            i++;
        i++;
        while (line.charAt(i) != '"')
        {
            s2 += line.charAt(i);
            i++;
        }

        while (line.charAt(i) != 'm')
            i++;
        i++;
        i++;
        String sim = "";
        while (line.charAt(i) != ']')
        {
            sim += line.charAt(i);
            i++;
        }

        Word w1 = allWords.get(s1);
        Word w2 = allWords.get(s2);
        similarity.put(new WordPair(w1, w2), Double.parseDouble(sim));
    }

    private Word parseNode(String line)
    {
        String s1 = "";
        int i = 0;
        while (line.charAt(i) != '"')
            i++;
        i++;
        while (line.charAt(i) != '"')
        {
            s1 += line.charAt(i);
            i++;
        }

        String s2 = "";
        while (line.charAt(i) != '=')
            i++;
        i++;
        while (line.charAt(i) != ',')
        {
            s2 += line.charAt(i);
            i++;
        }

        return new Word(s1, Double.parseDouble(s2));
    }

}
