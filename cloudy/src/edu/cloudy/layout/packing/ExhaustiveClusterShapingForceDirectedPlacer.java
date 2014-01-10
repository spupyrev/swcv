package edu.cloudy.layout.packing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Observer;
import java.util.Set;

import edu.cloudy.clustering.ClusterPermutations;
import edu.cloudy.layout.LayoutAlgo;
import edu.cloudy.layout.NoAlgo;
import edu.cloudy.layout.SingleStarAlgo;
import edu.cloudy.metrics.QualityMetric;
import edu.cloudy.metrics.UniformAreaMetric;
import edu.cloudy.nlp.Word;
import edu.cloudy.nlp.WordPair;
import edu.cloudy.utils.BoundingBoxGenerator;
import edu.cloudy.utils.Cluster;
import edu.cloudy.utils.SWCRectangle;

public class ExhaustiveClusterShapingForceDirectedPlacer implements ClusterWordPlacer
{
    private ClusterWordPlacer chosenPlacer;
    private Map<QualityMetric, Double> maxMetricValues;
    private Map<QualityMetric, ClusterWordPlacer> maxMetricPlacers;

    public ExhaustiveClusterShapingForceDirectedPlacer(List<Word> words, Map<WordPair, Double> similarity, List<SingleStarAlgo> forest, BoundingBoxGenerator bbGenerator, boolean animated)
    {

        // Generate all shapes of clusters
        ClusterPermutations clusterPermutations = new ClusterPermutations(words, similarity, bbGenerator);
        Set<Cluster> clusters = clusterPermutations.getClusters();
        Set<Set<LayoutAlgo>> permutationGroups = new HashSet<Set<LayoutAlgo>>();
        for (Cluster cluster : clusters)
        {
            Set<Cluster> permutations = clusterPermutations.getLayoutsForSingleCluster(cluster);
            Set<LayoutAlgo> asAlgos = new HashSet<LayoutAlgo>();
            for (Cluster c : permutations)
            {
                asAlgos.add(new NoAlgo(c));
            }
            permutationGroups.add(asAlgos);
        }

        // Generate all combinations of cluster shapes
        Set<List<LayoutAlgo>> allPossibleCombinations = new HashSet<List<LayoutAlgo>>();
        allPossibleCombinations.add(new ArrayList<LayoutAlgo>());
        // For each cluster...
        for (Set<LayoutAlgo> permutations : permutationGroups)
        {
            Set<List<LayoutAlgo>> tempCombinations = new HashSet<List<LayoutAlgo>>();
            // For each current possible combination...
            for (List<LayoutAlgo> possibleCombination : allPossibleCombinations)
            {
                // Add each of its possibilities to each of the current combinations
                for (LayoutAlgo cluster : permutations)
                {
                    List<LayoutAlgo> newCombination = new ArrayList<LayoutAlgo>();
                    newCombination.addAll(possibleCombination);
                    newCombination.add(cluster);
                    tempCombinations.add(newCombination);
                }
            }
            // And those are now the current combinations
            allPossibleCombinations = tempCombinations;
        }
        System.out.println(allPossibleCombinations.size() + " possible combinations");
        // Try out each combination and see which is best
        List<QualityMetric> metrics = new ArrayList<QualityMetric>();
        //		metrics.add(new AdjacenciesMetric());
        //		metrics.add(new AspectRatioMetric());
        //		metrics.add(new DistortionMetric());
        //		metrics.add(new ProximityMetric());
        //		metrics.add(new StressMetric());
        metrics.add(new UniformAreaMetric());
        //		metrics.add(new TotalWeightMetric());
        maxMetricValues = new HashMap<QualityMetric, Double>();
        maxMetricPlacers = new HashMap<QualityMetric, ClusterWordPlacer>();
        for (List<LayoutAlgo> combination : allPossibleCombinations)
        {
            ClusterForceDirectedPlacer currentRun = new ClusterForceDirectedPlacer(words, similarity, combination, bbGenerator, true);
            for (QualityMetric metric : metrics)
            {
                Map<Word, SWCRectangle> wordPositions = new HashMap<Word, SWCRectangle>();
                for (LayoutAlgo algo : combination)
                {
                    wordPositions.putAll(algo.getWordPositions());
                }
                double value = metric.getValue(words, similarity, new NoAlgo(wordPositions));
                if (maxMetricValues.get(metric) == null || maxMetricValues.get(metric) < value)
                {
                    maxMetricValues.put(metric, value);
                    maxMetricPlacers.put(metric, currentRun);
                }
            }
        }
        chosenPlacer = maxMetricPlacers.get(metrics.get(0));
    }

    @Override
    public SWCRectangle getRectangleForWord(Word w)
    {
        return chosenPlacer.getRectangleForWord(w);
    }

    @Override
    public boolean contains(Word w)
    {
        return false;
    }

    @Override
    public Set<Word> getWords()
    {
        return null;
    }

    @Override
    public void expandStars(Observer observer)
    {
        chosenPlacer.expandStars(observer);
    }

}
