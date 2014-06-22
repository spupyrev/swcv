package edu.cloudy.metrics;

import edu.cloudy.layout.LayoutAlgo;
import edu.cloudy.nlp.Word;
import edu.cloudy.nlp.WordPair;
import edu.cloudy.utils.SWCPoint;
import edu.cloudy.utils.SWCRectangle;

import java.util.List;
import java.util.Map;

/**
 * May 3, 2013
 * computes average stress
 */
public class StressMetric implements QualityMetric {

	@Override
	public double getValue(List<Word> words, Map<WordPair, Double> similarity, LayoutAlgo algo) {
		if (words.isEmpty())
			return 0;

		double[][] matrixDissimilarity = getDissimilarityMatrix(words, similarity, algo);
		double[][] matrixGeomDistance = getGeomDistance(words, similarity, algo);

		double dist = computeStress(matrixDissimilarity, matrixGeomDistance);
		return Math.max(0, 1 - dist);
	}

	private double computeStress(double[][] A, double[][] B) {
		assert (A.length == B.length);
		int n = A.length;

		double avgA = average(A);
		double avgB = average(B);

		assert (avgA > 0.0 && avgB > 0.0);

		double stress = 0, cnt = 0;
		for (int i = 0; i < n; i++)
			for (int j = 0; j < n; j++) {
				if (i == j)
					continue;

				if (A[i][j] == -1 || B[i][j] == -1)
					continue;

				double delta = A[i][j] / avgA - B[i][j] / avgB;

				stress += delta * delta;
				assert (!Double.isNaN(stress));
				cnt++;
			}

		return stress / cnt;
	}

	private double average(double[][] A) {
		double sum = 0;
		double cnt = 0;

		for (int i = 0; i < A.length; i++)
			for (int j = 0; j < A[i].length; j++) {
				if (A[i][j] == -1)
					continue;
				sum += A[i][j];
				cnt++;
			}

		if (cnt > 0)
			sum /= cnt;
		return sum;
	}

	private double[][] getDissimilarityMatrix(List<Word> words, Map<WordPair, Double> similarity, LayoutAlgo algo) {
		int n = words.size();

		double[][] matrix = new double[n][n];
		for (int i = 0; i < n; i++)
			for (int j = 0; j < n; j++) {
				WordPair wp = new WordPair(words.get(i), words.get(j));
				if (similarity.containsKey(wp))
					matrix[i][j] = 1.0 - similarity.get(wp);
				else
					matrix[i][j] = -1;
			}

		return matrix;
	}

	private double[][] getGeomDistance(List<Word> words, Map<WordPair, Double> similarity, LayoutAlgo algo) {
		int n = words.size();

		double[][] matrix = new double[n][n];
		for (int i = 0; i < n; i++)
			for (int j = 0; j < n; j++) {
				matrix[i][j] = distance(words.get(i), words.get(j), algo);
			}

		return matrix;
	}

	private double distance(Word first, Word second, LayoutAlgo algo) {
		SWCRectangle rect1 = algo.getWordPosition(first);
		SWCRectangle rect2 = algo.getWordPosition(second);

		double dist = Double.POSITIVE_INFINITY;
		SWCPoint[] corners1 = getCorners(rect1);
		SWCPoint[] corners2 = getCorners(rect2);
		for (SWCPoint p1 : corners1)
			for (SWCPoint p2 : corners2)
				dist = Math.min(dist, p1.distance(p2));

		return dist;
	}

	private SWCPoint[] getCorners(SWCRectangle rect) {
		SWCPoint[] res = new SWCPoint[4];
		res[0] = new SWCPoint(rect.getMinX(), rect.getMinY());
		res[1] = new SWCPoint(rect.getMaxX(), rect.getMinY());
		res[2] = new SWCPoint(rect.getMinX(), rect.getMaxY());
		res[3] = new SWCPoint(rect.getMaxX(), rect.getMaxY());
		return res;
	}

}
