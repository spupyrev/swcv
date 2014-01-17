package edu.test;

import edu.cloudy.layout.LayoutAlgo;
import edu.cloudy.layout.PackAlgo;
import edu.cloudy.layout.packing.ClusterSpiralPlacer;
import edu.cloudy.nlp.Word;
import edu.cloudy.nlp.WordPair;
import edu.cloudy.ui.WordCloudFrame;
import edu.cloudy.utils.BoundingBoxGenerator;
import edu.cloudy.utils.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SpiralsTest
{
    public static void main(String[] args)
    {
        Logger.doLogging = false;

        List<Word> words = createCycle2();
        Map<WordPair, Double> similarity = new HashMap();
        MatchingTest.randomSimilarities(words, similarity);

        LayoutAlgo algo = runLayout(words, similarity);

        new WordCloudFrame(words, similarity, algo, null);
    }

    private static List<Word> createCycle()
    {
        List<Word> cycle = new ArrayList<Word>();
        cycle.add(new Word("1The", 100));
        cycle.add(new Word("2very", 100));
        cycle.add(new Word("3very-very", 200));
        cycle.add(new Word("4long", 200));
        cycle.add(new Word("5sentence", 500));
        cycle.add(new Word("6tr", 100));
        cycle.add(new Word("7abcdefg", 300));
        cycle.add(new Word("8One", 750));
        cycle.add(new Word("9more", 1400));
        cycle.add(new Word("10sentence", 800));
        cycle.add(new Word("11querty67", 100));
        cycle.add(new Word("12sghgfqtgAKM", 400));
        return cycle;
    }

    private static List<Word> createCycle2()
    {
        List<Word> cycle = new ArrayList<Word>();
        for (int i = 1; i < 25; i++)
        {
            cycle.add(new Word("the" + i, 100));
        }
        return cycle;
    }

    private static LayoutAlgo runLayout(List<Word> cycle, Map<WordPair, Double> similarity)
    {
        BoundingBoxGenerator bbGenerator = new BoundingBoxGenerator(25000.0);
        ClusterSpiralPlacer placer = new ClusterSpiralPlacer(cycle, bbGenerator);
        placer.run();

        LayoutAlgo algo = new PackAlgo(placer);
        algo.setConstraints(bbGenerator);
        algo.setData(cycle, similarity);
        algo.run();

        return algo;
    }

}
