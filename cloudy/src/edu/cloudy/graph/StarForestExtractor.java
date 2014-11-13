package edu.cloudy.graph;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Apr 25, 2013 
 * Extract star forest in three steps: 
 *  extract heaviest planar subgraph 
 *  run schnyder thing 
 *  partition each tree into odd-even stars
 */
public class StarForestExtractor
{
    private Graph g;

    public StarForestExtractor(Graph g)
    {
        this.g = g;
    }

    public List<StarForest> run()
    {
        List<StarForest> result = new ArrayList<StarForest>();

        // partition into 3 trees
        List<Graph> trees = extract3Trees();

        // odd-even stars
        for (Graph tree : trees)
            result.addAll(extractOddEvenForests(tree));

        return result;
    }

    private List<Graph> extract3Trees()
    {
        Graph graph = (Graph)g.clone();
        List<Graph> trees = new ArrayList<Graph>();

        // this can be anything!!
        int maxTrees = 3;
        for (int i = 0; i < maxTrees; i++)
        {
            if (!new CycleDetector(graph).hasCycle())
            {
                trees.add(graph);
                break;
            }

            Set<Edge> mstEdges = new HashSet<Edge>();
            Graph mst = new MaxSpanningTreeBuilder(graph).getTree(mstEdges);
            trees.add(mst);
            graph.removeAllEdges(mstEdges);
        }

        return trees;
    }

    private List<StarForest> extractOddEvenForests(final Graph tree)
    {
        final Set<Edge> oddEdges = new HashSet<Edge>();
        final Set<Edge> evenEdges = new HashSet<Edge>();
        final Set<Vertex> taggedVertices = new HashSet<Vertex>();

        class TreeTagger
        {
            void tagVertex(Vertex v, boolean odd)
            {
                taggedVertices.add(v);

                for (Edge e : tree.edgesOf(v))
                {
                    if (!oddEdges.contains(e) && (!evenEdges.contains(e)))
                    {
                        if (odd)
                            oddEdges.add(e);
                        else
                            evenEdges.add(e);

                        tagVertex(tree.getOtherSide(e, v), !odd);
                    }
                }
            }
        }

        for (Vertex v : tree.vertexSet())
            if (!taggedVertices.contains(v))
                new TreeTagger().tagVertex(v, false);

        Graph oddTree = (Graph)tree.clone();
        oddTree.removeAllEdges(evenEdges);
        Graph evenTree = (Graph)tree.clone();
        evenTree.removeAllEdges(oddEdges);

        List<StarForest> result = new ArrayList<StarForest>();
        result.add(new StarForest(oddTree.getComponents()));
        result.add(new StarForest(evenTree.getComponents()));

        return result;
    }

}
