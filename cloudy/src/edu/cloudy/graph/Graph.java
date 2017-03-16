package edu.cloudy.graph;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.jgrapht.graph.SimpleWeightedGraph;

import edu.cloudy.layout.WordGraph;
import edu.cloudy.nlp.ItemPair;
import edu.cloudy.nlp.Word;
import edu.cloudy.utils.UnorderedPair;

public class Graph extends SimpleWeightedGraph<Vertex, Edge>
{
    private static final long serialVersionUID = -2417190563746825008L;

    public Graph()
    {
        super(Edge.class);
    }

    public Graph(WordGraph wordGraph)
    {
        this(wordGraph.getWords(), wordGraph.getSimilarity());
    }

    public Graph(List<Word> words, Map<ItemPair<Word>, Double> weights)
    {
        super(Edge.class);

        List<Vertex> vertices = new ArrayList<Vertex>();
        Map<Word, Vertex> wordToVertex = new HashMap<Word, Vertex>();

        for (Word w : words)
        {
            Vertex v = new Vertex(w.word, w.weight, this);
            vertices.add(v);
            wordToVertex.put(w, v);
        }

        Map<UnorderedPair<Vertex, Vertex>, Double> vertexWeights = new HashMap<UnorderedPair<Vertex, Vertex>, Double>();
        for (ItemPair<Word> cur : weights.keySet())
        {
            Vertex v1 = wordToVertex.get(cur.getFirst());
            Vertex v2 = wordToVertex.get(cur.getSecond());

            UnorderedPair<Vertex, Vertex> newPair = new UnorderedPair<Vertex, Vertex>(v1, v2);
            vertexWeights.put(newPair, weights.get(cur));
        }

        construct(vertices, vertexWeights);
    }

    public List<Word> getWords()
    {
        return new ArrayList<Word>(vertexSet());
    }

    public Map<ItemPair<Word>, Double> getSimilarities()
    {
        Map<ItemPair<Word>, Double> ret = new HashMap<ItemPair<Word>, Double>();

        for (Edge e : edgeSet())
        {
            ItemPair<Word> cur = new ItemPair<Word>(getEdgeSource(e), getEdgeTarget(e));
            ret.put(cur, getEdgeWeight(e));
        }

        for (Word w1 : vertexSet())
        {
            for (Word w2 : vertexSet())
            {
                ItemPair<Word> cur = new ItemPair<Word>(w1, w2);
                if (!ret.containsKey(cur))
                {
                    ret.put(cur, 0.0);
                }
            }
        }

        return ret;
    }

    public void outputSimilarities()
    {
        System.out.println("===========");
        for (Edge e : edgeSet())
            System.out.println(getEdgeWeight(e));
    }

    private void construct(List<Vertex> vertices, Map<UnorderedPair<Vertex, Vertex>, Double> weights)
    {
        //adding vertices
        for (Vertex v : vertices)
        {
            addVertex(v);
        }

        //adding edges
        for (UnorderedPair<Vertex, Vertex> pair : weights.keySet())
        {
            if (pair.getFirst().equals(pair.getSecond()))
                continue;

            Edge e = addEdge(pair.getFirst(), pair.getSecond());
            setEdgeWeight(e, weights.get(pair));
        }
    }

    public List<Graph> getComponents()
    {
        final Graph decompose = (Graph)this.clone();
        final List<Graph> ret = new LinkedList<Graph>();

        class ComponentCutter
        {
            void cutComponent(Vertex v, Graph out)
            {
                out.addVertex(v);

                for (Edge e : decompose.edgesOf(v))
                {
                    Vertex other = decompose.getOtherSide(e, v);
                    if (out.containsVertex(other))
                    {
                        // just add the edge
                        out.addEdge(v, other, e);
                        //decompose.removeEdge(e);
                    }
                    else
                    {
                        // continue cutting there - edge should be added from that side!
                        cutComponent(other, out);
                    }
                }

                //decompose.removeVertex(v);
            }
        }

        while (decompose.vertexSet().size() > 0)
        {
            Vertex seed = decompose.vertexSet().iterator().next();
            Graph component = new Graph();
            new ComponentCutter().cutComponent(seed, component);
            ret.add(component);

            decompose.removeAllVertices(component.vertexSet());
        }

        return ret;
    }

    public Vertex getOtherSide(Edge e, Vertex v)
    {
        if (this.getEdgeSource(e) == v)
        {
            return this.getEdgeTarget(e);
        }
        else
        {
            assert this.getEdgeTarget(e) == v;
            return this.getEdgeSource(e);
        }
    }

    public Iterator<Edge> weightOrderedEdgeIterator(boolean reverse)
    {
        List<Edge> list = new ArrayList<Edge>(edgeSet());
        Comparator<Edge> comparator = (e1, e2) -> Double.compare(getEdgeWeight(e1), getEdgeWeight(e2));
        if (!reverse)
            Collections.sort(list, comparator);
        else
            Collections.sort(list, comparator.reversed());
        return list.iterator();
    }

    public double totalWeight()
    {
        Iterator<Edge> it = this.edgeSet().iterator();
        Double weight = 0.0;

        while (it.hasNext())
        {
            Edge e = it.next();

            weight += getEdgeWeight(e);
        }

        return weight;
    }

}
