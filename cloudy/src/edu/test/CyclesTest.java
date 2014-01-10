package edu.test;

import edu.cloudy.layout.LayoutAlgo;
import edu.cloudy.layout.SinglePathAlgo;
import edu.cloudy.nlp.Word;
import edu.cloudy.nlp.WordPair;
import edu.cloudy.ui.WordCloudFrame;
import edu.cloudy.utils.BoundingBoxGenerator;
import edu.cloudy.utils.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CyclesTest {
	public static void main(String[] args) {
		Logger.doLogging = false;

		List<Word> words = createCycle();
		//List<Word> words = MatchingTest.();
		Map<WordPair, Double> similarity = new HashMap();
		MatchingTest.randomSimilarities(words, similarity);

		LayoutAlgo algo = runLayout(words, similarity);

		new WordCloudFrame(words, similarity, algo, null);
	}

	private static List<Word> createCycle() {
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

	private static LayoutAlgo runLayout(List<Word> cycle, Map<WordPair, Double> similarity) {
		LayoutAlgo algo = new SinglePathAlgo();
		//LayoutAlgo algo = new CycleCoverAlgo();
		algo.setConstraints(new BoundingBoxGenerator(25000.0));
		algo.setData(cycle, similarity);

		algo.run();
		return algo;
	}

}
