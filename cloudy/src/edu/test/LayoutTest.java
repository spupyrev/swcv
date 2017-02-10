package edu.test;

import edu.cloudy.geom.SWCRectangle;
import edu.cloudy.layout.ContextPreservingAlgo;
import edu.cloudy.layout.CycleCoverAlgo;
import edu.cloudy.layout.LayoutResult;
import edu.cloudy.layout.SeamCarvingAlgo;
import edu.cloudy.layout.StarForestAlgo;
import edu.cloudy.layout.TagCloudRankAlgo;
import edu.cloudy.layout.WordGraph;
import edu.cloudy.layout.WordleAlgo;
import edu.cloudy.layout.packing.ForceDirectedPackingAlgo;
import edu.cloudy.nlp.Word;
import edu.cloudy.nlp.ItemPair;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * @author spupyrev
 * Nov 11, 2014
 */
public class LayoutTest
{
    private static final Random rnd = new Random(123);

    @Test
    public void testCycle()
    {
        WordGraph wordGraph = createCycle();

        checkLayout(wordGraph, new ForceDirectedPackingAlgo().layout(wordGraph));
        checkLayout(wordGraph, new CycleCoverAlgo().layout(wordGraph));
        checkLayout(wordGraph, new StarForestAlgo().layout(wordGraph));
        checkLayout(wordGraph, new ContextPreservingAlgo().layout(wordGraph));
    }

    @Test
    public void testRandomGraph()
    {
        WordGraph wordGraph = createRandomGraph();

        checkLayout(wordGraph, new ForceDirectedPackingAlgo().layout(wordGraph));
        checkLayout(wordGraph, new WordleAlgo().layout(wordGraph));
        checkLayout(wordGraph, new TagCloudRankAlgo().layout(wordGraph));
        checkLayout(wordGraph, new SeamCarvingAlgo().layout(wordGraph));
        //checkLayout(wordGraph, new SinglePathAlgo().layout(wordGraph));
        checkLayout(wordGraph, new StarForestAlgo().layout(wordGraph));
    }

    private WordGraph createCycle()
    {
        Word a = new Word("Aaaa", 1.0);
        Word b = new Word("Bbbb", 2.0);
        Word c = new Word("Cccc", 3.0);
        Word d = new Word("D", 5.0);
        Word e = new Word("eeeeeeeeee", 2.0);

        List<Word> words = Arrays.asList(a, b, c, d, e);

        Map<ItemPair<Word>, Double> similarity = new HashMap();
        for (int i = 0; i < words.size(); i++)
            for (int j = 0; j < words.size(); j++)
            {
                similarity.put(new ItemPair<Word>(words.get(i), words.get(j)), (i == j ? 1.0 : 0.0));
            }

        similarity.put(new ItemPair<Word>(a, b), 0.5);
        similarity.put(new ItemPair<Word>(b, c), 0.9);
        similarity.put(new ItemPair<Word>(d, c), 0.8);
        similarity.put(new ItemPair<Word>(d, e), 1.0);
        similarity.put(new ItemPair<Word>(e, a), 0.99);

        return new WordGraph(words, similarity);
    }

    private WordGraph createRandomGraph()
    {
        return createRandomGraph(5 + rnd.nextInt(10));
    }

    public static WordGraph createRandomGraph(int n)
    {
        List<Word> words = randomWords(n);
        Map<ItemPair<Word>, Double> similarity = randomSimilarities(words);

        return new WordGraph(words, similarity);
    }

    private static List<Word> randomWords(int n)
    {
        List<Word> words = new ArrayList();
        for (int i = 0; i < n; i++)
        {
            Word a = new Word(randomWord(5, 10), 1.0 + rnd.nextDouble() * 4);
            words.add(a);
        }
        return words;
    }

    private static String randomWord(int minLength, int maxLength)
    {
        int len = minLength + rnd.nextInt(maxLength - minLength);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < len; i++)
        {
            if (rnd.nextBoolean())
                sb.append(Character.toChars(rnd.nextInt(26) + 'a'));
            else
                sb.append(Character.toChars(rnd.nextInt(26) + 'A'));
        }

        return sb.toString();
    }

    private static Map<ItemPair<Word>, Double> randomSimilarities(List<Word> words)
    {
        Map<ItemPair<Word>, Double> similarity = new HashMap();
        for (int i = 0; i < words.size(); i++)
        {
            Word a = words.get(i);
            for (int j = i + 1; j < words.size(); j++)
            {
                Word b = words.get(j);

                double weight = rnd.nextDouble();
                similarity.put(new ItemPair<Word>(a, b), weight);
            }
            similarity.put(new ItemPair<Word>(a, a), 1.0);
        }
        return similarity;
    }

    public static void checkLayout(WordGraph wordGraph, LayoutResult layout)
    {
        //ColorScheme colorScheme = ColorSchemeRegistry.getDefault();
        //colorScheme.initialize(wordGraph);
        //new WordCloudFrame(wordGraph, layout, colorScheme);

        // words do not intersect
        List<Word> words = wordGraph.getWords();
        for (int i = 0; i < words.size(); i++)
            for (int j = i + 1; j < words.size(); j++)
            {
                SWCRectangle rect1 = layout.getWordPosition(words.get(i));
                SWCRectangle rect2 = layout.getWordPosition(words.get(j));
                if (rect1.intersects(rect2))
                {
                    System.out.println(rect1);
                    System.out.println(rect2);
                }
                Assert.assertFalse(rect1.intersects(rect2));
            }
    }

}
