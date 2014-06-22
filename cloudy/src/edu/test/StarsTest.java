package edu.test;

import edu.cloudy.graph.WordGraph;
import edu.cloudy.layout.SingleStarAlgo;
import edu.cloudy.nlp.Word;
import edu.cloudy.nlp.WordPair;
import edu.cloudy.utils.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class StarsTest
{
    private static Random rnd = new Random(123);

    public static void main(String[] args)
    {
        Logger.doLogging = false;

        List<Word> words = new ArrayList<Word>();
        Map<WordPair, Double> similarity = new HashMap<WordPair, Double>();
        double expectedValue;

        expectedValue = test1(words, similarity);
        checkStarsResult(words, similarity, expectedValue);
    }

    private static void checkStarsResult(List<Word> words, Map<WordPair, Double> similarity, double expectedValue)
    {
        double totalRealizedValue = getStarsResult(words, similarity);

        System.out.println("====================");
        System.out.println("totalRealizedValue = " + totalRealizedValue);
        if (!close(totalRealizedValue, expectedValue))
            throw new RuntimeException("totalRealizedValue=" + totalRealizedValue + "  expectedValue=" + expectedValue);
    }

    private static double getStarsResult(List<Word> words, Map<WordPair, Double> similarity)
    {
        SingleStarAlgo starsAlgo = new SingleStarAlgo(words, similarity);
        WordGraph graph = new WordGraph(words, similarity);
        starsAlgo.setGraph(graph);

        // Run it!
        starsAlgo.run();
        return starsAlgo.getRealizedWeight();
    }

    static double test1(List<Word> words, Map<WordPair, Double> similarity)
    {
        Word a1 = new Word("a1", 125.0);
        Word a2 = new Word("a2", 115.0);
        Word a3 = new Word("a3", 120.0);
        Word a4 = new Word("a4", 120.0);
        Word a5 = new Word("a5", 5.0);
        Word a6 = new Word("a6", 5.0);
        Word a7 = new Word("a7", 100.0);
        Word a8 = new Word("a8", 200.0);
        Word a9 = new Word("a9", 120.0);
        Word b = new Word("BBBBBBBBBBBB", 70.0);

        words.clear();
        words.add(b);
        words.add(a1);
        words.add(a2);
        words.add(a3);
        words.add(a4);
        words.add(a5);
        words.add(a6);
        //words.add(a7);
        //words.add(a8);
        //words.add(a9);

        similarity.clear();
        similarity.put(new WordPair(b, a1), 10.0);
        similarity.put(new WordPair(a2, b), 10.0);
        similarity.put(new WordPair(a3, b), 10.0);
        similarity.put(new WordPair(a4, b), 10.0);
        similarity.put(new WordPair(a5, b), 1.0);
        similarity.put(new WordPair(a6, b), 1.0);
        //similarity.put(new WordPair(a7, b), 1.0);
        //similarity.put(new WordPair(b, a8), 1.0);
        //similarity.put(new WordPair(b, a9), 1.0);

        return 4;
    }

    static double test2(List<Word> words, Map<WordPair, Double> similarity)
    {
        Word a = new Word("Aaaa", 100.0);
        Word b = new Word("Bbbb", 200.0);
        Word c = new Word("Cccc", 300.0);
        Word d = new Word("D", 500.0);
        Word e = new Word("eeeeeeeeee", 200.0);

        words.clear();
        words.add(a);
        words.add(b);
        words.add(c);
        words.add(d);
        words.add(e);

        similarity.clear();
        similarity.put(new WordPair(a, b), 10.0);
        similarity.put(new WordPair(b, c), 160.0);
        similarity.put(new WordPair(d, c), 140.0);
        similarity.put(new WordPair(d, e), 100.0);
        similarity.put(new WordPair(e, a), 1.0);

        return 411;
    }

    static void testRandom(List<Word> words, Map<WordPair, Double> similarity, int n)
    {
        words.clear();
        for (int i = 0; i < n; i++)
        {
            Word a = new Word(randomWord(5, 10), rnd.nextDouble() * 100);
            words.add(a);
        }

        randomSimilarities(words, similarity);
    }

    static String randomWord(int minLength, int maxLength)
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

    static Map<WordPair, Double> randomSimilarities(List<Word> words, Map<WordPair, Double> similarity)
    {
        similarity.clear();
        for (int i = 0; i < words.size(); i++)
            for (int j = i + 1; j < words.size(); j++)
            {
                Word a = words.get(i);
                Word b = words.get(j);

                double weight = rnd.nextDouble();// * 100;
                similarity.put(new WordPair(a, b), weight);
            }

        return similarity;
    }

    static boolean close(double a, double b)
    {
        return (Math.abs(a - b) < 1e-8);
    }

}
