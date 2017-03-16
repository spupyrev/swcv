package edu.test.misc;

import edu.cloudy.layout.ContextPreservingAlgo;
import edu.cloudy.layout.LayoutAlgo;
import edu.cloudy.layout.LayoutResult;
import edu.cloudy.layout.WordGraph;
import edu.cloudy.nlp.Word;
import edu.cloudy.nlp.ItemPair;
import edu.cloudy.ui.WordCloudFrame;
import edu.cloudy.utils.Logger;
import edu.cloudy.utils.TimeMeasurer;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 * @author spupyrev 
 */
public class DotReader
{
    public static void main(String argc[])
    {
        Logger.doLogging = false;
        new DotReader().run();
    }

    private void run()
    {
        // 2. build similarities, words etc
        List<Word> words = new ArrayList<Word>();
        Map<ItemPair<Word>, Double> similarity = new HashMap<ItemPair<Word>, Double>();
        readDotFile("data/1101.dot", words, similarity);

        words = filterWords(words, similarity, 50);
        similarity = filterSimilarities(words, similarity);
        WordGraph wordGraph = new WordGraph(words, similarity);

        // 3. run a layout algorithm
        LayoutResult layout = runLayout(wordGraph);

        // 4. visualize it
        visualize(wordGraph, layout);
    }

    private List<Word> filterWords(List<Word> words, Map<ItemPair<Word>, Double> similarity, int maxWords)
    {
        Collections.sort(words, Comparator.reverseOrder());

        if (maxWords > words.size())
            return words;

        return words.subList(0, maxWords);
    }

    private Map<ItemPair<Word>, Double> filterSimilarities(List<Word> words, Map<ItemPair<Word>, Double> similarity)
    {
        Map<ItemPair<Word>, Double> similarityNew = new HashMap();
        for (ItemPair<Word> wp : similarity.keySet())
        {
            if (words.contains(wp.getFirst()) && words.contains(wp.getSecond()))
            {
                similarityNew.put(wp, similarity.get(wp));
            }
        }
        return similarityNew;
    }

    private void readDotFile(String filename, List<Word> words, Map<ItemPair<Word>, Double> similarity)
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

            sc.close();
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    private void parseEdge(String line, Map<String, Word> allWords, Map<ItemPair<Word>, Double> similarity)
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
        similarity.put(new ItemPair<Word>(w1, w2), Double.parseDouble(sim));
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

    private LayoutResult runLayout(WordGraph wordGraph)
    {
        LayoutAlgo algo = new ContextPreservingAlgo();
        return TimeMeasurer.execute(() -> algo.layout(wordGraph));
    }

    private void visualize(WordGraph wordGraph, LayoutResult layout)
    {
        new WordCloudFrame(wordGraph, layout, null);
    }

}
