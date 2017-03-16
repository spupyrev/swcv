package edu.cloudy.layout;

import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.cloudy.nlp.ItemPair;

public class ItemGraph {
	
	private List<Rectangle2D.Double> items;
	private Map<ItemPair<Rectangle2D.Double>, Double> similarity;
	private Map<ItemPair<Rectangle2D.Double>, Double> distance;
	
	private ItemGraphCache cache;
	
	public ItemGraph(List<Rectangle2D.Double> items, Map<ItemPair<Rectangle2D.Double>, Double> similarity) {
		this.items = items;
		this.similarity = similarity;
		
		checkConsistency();
		
		initializeDistances();
		cache = new WordGraphCache(items, similarity, distance);
	}
	
	public List<Rectangle2D.Double> getItems() {
		return items;
	}
	
	public Map<ItemPair<Rectangle2D.Double>, Double> getSimilarity() {
		return similarity;
	}
	
	public double distance(Rectangle2D.Double r1, Rectangle2D.Double r2) {
		return distance.get(new ItemPair<Rectangle2D.Double>(r1, r2));
	}
	
	public double weightedDegree(Rectangle2D.Double r) {
		return cache.weightedDegree(r);
	}
	
	public double shortestPath(Rectangle2D.Double r1, Rectangle2D.Double r2) {
		return cache.shortestPath(r1, r2);
	}
	
	public Integer[] nonZeroAdjacency(Rectangle2D.Double r) {
		return cache.nonZeroAdjacency(r);
	}
	
	public Rectangle2D.Double[] convertWordsToArray() {
		return items.toArray(new Rectangle2D.Double[items.size()]);
	}
	
	public double[][] convertSimilarityToArray() {
		double[][] result = new double[items.size()][items.size()];
		for( int i = 0; i < items.size(); ++i )
			for( int j = 0; j < items.size(); ++j ) {
				ItemPair<Rectangle2D.Double> rp = new ItemPair<Rectangle2D.Double>(items.get(i), items.get(j));
				result[i][j] = similarity.get(rp);
			}
		
		return result;
	}
	
	private void initializeDistances() {
		distance = new HashMap<ItemPair<Rectangle2D.Double>, Double>();
		for( int i = 0; i < items.size(); ++i )
			for( int j = 0; j < items.size(); ++j ) {
				ItemPair<Rectangle2D.Double> rp = new ItemPair<Rectangle2D.Double>(items.get(i), items.get(j));
				double sim = similarity.get(rp);
				double dist = LayoutUtils.idealDistanceConverter(sim);
				distance.put(rp, dist);
			}
	}
	
	public void reorderWords(int startIndex) {
		int n = items.size();
		List<Rectangle2D.Double> path = new ArrayList<Rectangle2D.Double>();
		for( int i = 0; i < n; ++i )
			path.add(items.get((i + startIndex + 1) % n));
		
		for( int i = 0; i < n; ++i )
			items.set(i, path.get(i));		
	}
	
	private void checkConsistency() {
		for( int i = 0; i < items.size(); ++i ) {
			
			Rectangle2D.Double ri = items.get(i);
			ItemPair<Rectangle2D.Double> rp = new ItemPair<Rectangle2D.Double>(ri, ri);
			
			assert( similarity.containsKey(rp) && similarity.get(rp) == 1.0 );
			
			for( int j = 0; j < items.size(); ++j ) {
				Rectangle2D.Double rj = items.get(j);
				ItemPair<Rectangle2D.Double> rp2 = new ItemPair<Rectangle2D.Double>(ri, rj);
				assert( similarity.containsKey(rp2) );
				
				double sim = similarity.get(rp2);
				assert( 0 <= sim && sim <= 1.0);
			}
			
		}
	}

}
