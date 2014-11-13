package edu.cloudy.graph;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;

/**
 * Apr 25, 2013
 * Extract heaviest cycle cover via greedy heuristic
 */
public class GreedyCycleCoverExtractor
{
    private Graph g;
    private List<Edge> matchedEdges;

    public GreedyCycleCoverExtractor(Graph g)
    {
        this.g = g;
    }

    public List<Edge> getMatchedEdges()
    {
        return this.matchedEdges;
    }

    public void run()
    {
        run(new ArrayList<Edge>());
    }

    public void run(List<Edge> usedEdges)
    {
        Map<Vertex, Integer> degree = new HashMap<Vertex, Integer>();

        Queue<Edge> q = new PriorityQueue<Edge>(10, new Comparator<Edge>()
        {
            @Override
            public int compare(Edge e1, Edge e2)
            {
                Double w1 = g.getEdgeWeight(e1);
                Double w2 = g.getEdgeWeight(e2);
                return w2.compareTo(w1);
            }
        });

        Set<Edge> availableEdges = new HashSet<Edge>(g.edgeSet());
        for (Edge e : usedEdges)
        {
            Vertex u = g.getEdgeSource(e);
            Vertex v = g.getEdgeTarget(e);
            incrementDegree(degree, u);
            incrementDegree(degree, v);

            availableEdges.remove(e);
        }

        for (Edge e : availableEdges)
            q.add(e);

        matchedEdges = new ArrayList<Edge>();
        while (!q.isEmpty())
        {
            Edge edge = q.poll();
            Vertex u = g.getEdgeSource(edge);
            Vertex v = g.getEdgeTarget(edge);
            if (degree.containsKey(u) && degree.get(u) >= 2)
                continue;
            if (degree.containsKey(v) && degree.get(v) >= 2)
                continue;

            incrementDegree(degree, u);
            incrementDegree(degree, v);
            matchedEdges.add(edge);
        }
    }

    private void incrementDegree(Map<Vertex, Integer> degree, Vertex v)
    {
        int currentV = (degree.containsKey(v) ? degree.get(v) : 0);
        degree.put(v, currentV + 1);
    }

}
