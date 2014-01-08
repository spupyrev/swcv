package de.tinloaf.cloudy.main;

import de.tinloaf.cloudy.algos.LayoutAlgo;
import de.tinloaf.cloudy.algos.StarForestAlgoNew;
import de.tinloaf.cloudy.clustering.IClusterAlgo;
import de.tinloaf.cloudy.clustering.KMeansAlgo;
import de.tinloaf.cloudy.similarity.CosineCoOccurenceAlgo;
import de.tinloaf.cloudy.similarity.EuclideanAlgo;
import de.tinloaf.cloudy.similarity.JaccardCoOccurenceAlgo;
import de.tinloaf.cloudy.similarity.LexicalSimilarityAlgo;
import de.tinloaf.cloudy.similarity.SimilarityAlgo;
import de.tinloaf.cloudy.similarity.TFRankingAlgo;
import de.tinloaf.cloudy.text.WCVDocument;
import de.tinloaf.cloudy.text.Word;
import de.tinloaf.cloudy.ui.WordCloudFrame;
import de.tinloaf.cloudy.utils.BoundingBoxGenerator;
import de.tinloaf.cloudy.utils.Logger;
import de.tinloaf.cloudy.utils.SWCPoint;
import de.tinloaf.cloudy.utils.WikipediaXMLReader;
import de.tinloaf.cloudy.utils.WordPair;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @author spupyrev 
 * Apr 30, 2013 
 * create and visualize a wordcloud for a document
 */
public class WCVisualizer {

	public static void main(String argc[]) {
		Logger.doLogging = false;
		new WCVisualizer().run();
	}

	private void run() {
		// 1. read a document
		WCVDocument document = readDocument();

		// 2. build similarities, words etc
		List<Word> words = new ArrayList<Word>();
		Map<WordPair, Double> similarity = new HashMap<WordPair, Double>();
		extractSimilarities(document, words, similarity);

		// 3. run a layout algorithm
		LayoutAlgo algo = runLayout(words, similarity);
		IClusterAlgo clusterAlgo = runClustering(words, similarity);

		// 4. visualize it
		visualize(words, similarity, algo, clusterAlgo);
	}

	private WCVDocument readDocument() {
		//List<WCVDocument> alldocs = ALENEXPaperEvalulator.readDocuments(ALENEXPaperEvalulator.FILES_WIKI);

		WikipediaXMLReader xmlReader = new WikipediaXMLReader("data/test_wiki");
		xmlReader.read();
		Iterator<String> texts = xmlReader.getTexts();

		WCVDocument doc = null;
		while (texts.hasNext()) {
			doc = new WCVDocument(texts.next());
			doc.parse();

			//alldocs.add(doc);
		}

		System.out.println("#words: " + doc.getWords().size());
		//doc.weightFilter(15, new TFIDFRankingAlgo());
		doc.weightFilter(30, new TFRankingAlgo());
		//doc.weightFilter(50, new TFRankingAlgo());
		//doc.weightFilter(15, new LexRankingAlgo());

		return doc;
	}

	private WCVDocument readPDFDocument() {
		/*PDFReader reader = new PDFReader("file:///E:/Research/Arizona/wordle/tex-apprx/clouds.pdf");
		assert (reader.isConnected());
		WCVDocument doc = new WCVDocument(reader.getText());
		doc.parse();

		System.out.println("#words: " + doc.getWords().size());
		doc.weightFilter(85, new LexRankingAlgo());

		return doc;*/
		return null;
	}

	private void extractSimilarities(WCVDocument wordifier, List<Word> words, final Map<WordPair, Double> similarity) {
		//SimilarityAlgo[] coOccurenceAlgo111 = {new LexicalSimilarityAlgo(),new CosineCoOccurenceAlgo(), new JaccardCoOccurenceAlgo()};
		//SimilarityAlgo coOccurenceAlgo = new LexicalSimilarityAlgo();
		SimilarityAlgo coOccurenceAlgo = new EuclideanAlgo();

		//SimilarityAlgo coOccurenceAlgo = new JaccardCoOccurenceAlgo();
		//SimilarityAlgo coOccurenceAlgo = new CosineCoOccurenceAlgo();
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

		/*System.out.println("top words:");
		for (int i = 0; i < words.size(); i++) {
			Word w = words.get(i);
			System.out.println(w.word + " (" + w.stem + ")  " + w.weight);
		}

		System.out.println("===================");
		System.out.println("top pairs:");
		for (int i = 0; i < 20; i++) {
			WordPair wp = topPairs.get(i);
			System.out.println(wp.getFirst().word + " " + wp.getSecond().word + "  " + similarity.get(wp));
		}*/
	}

	private void extractSimilaritiesTest(WCVDocument wordifier, List<Word> words, Map<WordPair, Double> similarity) {
		for (Word w : wordifier.getWords())
			words.add(w);

		SWCPoint[] p = new SWCPoint[words.size()];
		for (int i = 0; i < words.size(); i++) {
			p[i] = SWCPoint.random();
		}

		for (int i = 0; i < words.size(); i++) {
			for (int j = i + 1; j < words.size(); j++) {
				double len = p[i].distance(p[j]);
				similarity.put(new WordPair(words.get(i), words.get(j)), 1.0 - len);
			}
		}
	}

	private LayoutAlgo runLayout(List<Word> words, Map<WordPair, Double> similarity) {
		//LayoutAlgo algo = new ContextPreservingAlgo();
		//LayoutAlgo algo = new InflateAndPushAlgo();
		//LayoutAlgo algo = new PathLayerAlgo(300.0, 150.0);
		//LayoutAlgo algo = new MDSAlgo();
		//LayoutAlgo algo = new StarForestAlgo();
		LayoutAlgo algo = new StarForestAlgoNew();
		//LayoutAlgo algo = new CycleCoverAlgo();
		//LayoutAlgo algo = new SeamCarvingAlgo();
		//LayoutAlgo algo = new WordleAlgo();
		algo.setData(words, similarity);
		algo.setConstraints(new BoundingBoxGenerator(25000.0));

		long startTime = System.currentTimeMillis();
		algo.run();
		System.out.printf("text processing done in %.3f sec\n", (System.currentTimeMillis() - startTime) / 1000.0);

		return algo;
	}

	private IClusterAlgo runClustering(List<Word> words, Map<WordPair, Double> similarity) {
		IClusterAlgo algo = new KMeansAlgo(4);

		long startTime = System.currentTimeMillis();
		algo.run(words, similarity);
		System.out.printf("clustering done in %.3f sec\n", (System.currentTimeMillis() - startTime) / 1000.0);

		return algo;
	}

	private void visualize(List<Word> words, Map<WordPair, Double> similarity, LayoutAlgo algo, IClusterAlgo clusterAlgo) {
		new WordCloudFrame(words, similarity, algo, clusterAlgo);
	}

}
