package de.tinloaf.cloudy.similarity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.tinloaf.cloudy.text.WCVDocument;
import de.tinloaf.cloudy.text.Word;
import de.tinloaf.cloudy.utils.WordPair;
import edu.cmu.lti.lexical_db.ILexicalDatabase;
import edu.cmu.lti.lexical_db.NictWordNet;
import edu.cmu.lti.ws4j.RelatednessCalculator;
import edu.cmu.lti.ws4j.impl.HirstStOnge;
import edu.cmu.lti.ws4j.impl.JiangConrath;
import edu.cmu.lti.ws4j.impl.LeacockChodorow;
import edu.cmu.lti.ws4j.impl.Lesk;
import edu.cmu.lti.ws4j.impl.Lin;
import edu.cmu.lti.ws4j.impl.Path;
import edu.cmu.lti.ws4j.impl.Resnik;
import edu.cmu.lti.ws4j.impl.WuPalmer;

public class LexicalSimilarityAlgo implements SimilarityAlgo {
	private Map<WordPair, Double> similarity;
	private WCVDocument wordifier;
	private static ILexicalDatabase db = new NictWordNet();
	private static RelatednessCalculator rc = new Lin(db);
	private static RelatednessCalculator[] rcs = { new HirstStOnge(db), new LeacockChodorow(db), new Lesk(db), new WuPalmer(db), new Resnik(db),
			new JiangConrath(db), new Lin(db), new Path(db) };
	//private final static double SIMILARITY_THRESHOLD = 0.000001;


	public static void main(String[] args) {
		for (RelatednessCalculator rcc : rcs) {
			long t0 = System.currentTimeMillis();
			double s = rcc.calcRelatednessOfWords("University", "arizona");
			System.out.println(rcc.getClass().getName() + "\t" + s);
			long t1 = System.currentTimeMillis();
			System.out.println("Done in " + (t1 - t0) + " msec.\n\n\n");
		}
	}

	@Override
	public void initialize(WCVDocument wordifier) {
		this.wordifier = wordifier;
		this.similarity = null;
	}

	@Override
	public void run() {
		List<Word> words = wordifier.getWords();
		similarity = new HashMap<WordPair, Double>();
		for (Word x : words) {
			for (Word y : words) {
				if (x.stem.equals(y.stem) )
					continue;
				WordPair xyPair = new WordPair(x, y);
				
				double xySimilarity = rc.calcRelatednessOfWords(x.word, y.word);
				if(xySimilarity<=0)
					xySimilarity=0.0;
				similarity.put(xyPair, xySimilarity);

			}
		}
	}

	@Override
	public Map<WordPair, Double> getSimilarity() {
		return this.similarity;

	}

}
