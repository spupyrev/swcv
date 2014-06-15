package edu.test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
		BoundingBoxGenerator bbg = new BoundingBoxGenerator(25000.0);
		LayoutAlgo algo = runLayout(doc.getWords(), similarity, bbg);

		Word w = doc.getWords().get(0);// two meows share 1 rect
		SWCRectangle rect = algo.getWordRectangle(w);

		System.out.println("word: " + w.word);
		System.out.println(rect); // this is the size before transformation in WordCloudPanel panel

		// use same step in WordCloudPanel to rescale each word.
		// assume a panel size is 1024 * 800 offset = 50
		double max_width = 1024;
		double max_height = 800;
		double offset = 50;
		double scale;
		
		double maxX = rect.getMaxX();
		double minX = rect.getMinX();
		double maxY = rect.getMaxY();
		double minY = rect.getMinY();
		
		double panel_width = max_width - offset * 2;
		double panel_height = max_height - offset * 2;
		
		scale = panel_width / (maxX - minX);
		scale = Math.min(scale, panel_height / (maxY - minY));
		
		System.out.println("scale: "+scale);
		
		double shiftX = -1 * minX + offset / scale;
		double shiftY = -1 * minY + offset / scale;
		System.out.println("sx: "+shiftX);
		System.out.println("sy: "+shiftY);
		
		double finalX = scale * (shiftX + rect.getX());
		double finalY = scale * (shiftY + rect.getY());
		double finalW = scale * rect.getWidth();
		double finalH = scale * rect.getHeight();
		System.out.println("x:"+finalX);
		System.out.println("y:"+finalY);
		System.out.println("width:"+finalW);
		System.out.println("height:"+finalH);
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
