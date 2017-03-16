package edu.cloudy.layout;

import edu.cloudy.geom.SWCRectangle;
import edu.cloudy.graph.CycleCoverExtractor;
import edu.cloudy.graph.Edge;
import edu.cloudy.graph.Graph;
import edu.cloudy.graph.GreedyCycleCoverExtractor;
import edu.cloudy.graph.Vertex;
import edu.cloudy.layout.clusters.ClusterForceDirectedPlacer;
import edu.cloudy.layout.clusters.WordPlacer;
import edu.cloudy.layout.overlaps.ForceDirectedUniformity;
import edu.cloudy.nlp.Word;
import edu.cloudy.nlp.ItemPair;
import edu.cloudy.utils.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.IntStream;

public class CycleCoverAlgo extends BaseLayoutAlgo
{
    private Graph graph;
    private List<Edge> edgesInMatching;
    private boolean useGreedy = false;

    public CycleCoverAlgo()
    {
        super();
    }

    public void setUseGreedy(boolean useGreedy)
    {
        this.useGreedy = useGreedy;
    }

    @Override
    protected void run()
    {
        graph = new Graph(wordGraph);

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
        int CYCLE_SIZE_LIMIT = 12;
        cycles = breakLongCycles(cycles, CYCLE_SIZE_LIMIT);

        List<LayoutResult> cycleLayouts = new ArrayList<LayoutResult>();
        for (List<Vertex> c : cycles)
        {
            BaseLayoutAlgo algo = null;

            if (c.size() <= CYCLE_SIZE_LIMIT)
                algo = new SingleCycleAlgo();
            else
                algo = new SinglePathAlgo();

            cycleLayouts.add(algo.layout(new WordGraph(getCycleWords(c), getCycleWeights(c))));
        }

        Logger.println("#cycles: " + cycles.size());
        Logger.println("weight: " + getRealizedWeight());

        WordPlacer wordPlacer = new ClusterForceDirectedPlacer(wordGraph, cycleLayouts, bbGenerator);
        IntStream.range(0, words.length).forEach(i -> wordPositions[i] = wordPlacer.getRectangleForWord(words[i]));

        new ForceDirectedUniformity<SWCRectangle>().run(wordPositions);
    }

    private List<List<Vertex>> breakLongCycles(List<List<Vertex>> cycles, int cycleSizeLimit)
    {
        List<List<Vertex>> result = new ArrayList<List<Vertex>>();
        for (List<Vertex> c : cycles)
            if (c.size() <= cycleSizeLimit)
                result.add(c);
            else
                result.addAll(breakLongCycle(c, cycleSizeLimit));

        return result;
    }

    private List<List<Vertex>> breakLongCycle(List<Vertex> c, int cycleSizeLimit)
    {
        List<List<Vertex>> result = new ArrayList<List<Vertex>>();
        List<Vertex> cur = new ArrayList<Vertex>();
        for (Vertex v : c)
        {
            cur.add(v);
            if (cur.size() >= cycleSizeLimit)
            {
                result.add(new ArrayList<Vertex>(cur));
                cur.clear();
            }
        }

        if (cur.size() > 0)
            result.add(new ArrayList<Vertex>(cur));

        return result;
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

    private Map<ItemPair<Word>, Double> getCycleWeights(List<Vertex> cycle)
    {
        Map<ItemPair<Word>, Double> res = new HashMap<ItemPair<Word>, Double>();
        for (int i = 0; i < cycle.size(); i++)
            for (int j = 0; j < cycle.size(); j++)
            {
                Vertex u = cycle.get(i);
                Vertex v = cycle.get(j);

                ItemPair<Word> wp = new ItemPair<Word>(u, v);
                res.put(wp, (i == j ? 1.0 : 0));
            }

        for (int i = 0; i < cycle.size(); i++)
        {
            Vertex now = cycle.get(i);
            Vertex next = cycle.get((i + 1) % cycle.size());

            Edge edge = graph.getEdge(now, next);
            double weight = graph.getEdgeWeight(edge);
            ItemPair<Word> wp = new ItemPair<Word>(now, next);
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

    public double getRealizedWeight()
    {
        double realizedWeight = 0;
        for (Edge e : edgesInMatching)
            realizedWeight += graph.getEdgeWeight(e);

        return realizedWeight;
    }

}
