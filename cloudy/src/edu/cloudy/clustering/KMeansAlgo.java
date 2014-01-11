package edu.cloudy.clustering;

import edu.cloudy.nlp.Word;
import edu.cloudy.nlp.WordPair;
import net.sf.javaml.clustering.FarthestFirst;
import net.sf.javaml.core.Dataset;
import net.sf.javaml.core.DefaultDataset;
import net.sf.javaml.core.DenseInstance;
import net.sf.javaml.core.Instance;
import net.sf.javaml.distance.AbstractSimilarity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author spupyrev
 * Nov 28, 2013
 */
public class KMeansAlgo implements IClusterAlgo {
	private int K;
	private Map<Word, Integer> clusters;

	public KMeansAlgo(int K) {
		this.K = K;
	}

	@Override
	public void run(List<Word> words, Map<WordPair, Double> similarity) {
		clusters = runInternal(words, similarity);
	}

	private Map<Word, Integer> runInternal(final List<Word> words, final Map<WordPair, Double> similarity) {
		assert (K >= 1);

		final Map<Integer, Integer> idToIndex = new HashMap<Integer, Integer>();
		Dataset data = new DefaultDataset();
		for (int i = 0; i < words.size(); i++) {
			double[] values = new double[] { i };
			Instance instance = new DenseInstance(values);
			data.add(instance);
			idToIndex.put(instance.getID(), i);
		}

		FarthestFirst km = new FarthestFirst(K, new AbstractSimilarity() {
			@Override
			public double measure(Instance arg0, Instance arg1) {
				int x = arg0.getID();
				int y = arg1.getID();
				if (x == y)
					return 0;

				Word wx = words.get(idToIndex.get(x));
				Word wy = words.get(idToIndex.get(y));
				double sim = similarity.get(new WordPair(wx, wy));
				//return 1.0 - similarity.get(new WordPair(wx, wy));
				double D = -Math.log((1.0 - 0.1) * sim + 0.1);
				return D;
			}
		});

		/*
		 * Cluster the data, it will be returned as an array of data sets, with
		 * each dataset representing a cluster.
		 */
		Dataset[] cls = km.cluster(data);

		Map<Word, Integer> result = new HashMap();
		for (int i = 0; i < cls.length; i++) {
			for (int j = 0; j < cls[i].size(); j++) {
				int index = idToIndex.get(cls[i].get(j).getID());
				result.put(words.get(index), i);
			}
		}

		return result;
	}

	@Override
	public int getCluster(Word word) {
		return clusters.get(word);
	}

	@Override
	public int getClusterNumber() {
		return K;
	}
}
