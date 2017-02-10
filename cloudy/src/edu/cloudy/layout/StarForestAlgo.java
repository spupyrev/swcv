package edu.cloudy.layout;

import edu.cloudy.geom.SWCRectangle;
import edu.cloudy.graph.Edge;
import edu.cloudy.graph.Graph;
import edu.cloudy.graph.Vertex;
import edu.cloudy.layout.clusters.ClusterForceDirectedPlacer;
import edu.cloudy.layout.clusters.WordPlacer;
import edu.cloudy.layout.overlaps.ForceDirectedUniformity;
import edu.cloudy.nlp.Word;
import edu.cloudy.nlp.ItemPair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.IntStream;

/**
 * @author spupyrev 
 * Aug 29, 2013 
 */
public class StarForestAlgo extends BaseLayoutAlgo
{
    public StarForestAlgo()
    {
        super();
    }

    @Override
    public void run()
    {
        Graph g = new Graph(wordGraph);

        List<LayoutResult> forest = greedyExtractStarForest(g);

        WordPlacer wordPlacer = new ClusterForceDirectedPlacer(wordGraph, forest, bbGenerator);
        IntStream.range(0, words.length).forEach(i -> wordPositions[i] = wordPlacer.getRectangleForWord(words[i]));

        new ForceDirectedUniformity<SWCRectangle>().run(wordPositions);
    }

    private List<LayoutResult> greedyExtractStarForest(Graph g)
    {
        List<LayoutResult> result = new ArrayList<LayoutResult>();
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
            Graph star = createStar(bestStarCenter, usedVertices, g);
            SingleStarAlgo ssa = new SingleStarAlgo();
            ssa.setGraph(star);

            //take the star
            result.add(ssa.layout(wordGraph));
            //update used
            usedVertices.addAll(ssa.getRealizedVertices());
        }

        return result;
    }

    private Graph createStar(Vertex center, Set<Vertex> usedVertices, Graph g)
    {
        List<Word> words = new ArrayList<Word>();
        for (Vertex v : g.vertexSet())
            if (!usedVertices.contains(v))
                words.add(v);

        Map<ItemPair<Word>, Double> weights = new HashMap<ItemPair<Word>, Double>();
        for (Vertex v : g.vertexSet())
        {
            if (center.equals(v))
                continue;
            if (usedVertices.contains(v))
                continue;
            if (!g.containsEdge(center, v))
                continue;

            ItemPair<Word> wp = new ItemPair<Word>(center, v);
            Edge edge = g.getEdge(center, v);
            weights.put(wp, g.getEdgeWeight(edge));
        }

        return new Graph(words, weights);
    }

}
