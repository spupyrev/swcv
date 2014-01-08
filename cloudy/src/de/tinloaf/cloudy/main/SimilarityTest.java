package de.tinloaf.cloudy.main;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import de.tinloaf.cloudy.similarity.CosineCoOccurenceAlgo;
import de.tinloaf.cloudy.similarity.DicecoefficientAlgo;
import de.tinloaf.cloudy.similarity.EuclideanAlgo;
import de.tinloaf.cloudy.similarity.JaccardCoOccurenceAlgo;
import de.tinloaf.cloudy.similarity.LexicalSimilarityAlgo;
import de.tinloaf.cloudy.similarity.SimilarityAlgo;
import de.tinloaf.cloudy.similarity.TFIDFRankingAlgo;
import de.tinloaf.cloudy.text.WCVDocument;
import de.tinloaf.cloudy.text.Word;
import de.tinloaf.cloudy.utils.WikipediaXMLReader;
import de.tinloaf.cloudy.utils.WordPair;

public class SimilarityTest {

	public static void main(String[] args) {

		WCVDocument document = readDoc();

		// 2. build similarities, words etc
		List<Word> words = new ArrayList<Word>();
		Map<WordPair, Double> similarity = new HashMap<WordPair, Double>();
		extractSimilarities(document, words, similarity);
	}

	public static WCVDocument readDoc() {
		List<WCVDocument> alldocs = ALENEXPaperEvalulator.readDocuments(ALENEXPaperEvalulator.FILES_WIKI);

		WikipediaXMLReader xmlReader = new WikipediaXMLReader("data/turing");
		xmlReader.read();
		Iterator<String> texts = xmlReader.getTexts();

		WCVDocument doc = null;
		while (texts.hasNext()) {
			doc = new WCVDocument(texts.next());
			doc.parse();

			alldocs.add(doc);
		}
		System.out.println("#words: " + doc.getWords().size());
		doc.weightFilter(30, new TFIDFRankingAlgo());
		return doc;
	}

	private static void extractSimilarities(WCVDocument wordifier, List<Word> words, final Map<WordPair, Double> similarity) {
		SimilarityAlgo[] coOccurenceAlgoArray = {new LexicalSimilarityAlgo(),new CosineCoOccurenceAlgo(), new JaccardCoOccurenceAlgo(),new EuclideanAlgo(),new DicecoefficientAlgo()};
		//SimilarityAlgo CosineCoOccurenceAlgo2 = new LexicalSimilarityAlgo();
		
		//SimilarityAlgo coOccurenceAlgo3 = new JaccardCoOccurenceAlgo();
		//SimilarityAlgo coOccurenceAlgo4 = new RandomSimilarityAlgo();
		for(SimilarityAlgo coOccurenceAlgo: coOccurenceAlgoArray){
		coOccurenceAlgo.initialize(wordifier);
		coOccurenceAlgo.run();
		Map<WordPair, Double> sim = coOccurenceAlgo.getSimilarity();

		for (Word w : wordifier.getWords())
			words.add(w);

		List<WordPair> topPairs = new ArrayList<WordPair>();
		for (WordPair wp : sim.keySet()) {
			similarity.put(wp, sim.get(wp));
			topPairs.add(wp);
		}

		Collections.sort(topPairs, new Comparator<WordPair>() {
			@Override
			public int compare(WordPair o1, WordPair o2) {
				return similarity.get(o2).compareTo(similarity.get(o1));
			}
		});

		System.out.println("top pairs of " +coOccurenceAlgo.getClass().getName()+":");
		for (int i = 0; i <topPairs.size() ; i++) {
			WordPair wp = topPairs.get(i);
			System.out.println(wp.getFirst().word + " " + wp.getSecond().word + "  " + similarity.get(wp));
		}
		System.out.println("\n\n\n\n");
		}
	}

}
