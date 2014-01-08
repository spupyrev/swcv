package de.tinloaf.cloudy.algos;

import de.tinloaf.cloudy.algos.overlaps.ForceDirectedUniformity;
import de.tinloaf.cloudy.algos.packing.ClusterForceDirectedPlacer;
import de.tinloaf.cloudy.algos.packing.WordPlacer;
import de.tinloaf.cloudy.graph.CycleCoverExtractor;
import de.tinloaf.cloudy.graph.Edge;
import de.tinloaf.cloudy.graph.GreedyCycleCoverExtractor;
import de.tinloaf.cloudy.graph.Vertex;
import de.tinloaf.cloudy.graph.WordGraph;
import de.tinloaf.cloudy.text.Word;
import de.tinloaf.cloudy.utils.BoundingBoxGenerator;
import de.tinloaf.cloudy.utils.Logger;
import de.tinloaf.cloudy.utils.SWCRectangle;
import de.tinloaf.cloudy.utils.WordPair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CycleCoverAlgo implements LayoutAlgo
{
    private WordGraph graph;
    private List<Edge> edgesInMatching;
    private boolean useGreedy = false;

    private BoundingBoxGenerator bbGenerator;

    private Map<Word, SWCRectangle> wordPositions = new HashMap<Word, SWCRectangle>();

    @Override
    public void setConstraints(BoundingBoxGenerator bbGenerator)
    {
        this.bbGenerator = bbGenerator;
    }

    @Override
    public void setData(List<Word> words, Map<WordPair, Double> similarity)
    {
        this.graph = new WordGraph(words, similarity);
    }

    public void setUseGreedy(boolean useGreedy)
    {
        this.useGreedy = useGreedy;
    }

    @Override
    public void run()
    {
        if (!useGreedy)
        {
            CycleCoverExtractor tme = new CycleCoverExtractor(graph);
            tme.runUndirected();
            edgesInMatching = tme.getMatchedEdges();
        }
        else
        {
            GreedyCycleCoverExtractor tme = new GreedyCycleCoverExtractor(graph);
            tme.run();
            edgesInMatching = tme.getMatchedEdges();
        }

        checkConsistency(edgesInMatching);

        List<List<Vertex>> cycles = getCycles(edgesInMatching);

        List<LayoutAlgo> cycleAlgos = new ArrayList<LayoutAlgo>();
        for (List<Vertex> c : cycles)
        {
            LayoutAlgo algo = null;

            if (c.size() <= 12)
                algo = new SingleCycleAlgo(getCycleWords(c));
            else
                algo = new SinglePathAlgo();

            algo.setConstraints(bbGenerator);
            algo.setData(getCycleWords(c), getCycleWeights(c));
            algo.run();
            cycleAlgos.add(algo);
        }

        Logger.println("#cycles: " + cycles.size());
        Logger.println("weight: " + getRealizedWeight());

        WordPlacer wordPlacer = new ClusterForceDirectedPlacer(graph.getWords(), graph.getSimilarities(), cycleAlgos, bbGenerator);

        for (Word w : graph.getWords())
        {
            wordPositions.put(w, wordPlacer.getRectangleForWord(w));
        }

        new ForceDirectedUniformity<SWCRectangle>().run(graph.getWords(), wordPositions);
    }

    private List<List<Vertex>> getCycles(List<Edge> edges)
    {
        List<List<Vertex>> result = new ArrayList<List<Vertex>>();

        Map<Vertex, List<Vertex>> next = new HashMap<Vertex, List<Vertex>>();
        for (Vertex v : graph.vertexSet())
            next.put(v, new ArrayList<Vertex>());

        for (Edge edge : edges)
        {
            Vertex u = graph.getEdgeSource(edge);
            Vertex v = graph.getEdgeTarget(edge);
            next.get(u).add(v);
            next.get(v).add(u);
        }

        Set<Vertex> used = new HashSet<Vertex>();
        for (Vertex v : graph.vertexSet())
            if (!used.contains(v))
            {
                List<Vertex> cycle = new ArrayList<Vertex>();
                dfs(v, v, next, used, cycle);

                result.add(cycle);
            }

        return result;
    }

    private void dfs(Vertex v, Vertex parent, Map<Vertex, List<Vertex>> next, Set<Vertex> used, List<Vertex> cycle)
    {
        used.add(v);
        cycle.add(v);

        for (Vertex u : next.get(v))
            if (!u.equals(parent) && !used.contains(u))
                dfs(u, v, next, used, cycle);
    }

    private List<Word> getCycleWords(List<Vertex> cycle)
    {
        List<Word> res = new ArrayList<Word>();

        for (int i = 0; i < cycle.size(); i++)
            res.add(cycle.get(i));

        return res;
    }

    private Map<WordPair, Double> getCycleWeights(List<Vertex> cycle)
    {
        Map<WordPair, Double> res = new HashMap<WordPair, Double>();

        for (int i = 0; i < cycle.size(); i++)
        {
            Vertex now = cycle.get(i);
            Vertex next = cycle.get((i + 1) % cycle.size());

            Edge edge = graph.getEdge(now, next);
            double weight = graph.getEdgeWeight(edge);
            WordPair wp = new WordPair(now, next);
            res.put(wp, weight);
        }

        return res;
    }

    private void checkConsistency(List<Edge> edges)
    {
        // check that we really have cycles
        Map<Vertex, Integer> degree = new HashMap<Vertex, Integer>();
        for (Edge edge : edges)
        {
            Vertex u = graph.getEdgeSource(edge);
            Vertex v = graph.getEdgeTarget(edge);
            int currentU = (degree.containsKey(u) ? degree.get(u) : 0);
            int currentV = (degree.containsKey(v) ? degree.get(v) : 0);

            if (currentU >= 2 || currentV >= 2)
                throw new RuntimeException("not a cycle");

            degree.put(u, currentU + 1);
            degree.put(v, currentV + 1);
        }
    }

    @Override
    public SWCRectangle getWordRectangle(Word w)
    {
        return wordPositions.get(w);
    }

    public double getRealizedWeight()
    {
        double realizedWeight = 0;
        for (Edge e : edgesInMatching)
            realizedWeight += graph.getEdgeWeight(e);

        return realizedWeight;
    }

}
