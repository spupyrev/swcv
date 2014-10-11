package edu.test;

import edu.cloudy.layout.CycleCoverAlgo;
import edu.cloudy.layout.SingleStarAlgo;
import edu.cloudy.nlp.Word;
import edu.cloudy.nlp.WordPair;
import edu.cloudy.utils.Logger;
import edu.cloudy.utils.UnorderedPair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

@SuppressWarnings("all")
public class MatchingTest
{
    private static Random rnd = new Random(123);

    public static void main(String[] args)
    {
        Logger.doLogging = false;

        List<Word> words = new ArrayList<Word>();
        Map<WordPair, Double> similarity = new HashMap<WordPair, Double>();
        double expectedValue;

        expectedValue = test2(words, similarity);
        checkCyclesResult(words, similarity, expectedValue);

        expectedValue = test2(words, similarity);
        checkCyclesResult(words, similarity, expectedValue / 2);

        expectedValue = testHamiltonian(words, similarity, 50);
        checkCyclesResult(words, similarity, expectedValue / 2);

        expectedValue = testHamiltonian(words, similarity, 99);
        checkCyclesResult(words, similarity, expectedValue / 2);

        expectedValue = testTwoCycles(words, similarity, 10);
        checkCyclesResult(words, similarity, expectedValue / 2);

        expectedValue = testTwoCycles(words, similarity, 53);
        checkCyclesResult(words, similarity, expectedValue / 2);

        expectedValue = testTwoCycles(words, similarity, 77);
        checkCyclesResult(words, similarity, expectedValue / 2);

        expectedValue = testTwoCycles(words, similarity, 100);
        checkCyclesResult(words, similarity, expectedValue / 2);

        double cycleSum = 0;
        double greedySum = 0;
        double starSum = 0;
        for (int i = 0; i < 50; i++)
        {
            rnd = new Random(i);
            testRandom(words, similarity, 10 + rnd.nextInt(100));
            // test2(words, similarity);

            cycleSum += getCyclesResult(words, similarity);
            greedySum += getCyclesResult(words, similarity, true);
            starSum += getStarsResult(words, similarity);
        }

        System.out.println("CycleSum  = " + cycleSum);
        System.out.println("GreedySum = " + greedySum);
        System.out.println("StarSum   = " + starSum);
    }

    private static void checkCyclesResult(List<Word> words, Map<WordPair, Double> similarity, double expectedValue)
    {
        double totalRealizedValue = getCyclesResult(words, similarity);
        // double totalRealizedValue = getStarsResult(words, similarity);

        System.out.println("====================");
        if (!close(totalRealizedValue, expectedValue) && totalRealizedValue < expectedValue)
            throw new RuntimeException("totalRealizedValue=" + totalRealizedValue + "  expectedValue=" + expectedValue);

        System.out.println("totalRealizedValue = " + totalRealizedValue);
    }

    private static double getCyclesResult(List<Word> words, Map<WordPair, Double> similarity)
    {
        return getCyclesResult(words, similarity, false);
    }

    private static double getCyclesResult(List<Word> words, Map<WordPair, Double> similarity, boolean useGreedy)
    {
        CycleCoverAlgo algorithm = new CycleCoverAlgo(words, similarity);
        algorithm.setUseGreedy(useGreedy);

        // Run it!
        algorithm.layout();
        return algorithm.getRealizedWeight();
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

        // Run it!
        starsAlgo.layout();
        return starsAlgo.getRealizedWeight();
    }

    static double test1(List<Word> words, Map<UnorderedPair<Word, Word>, Double> similarity)
    {
        Word a = new Word("Aaaa", 100.0);
        Word b = new Word("Bbbb", 200.0);

        words.clear();
        words.add(a);
        words.add(b);

        similarity.clear();
        similarity.put(new UnorderedPair(a, b), 10.0);

        return 10;
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

    static double testHamiltonian(List<Word> words, Map<WordPair, Double> similarity, int n)
    {
        words.clear();
        for (int i = 0; i < n; i++)
        {
            Word a = new Word(randomWord(5, 10), rnd.nextDouble() * 100);
            words.add(a);
        }

        double res = 0;
        similarity.clear();
        for (int i = 0; i < n; i++)
            for (int j = i + 1; j < n; j++)
            {
                Word a = words.get(i);
                Word b = words.get(j);

                double weight = rnd.nextDouble() * 10;
                if (j == i + 1 || (i == 0 && j == n - 1))
                {
                    weight *= 10;
                    res += weight;
                }

                similarity.put(new WordPair(a, b), weight);
            }

        return res;
    }

    static double testTwoCycles(List<Word> words, Map<WordPair, Double> similarity, int n)
    {
        words.clear();
        for (int i = 0; i < n; i++)
        {
            Word a = new Word(randomWord(5, 10), rnd.nextDouble() * 100);
            words.add(a);
        }

        int n1 = n / 2;
        double res = 0;
        similarity.clear();
        for (int i = 0; i < n; i++)
            for (int j = i + 1; j < n; j++)
            {
                Word a = words.get(i);
                Word b = words.get(j);

                double weight = rnd.nextDouble() * 10;
                if ((j == i + 1 && i != n1 - 1) || (i == 0 && j == n1 - 1) || (i == n1 && j == n - 1))
                {
                    weight *= 10;
                    res += weight;
                }

                similarity.put(new WordPair(a, b), weight);
            }

        return res;
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
