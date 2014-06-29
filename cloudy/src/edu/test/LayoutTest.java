package edu.test;

import edu.cloudy.layout.CycleCoverAlgo;
import edu.cloudy.layout.InflateAndPushAlgo;
import edu.cloudy.layout.LayoutAlgo;
import edu.cloudy.nlp.WCVDynamicDocument;
import edu.cloudy.nlp.Word;
import edu.cloudy.nlp.WordPair;
import edu.cloudy.nlp.ranking.TFRankingAlgo;
import edu.cloudy.utils.BoundingBoxGenerator;
import edu.cloudy.utils.SWCRectangle;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class LayoutTest
{

	public static void main(String[] args)
	{
		String text1 = "";
		String text2 = "";
		try
		{
			Scanner scan = new Scanner(new File("data/wiki_cs.txt"));
			Scanner scan2 = new Scanner(new File("data/wiki_tc.txt"));
			while (scan.hasNextLine())
				text1 += scan.nextLine();
			while (scan2.hasNextLine())
				text2 += scan2.nextLine();
			
            scan.close();
            scan2.close();
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
			System.exit(1);
		}

		WCVDynamicDocument doc = new WCVDynamicDocument(text1, text2);
		doc.parse();
		doc.weightFilter(100, new TFRankingAlgo());

		Map<WordPair, Double> similarity = new HashMap<WordPair, Double>();
		MatchingTest.randomSimilarities(doc.getWords(), similarity);
		BoundingBoxGenerator bbg = new BoundingBoxGenerator(1.0);
		LayoutAlgo algo = runLayout(doc.getWords(), similarity, bbg);

	}

	private static LayoutAlgo runLayout(List<Word> words, Map<WordPair, Double> similarity, BoundingBoxGenerator bbg)
	{
		LayoutAlgo algo = new CycleCoverAlgo(words, similarity);
		algo.run();
		return algo;
	}

}
