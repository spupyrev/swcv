package de.tinloaf.cloudy.algos;

import de.tinloaf.cloudy.algos.packing.ClusterForceDirectedPlacer;
import de.tinloaf.cloudy.algos.packing.ClusterSpiralPlacer;
import de.tinloaf.cloudy.algos.packing.ClusterWordPlacer;
import de.tinloaf.cloudy.algos.packing.ExhaustiveClusterShapingForceDirectedPlacer;
import de.tinloaf.cloudy.algos.packing.RecursiveSpiralCluster;
import de.tinloaf.cloudy.algos.packing.WordPlacer;
import de.tinloaf.cloudy.graph.Edge;
import de.tinloaf.cloudy.graph.Vertex;
import de.tinloaf.cloudy.graph.WordGraph;
import de.tinloaf.cloudy.text.Word;
import de.tinloaf.cloudy.utils.BoundingBoxGenerator;
import de.tinloaf.cloudy.utils.Cluster;
import de.tinloaf.cloudy.utils.SWCRectangle;
import de.tinloaf.cloudy.utils.WordPair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author spupyrev 
 * Aug 29, 2013 
 */
public class StarForestAlgoNew2 implements ClusterLayoutAlgo
{
    private List<Word> words;
    private Map<WordPair, Double> similarity;

    private BoundingBoxGenerator bbGenerator;
    private WordPlacer wordPlacer;
    private PlacerType placer;
    private boolean animated;

    public StarForestAlgoNew2(PlacerType placer, boolean animate)
    {
        this.placer = placer;
        this.animated = animate;
    }

    public StarForestAlgoNew2()
    {
        this.placer = PlacerType.FORCE_DIRECTED;
        this.animated = false;
    }

    @Override
    public void setConstraints(BoundingBoxGenerator bbGenerator)
    {
        this.bbGenerator = bbGenerator;
    }

    @Override
    public void setData(List<Word> words, Map<WordPair, Double> similarity)
    {
        this.words = words;
        this.similarity = similarity;

    }

    @Override
    public String toString()
    {
        return "StarForest_" + placer;
    }

    @Override
    public void run()
    {
        WordGraph g = new WordGraph(words, similarity);

        List<SingleStarAlgo> forest = greedyExtractStarForest(g);

        if (placer == PlacerType.SINGLE_SPIRAL)
        {
            wordPlacer = new ClusterSpiralPlacer(words, similarity, forest, bbGenerator, animated);
        }
        else if (placer == PlacerType.RECURSIVE_SPIRAL)
        {
            List<Cluster> initialClusters = new ArrayList<Cluster>(forest.size());
            for (SingleStarAlgo star : forest)
            {
                Cluster c = new Cluster();
                Map<Word, SWCRectangle> newWordPosition = new HashMap<Word, SWCRectangle>();
                for (Word w : star.getWordPositions().keySet())
                {
                    newWordPosition.put(w, star.getWordRectangle(w));
                }
                c.wordPositions = newWordPosition;
                initialClusters.add(c);
            }
            wordPlacer = new RecursiveSpiralCluster(words, similarity, bbGenerator, initialClusters, animated);
            ((RecursiveSpiralCluster)wordPlacer).run();
        }
        else if (placer == PlacerType.FORCE_DIRECTED)
        {
            wordPlacer = new ClusterForceDirectedPlacer(words, similarity, forest, bbGenerator, animated);
        }
        else if (placer == PlacerType.EXHAUSTIVE_FORCE_DIRECTED)
        {
            wordPlacer = new ExhaustiveClusterShapingForceDirectedPlacer(words, similarity, forest, bbGenerator, animated);
        }
    }

    private List<SingleStarAlgo> greedyExtractStarForest(WordGraph g)
    {
        List<SingleStarAlgo> result = new ArrayList<SingleStarAlgo>();
        Set<Vertex> usedVertices = new HashSet<Vertex>();

        while (true)
        {
            //find best star center
            double bestSum = 0;
            Vertex bestStarCenter = null;

            for (Vertex v : g.vertexSet())
                if (!usedVertices.contains(v))
                {
                    double sum = 0;
                    for (Edge e : g.edgesOf(v))
                    {
                        Vertex u = g.getOtherSide(e, v);
                        if (!usedVertices.contains(u))
                        {
                            sum += g.getEdgeWeight(e);
                        }
                    }

                    if (bestStarCenter == null || sum > bestSum)
                    {
                        bestSum = sum;
                        bestStarCenter = v;
                    }
                }

            //every word is taken
            if (bestStarCenter == null)
                break;

            assert (!usedVertices.contains(bestStarCenter));

            //run FPTAS on the star
            WordGraph star = createStar(bestStarCenter, usedVertices, g);
            SingleStarAlgo ssa = new SingleStarAlgo();
            ssa.setConstraints(bbGenerator);
            ssa.setData(words, similarity);
            ssa.setGraph(star);
            ssa.run();

            //take the star
            result.add(ssa);
            //update used
            usedVertices.addAll(ssa.getRealizedVertices());
        }

        return result;
    }

    private WordGraph createStar(Vertex center, Set<Vertex> usedVertices, WordGraph g)
    {
        List<Word> words = new ArrayList<Word>();
        for (Vertex v : g.vertexSet())
            if (!usedVertices.contains(v))
                words.add(v);

        Map<WordPair, Double> weights = new HashMap<WordPair, Double>();
        for (Vertex v : g.vertexSet())
        {
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

    @Override
    public SWCRectangle getWordRectangle(Word w)
    {
        return wordPlacer.getRectangleForWord(w);
    }

    public List<Cluster> getClusters()
    {
        return ((ClusterSpiralPlacer)this.wordPlacer).getClusters();
    }

    @Override
    public ClusterWordPlacer getPlacer()
    {
        // TODO Auto-generated method stub
        return null;
    }

}
