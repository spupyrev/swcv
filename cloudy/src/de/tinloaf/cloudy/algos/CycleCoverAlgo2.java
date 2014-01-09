package de.tinloaf.cloudy.algos;

import de.tinloaf.cloudy.algos.packing.ClusterForceDirectedPlacer;
import de.tinloaf.cloudy.algos.packing.ClusterSpiralPlacer;
import de.tinloaf.cloudy.algos.packing.RecursiveSpiralCluster;
import de.tinloaf.cloudy.algos.packing.WordPlacer;
import de.tinloaf.cloudy.graph.CycleCoverExtractor;
import de.tinloaf.cloudy.graph.Edge;
import de.tinloaf.cloudy.graph.GreedyCycleCoverExtractor;
import de.tinloaf.cloudy.graph.Vertex;
import de.tinloaf.cloudy.graph.WordGraph;
import de.tinloaf.cloudy.text.Word;
import de.tinloaf.cloudy.utils.BoundingBoxGenerator;
import de.tinloaf.cloudy.utils.Cluster;
import de.tinloaf.cloudy.utils.Logger;
import de.tinloaf.cloudy.utils.SWCRectangle;
import de.tinloaf.cloudy.utils.WordPair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CycleCoverAlgo2 implements LayoutAlgo
{
    private WordGraph graph;
    private List<Edge> edgesInMatching;
    private boolean useGreedy = false;

    private WordPlacer wordPlacer;
    private BoundingBoxGenerator bbGenerator;
    private CycleType type;
    private PlacerType placer;

    boolean debug = false;
    boolean animated;

    public CycleCoverAlgo2(PlacerType placer, CycleType type, boolean animated)
    {
        this.placer = placer;
        this.type = type;
        this.animated = animated;
    }

    public CycleCoverAlgo2()
    {
        placer = PlacerType.FORCE_DIRECTED;
        type = CycleType.REGULAR;
        animated = false;
    }

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

    public String toString()
    {
        return "Cycle_" + type + "_" + placer;
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

        List<LayoutAlgo> cycleAlgos = new ArrayList<LayoutAlgo>();
        List<List<Word>> cycles = null;
        if (type == CycleType.WRAPPED)
        {
            double cycleTolerance = 0.1;
            cycles = getCycles(edgesInMatching, cycleTolerance);

            if (debug)
            {
                System.out.println(cycles.size());
            }

            while (cycles.size() > 12)
            {
                if (debug)
                {
                    System.out.println(cycles.size());
                }
                cycleTolerance -= 0.0025;
                cycles = getCycles(edgesInMatching, cycleTolerance);

            }
            // SpiralCycle
            for (List<Word> c : cycles)
            {
                LayoutAlgo singleCycleAlgo = new SpiralCycle(c);
                singleCycleAlgo.setConstraints(bbGenerator);
                singleCycleAlgo.run();
                cycleAlgos.add(singleCycleAlgo);

            }

            if (debug)
            {
                System.out.println("Num clusers: " + cycles.size());
            }
        }
        else if (type == CycleType.REGULAR)
        {
            // SingleCycleAlgo
            cycles = getCycles(edgesInMatching, 0);
            for (List<Word> c : cycles)
            {
                LayoutAlgo singleCycleAlgo = new SingleCycleAlgo(c);
                singleCycleAlgo.setConstraints(bbGenerator);
                singleCycleAlgo.run();
                cycleAlgos.add(singleCycleAlgo);
            }
        }

        if (debug)
        {
            Logger.println("#cycles: " + cycles.size());
            Logger.println("weight: " + getRealizedWeight());
        }

        //wordPlacer = new BoundingBoxPackingPlacer(g.getWords(), cycleAlgos, weightToAreaFactor, bbGenerator);
        if (placer == PlacerType.FORCE_DIRECTED)
        {
            wordPlacer = new ClusterForceDirectedPlacer(graph.getWords(), graph.getSimilarities(), cycleAlgos, bbGenerator);
        }
        else if (placer == PlacerType.SINGLE_SPIRAL)
        {
            wordPlacer = new ClusterSpiralPlacer(graph.getWords(), graph.getSimilarities(), cycleAlgos, bbGenerator, animated);
        }
        else if (placer == PlacerType.RECURSIVE_SPIRAL)
        {
            List<Cluster> initialClusters = new ArrayList<Cluster>(cycleAlgos.size());
            for (LayoutAlgo cyc : cycleAlgos)
            {
                Cluster c = new Cluster();
                Map<Word, SWCRectangle> newWordPosition = new HashMap<Word, SWCRectangle>();
                for (Word w : (cyc.getWordPositions().keySet()))
                {
                    newWordPosition.put(w, cyc.getWordRectangle(w));
                }
                c.wordPositions = newWordPosition;
                initialClusters.add(c);
            }
            wordPlacer = new RecursiveSpiralCluster(graph.getWords(), graph.getSimilarities(), bbGenerator, initialClusters, animated);
            ((RecursiveSpiralCluster)wordPlacer).run();
        }

    }

    private List<List<Word>> getCycles(List<Edge> edges, double tolerance)
    {

        List<List<Word>> result = new ArrayList<List<Word>>();

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
        {
            if (!used.contains(v))
            {
                List<Vertex> cycle = new ArrayList<Vertex>();
                dfs(v, v, next, used, cycle, tolerance);

                result.add(getCycleWords(cycle));
            }
        }

        return result;
    }

    private void dfs(Vertex v, Vertex parent, Map<Vertex, List<Vertex>> next, Set<Vertex> used, List<Vertex> cycle, double tolerance)
    {
        used.add(v);
        cycle.add(v);

        for (Vertex u : next.get(v))
        {
            if (!u.equals(parent) && !used.contains(u) && graph.getEdgeWeight(v, u) > tolerance)
                dfs(u, v, next, used, cycle, tolerance);
        }

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
        return wordPlacer.getRectangleForWord(w);
    }

    public double getRealizedWeight()
    {
        double realizedWeight = 0;
        for (Edge e : edgesInMatching)
            realizedWeight += graph.getEdgeWeight(e);

        return realizedWeight;
    }

}
