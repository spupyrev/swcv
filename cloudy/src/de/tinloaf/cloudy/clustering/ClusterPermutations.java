package de.tinloaf.cloudy.clustering;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.tinloaf.cloudy.algos.SingleStarAlgo;
import de.tinloaf.cloudy.graph.Edge;
import de.tinloaf.cloudy.graph.Vertex;
import de.tinloaf.cloudy.graph.WordGraph;
import de.tinloaf.cloudy.text.Word;
import de.tinloaf.cloudy.utils.BoundingBoxGenerator;
import de.tinloaf.cloudy.utils.Cluster;
import de.tinloaf.cloudy.utils.SWCPoint;
import de.tinloaf.cloudy.utils.SWCRectangle;
import de.tinloaf.cloudy.utils.WordPair;

public class ClusterPermutations {
	
	private List<Word> words;
	private Map<WordPair, Double> similarity;
	private BoundingBoxGenerator bbGenerator;
	private Set<Cluster> clusters;
	
	
	public ClusterPermutations(List<Word> words, Map<WordPair, Double> similarity, BoundingBoxGenerator bb){
		this.words = words;
		this.similarity = similarity;
		this.bbGenerator = bb;
		makeClusters();
	}
	
	// each List<Cluster> contains all words
	// each List<Cluster> has all those words layed out in different orientations
	public Set<Cluster> getClusters(){
		return clusters;	
	}
	
	private void makeClusters(){
		clusters = new HashSet<Cluster>();
		
		int num_clusters = words.size()/5;  // ideal if each cluster has 5 words, not even likely
		KMeansAlgo kmeans = new KMeansAlgo(num_clusters);
		kmeans.run(words, similarity);
		
		// since I have no fucking clue what the possible cluster numbers are, I have
		// to go looking for them.
		
		// its either 1 to num_clusters or 0 to num_clusters-1, but given this project's history
		// I am not comfortable assuming either one
		int smallest_cluster_number = num_clusters;
		int largest_cluster_number = 0;		
		for(Word w : words){
			int cluster_num = kmeans.getCluster(w);
			if(cluster_num < smallest_cluster_number)
				smallest_cluster_number = cluster_num;
			if(cluster_num > largest_cluster_number)
				largest_cluster_number = cluster_num;
		}
			
		// actually make the list of clusters.  very brute force and very ugly
		for(int i = smallest_cluster_number; i <= largest_cluster_number; i++){
			Cluster c = new Cluster();
			for(Word w : words){
				int cluster_num = kmeans.getCluster(w);
				if(cluster_num == i){
					c.wordPositions.put(w, bbGenerator.getBoundingBox(w, w.weight));
				}
			}			
			clusters.add(c);
		}		
		
	}
	
	public Set<Cluster> getLayoutsForSingleCluster(Cluster c){
		
	
		Set<Cluster> permutations = new HashSet<Cluster>();
		
		ArrayList<Word> clusterWords = new ArrayList<Word>(c.wordPositions.keySet());
		Map<WordPair, Double> clusterSimilarity = new HashMap<WordPair, Double>();
		
		// make a similarity object just for this cluster
		for(Word w1 : clusterWords){
			for(Word w2 : clusterWords){
				if(w1.equals(w2))
					break;

				WordPair wp = new WordPair(w1,w2);
				if(clusterSimilarity.containsKey(wp))
					break;
				else
					clusterSimilarity.put(wp, similarity.get(wp));				
			}
		}
		WordGraph g = new WordGraph(clusterWords, clusterSimilarity);

		ArrayList<Vertex> vertices = new ArrayList<Vertex>(g.vertexSet());		

		for(Vertex center : vertices){
			
			SingleStarAlgo ssa = new SingleStarAlgo();
			ssa.setConstraints(bbGenerator);
			ssa.setData(clusterWords, clusterSimilarity);
			
			// pass a new hash set since we don't care about usedVertices, but too lazy to change it
			WordGraph star = createStar(center, new HashSet<Vertex>(), g);

			ssa.setGraph(star);
			ssa.run();
			
			Cluster permutation = new Cluster();
			for(Word w : clusterWords){
				SWCRectangle box = ssa.getWordRectangle(w);
				permutation.wordPositions.put(w, box);
			}
			permutation.center = new SWCPoint(0,0);
			
			permutations.add(permutation);
			
		}
		
		return permutations;	
		
	}
	
	private WordGraph createStar(Vertex center, Set<Vertex> usedVertices, WordGraph g) {
		List<Word> words = new ArrayList<Word>();
		for (Vertex v : g.vertexSet())
			if (!usedVertices.contains(v))
				words.add(v);

		Map<WordPair, Double> weights = new HashMap<WordPair, Double>();
		for (Vertex v : g.vertexSet()) {
			if (center.equals(v))
				continue;
			if (usedVertices.contains(v))
				continue;
			if (!g.containsEdge(center, v))
				continue;

			WordPair wp = new WordPair(center, v);
			Edge edge = g.getEdge(center, v);
			weights.put(wp, g.getEdgeWeight(edge));
		}

		return new WordGraph(words, weights);
	}
	
	
}
