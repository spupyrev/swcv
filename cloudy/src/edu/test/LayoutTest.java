package edu.test;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JPanel;

import edu.cloudy.colors.DynamicColorScheme;
import edu.cloudy.layout.ContextPreservingAlgo;
import edu.cloudy.layout.InflateAndPushAlgo;
import edu.cloudy.layout.LayoutAlgo;
import edu.cloudy.layout.SinglePathAlgo;
import edu.cloudy.nlp.WCVDocument;
import edu.cloudy.nlp.WCVDynamicDocument;
import edu.cloudy.nlp.Word;
import edu.cloudy.nlp.WordPair;
import edu.cloudy.nlp.ranking.TFRankingAlgo;
import edu.cloudy.ui.WordCloudPanel;
import edu.cloudy.utils.BoundingBoxGenerator;
import edu.cloudy.utils.SWCRectangle;

public class LayoutTest
{

	public static void main(String[] args)
	{
		WCVDocument doc = new WCVDynamicDocument("meow", "meow");
		doc.parse();
		doc.weightFilter(100, new TFRankingAlgo());

		Map<WordPair, Double> similarity = new HashMap<WordPair, Double>();
		MatchingTest.randomSimilarities(doc.getWords(), similarity);
		BoundingBoxGenerator bbg = new BoundingBoxGenerator(1.0);
		LayoutAlgo algo = runLayout(doc.getWords(), similarity, bbg);

		Word w = doc.getWords().get(0);// two meows share 1 rect
		SWCRectangle rect = algo.getWordRectangle(w);
		SWCRectangle rect2 = bbg.getBoundingBox(w, w.weight);
		System.out.println("word: " + w.word);
		System.out.println(rect); // this is the size before transformation in WordCloudPanel panel
		System.out.println(rect2);
		/*
		WordCloudPanel panel = new WordCloudPanel(doc.getWords(), algo, null, new DynamicColorScheme("REDBLUEBLACK"), bbg);
		BufferedImage dummy = new BufferedImage(1000, 1000, BufferedImage.TYPE_3BYTE_BGR);
		panel.paintComponent(dummy.getGraphics());
		*/
	}

	private static LayoutAlgo runLayout(List<Word> words, Map<WordPair, Double> similarity, BoundingBoxGenerator bbg)
	{
		LayoutAlgo algo;
		//algo = new ContextPreservingAlgo();
		algo = new InflateAndPushAlgo();
		algo.setConstraints(bbg);
		algo.setData(words, similarity);
		algo.run();
		return algo;
	}

}
