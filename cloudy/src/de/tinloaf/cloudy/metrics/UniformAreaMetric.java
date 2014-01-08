package de.tinloaf.cloudy.metrics;

import de.tinloaf.cloudy.algos.LayoutAlgo;
import de.tinloaf.cloudy.text.Word;
import de.tinloaf.cloudy.utils.SWCRectangle;
import de.tinloaf.cloudy.utils.WordPair;

import java.util.List;
import java.util.Map;

/**
 * @author spupyrev
 * May 16, 2013
 * 
 * how uniformly area is used
 * using Pearson's chi-squared test
 */
public class UniformAreaMetric implements QualityMetric {

	@Override
	public double getValue(List<Word> words, Map<WordPair, Double> similarity, LayoutAlgo algo) {
		SWCRectangle bb = SpaceMetric.computeBoundingBox(words, algo);

		//number of cells
		int W = (int) Math.sqrt(words.size() + 1.0);
		int H = (int) Math.sqrt(words.size() + 1.0);

		double entropy = 0;
		for (int i = 0; i < W; i++)
			for (int j = 0; j < H; j++) {
				double cellWidth = bb.getWidth() / W;
				double cellHeight = bb.getHeight() / H;
				SWCRectangle cell = new SWCRectangle(bb.getX() + cellWidth * i, bb.getY() + cellHeight * j, cellWidth, cellHeight);

				//observed frequency
				double O = computeWordsInsideCell(cell, words, algo);
				//expected frequency
				double E = (double) words.size() / (W * H);

				double p = O / words.size();
				double q = E / words.size();
				if (Math.abs(p) < 0.0001)
					continue;
				
				entropy += p * Math.log(p / q);
			}

		double maxEntropy = Math.log(words.size());

		return 1.0 - entropy / maxEntropy;
	}

	public double getValueCur(List<Word> words, Map<WordPair, Double> similarity, LayoutAlgo algo) {
		SWCRectangle bb = SpaceMetric.computeBoundingBox(words, algo);

		//number of cells
		int W = 10, H = 10;

		double cellArea = bb.getWidth() * bb.getHeight() / (W * H);
		double avgArea = 0;

		for (Word w : words) {
			SWCRectangle rect = algo.getWordRectangle(w);
			avgArea += rect.getWidth() * rect.getHeight();
		}
		avgArea /= words.size();
		double res = 3.0 * cellArea / avgArea;

		double chi2 = 0, cnt = 0;
		for (int i = 0; i < W; i++)
			for (int j = 0; j < H; j++) {
				double cellWidth = bb.getWidth() / W;
				double cellHeight = bb.getHeight() / H;
				SWCRectangle cell = new SWCRectangle(bb.getX() + cellWidth * i, bb.getY() + cellHeight * j, cellWidth, cellHeight);

				//observed frequency
				double O = computeWordsInsideCell(cell, words, algo);
				//expected frequency
				double E = (double) words.size() / (W * H);

				chi2 += Math.abs((O - E) * (O - E)) / (res);
				cnt++;
			}

		return 1.0 - Math.sqrt(chi2 / cnt);
	}

	public double getValue2(List<Word> words, Map<WordPair, Double> similarity, LayoutAlgo algo) {
		SWCRectangle bb = SpaceMetric.computeBoundingBox(words, algo);

		double totalArea = bb.getWidth() * bb.getHeight();
		double usedArea = SpaceMetric.computeUsedArea(words, algo);

		//number of cells
		int W = 10, H = 10;

		double chi2 = 0, cnt = 0;
		for (int i = 0; i < W; i++)
			for (int j = 0; j < H; j++) {
				double cellWidth = bb.getWidth() / W;
				double cellHeight = bb.getHeight() / H;
				SWCRectangle cell = new SWCRectangle(bb.getX() + cellWidth * i, bb.getY() + cellHeight * j, cellWidth, cellHeight);

				double areaOfCell = cellWidth * cellHeight;
				double usedAreaOfCell = computeUsedAreaOfCell(cell, words, algo);

				//observed frequency
				double O = usedAreaOfCell / areaOfCell;
				//expected frequency
				double E = usedArea / totalArea;

				double delta = Math.abs(O - E);
				chi2 += delta * delta;
				cnt++;
			}

		return 1.0 - Math.sqrt(chi2 / cnt);
	}

	private double computeUsedAreaOfCell(SWCRectangle cell, List<Word> words, LayoutAlgo algo) {
		double res = 0;
		for (Word w : words) {
			SWCRectangle rect = algo.getWordRectangle(w);
			if (!rect.intersects(cell))
				continue;

			SWCRectangle intersection = rect.createIntersection(cell);
			res += intersection.getHeight() * intersection.getWidth();
		}

		/*if (res - 1 > cell.getWidth() * cell.getHeight()) {
			System.out.println(res);
			System.out.println(cell.getWidth() * cell.getHeight());
		}*/
		//assert (res + 1 <= cell.getWidth() * cell.getHeight());
		return res;
	}

	private double computeWordsInsideCell(SWCRectangle cell, List<Word> words, LayoutAlgo algo) {
		double res = 0;
		for (Word w : words) {
			SWCRectangle rect = algo.getWordRectangle(w);
			if (cell.contains(rect.getCenterX(), rect.getCenterY()))
				res++;
		}

		return res;
	}

}
