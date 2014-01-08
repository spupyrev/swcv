package de.tinloaf.cloudy.algos.packing;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Observer;
import java.util.Set;

import de.tinloaf.cloudy.algos.LayoutAlgo;
import de.tinloaf.cloudy.algos.overlaps.ForceDirectedOverlapRemoval;
import de.tinloaf.cloudy.text.Word;
import de.tinloaf.cloudy.utils.BoundingBoxGenerator;
import de.tinloaf.cloudy.utils.Cluster;
import de.tinloaf.cloudy.utils.SWCPoint;
import de.tinloaf.cloudy.utils.SWCRectangle;
import de.tinloaf.cloudy.utils.StarExpander;
import de.tinloaf.cloudy.utils.WordPair;

public class ClusterSpiralPlacer implements ClusterWordPlacer {

	private List<Word> words;
	private Map<WordPair, Double> similarities;
	private Map<Word, SWCRectangle> wordPositions = new HashMap<Word, SWCRectangle>();
	private List<? extends LayoutAlgo> singlePlacers;

	private List<Cluster> clusters;

	boolean debug = false;
	boolean animated = true;

	private BoundingBoxGenerator bbGenerator;

	public ClusterSpiralPlacer(List<Word> words, Map<WordPair, Double> similarities, List<? extends LayoutAlgo> singlePlacers,
			BoundingBoxGenerator bbGenerator, boolean animated) {
		this.words = words;
		this.similarities = similarities;
		this.singlePlacers = singlePlacers;
		this.bbGenerator = bbGenerator;
		this.animated = animated;

		run();
	}

	@Override
	public SWCRectangle getRectangleForWord(Word w) {
		assert (wordPositions.containsKey(w));
		return wordPositions.get(w);
	}

	@Override
	public boolean contains(Word w) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Set<Word> getWords() {
		return wordPositions.keySet();
	}

	private void run() {
		//get the groups of words: stars, cycles etc
		clusters = createClusters();

		if (debug) {
			for (Cluster c : clusters) {
				System.out.println(c);
			}
		}

		// make the largest cluster come first
		Collections.sort(clusters, new Comparator<Cluster>() {
			public int compare(Cluster c1, Cluster c2) {
				return c2.wordPositions.size() - c1.wordPositions.size();
			}
		});

		boolean isFirstCluster = true;
		double spiralConstant = 1;
		List<Cluster> placedClusters = new LinkedList<Cluster>();
		for (Cluster c : clusters) {

			if (debug)
				System.out.println("on cluster: " + c);

			if (isFirstCluster) {
				c.center = new SWCPoint(0, 0);
				placedClusters.add(c);
				isFirstCluster = false;
				spiralConstant = 1;
			} else {
				c.center = new SWCPoint(0, 0);

				int spiralPosition = 0;

				while (c.overlap(placedClusters)) {
					spiralOut(c, spiralPosition, spiralConstant);
					spiralPosition++;
					//System.out.println(c.center);
				}
				spiralConstant += 3.0;
				placedClusters.add(c);
			}
		}

		//		initialPlacement();
		//
		//		runForceDirected();
		//
		restoreWordPositions();
		//		
		//		for(Cluster c : clusters){
		//			System.out.println(c.center);
		//		}
	}

	private static void spiralOut(Cluster c, int spiralValue, double constant) {
		SWCPoint p = c.center;

		SWCPoint newCenter;
		newCenter = new SWCPoint(p.x() + constant * Math.sqrt(spiralValue) * Math.cos(spiralValue), p.y() + constant * Math.sqrt(spiralValue)
				* Math.sin(spiralValue));

		c.center = newCenter;

	}

	private List<Cluster> createClusters() {
		List<Cluster> result = new ArrayList<Cluster>();
		for (int i = 0; i < singlePlacers.size(); i++)
			result.add(new Cluster());

		for (Word w : words) {
			SWCRectangle rect = null;
			for (int i = 0; i < singlePlacers.size(); i++) {
				SWCRectangle tmp = singlePlacers.get(i).getWordRectangle(w);
				if (tmp != null) {
					result.get(i).wordPositions.put(w, tmp);
					rect = tmp;
					break;
				}
			}

			//create its own cluster
			if (rect == null) {
				Cluster c = new Cluster();
				c.wordPositions.put(w, bbGenerator.getBoundingBox(w, w.weight));
				result.add(c);
			}
		}

		return result;
	}

	private void restoreWordPositions() {
		for (Cluster c : clusters)
			for (Word w : c.wordPositions.keySet())
				wordPositions.put(w, c.actualWordPosition(w));

		new ForceDirectedOverlapRemoval<SWCRectangle>().run(words, wordPositions);
		if (!animated)
			expandStars(null);
	}

	public List<Cluster> getClusters() {
		return this.clusters;
	}

	public void expandStars(Observer obs) {
		StarExpander se = new StarExpander(clusters, wordPositions, words, true);
		if (obs != null) {
			se.addObserver(obs);
		}
		se.expandStars();
	}

	public Map<Word, SWCRectangle> getWordPositions() {
		return this.wordPositions;
	}
}
