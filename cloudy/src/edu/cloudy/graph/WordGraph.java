package edu.cloudy.graph;

import edu.cloudy.nlp.Word;
import edu.cloudy.nlp.WordPair;
import edu.cloudy.utils.UnorderedPair;

import org.jgrapht.graph.SimpleWeightedGraph;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class WordGraph extends SimpleWeightedGraph<Vertex, Edge>
{
    private static final long serialVersionUID = -2417190563746825008L;

    public class EdgeComparator implements Comparator<Edge>
    {
        private boolean reverse = false;

        public EdgeComparator(boolean reverse)
        {
            this.reverse = reverse;
        }

        @Override
        public int compare(Edge o1, Edge o2)
        {
            Double w1 = WordGraph.this.getEdgeWeight(o1);
            Double w2 = WordGraph.this.getEdgeWeight(o2);

            if (!reverse)
            {
                return w1.compareTo(w2);
            }
            else
            {
                return w2.compareTo(w1);
            }
        }

    }

    public List<Word> getWords()
    {
        return new ArrayList<Word>(this.vertexSet());
    }

    public Map<WordPair, Double> getSimilarities()
    {
        Map<WordPair, Double> ret = new HashMap<WordPair, Double>();

        for (Edge e : edgeSet())
        {
            WordPair cur = new WordPair(getEdgeSource(e), getEdgeTarget(e));
            ret.put(cur, getEdgeWeight(e));
        }

        for (Word w1 : vertexSet())
        {
            for (Word w2 : vertexSet())
            {
                WordPair cur = new WordPair(w1, w2);
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

    public WordGraph()
    {
        super(Edge.class);
    }

    public WordGraph(List<Word> words, Map<WordPair, Double> weights)
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
        for (WordPair cur : weights.keySet())
        {
            Vertex v1 = wordToVertex.get(cur.getFirst());
            Vertex v2 = wordToVertex.get(cur.getSecond());

            UnorderedPair<Vertex, Vertex> newPair = new UnorderedPair<Vertex, Vertex>(v1, v2);
            vertexWeights.put(newPair, weights.get(cur));
        }

        construct(vertices, vertexWeights);
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

    public List<WordGraph> getComponents()
    {
        final WordGraph decompose = (WordGraph)this.clone();
        final List<WordGraph> ret = new LinkedList<WordGraph>();

        class ComponentCutter
        {
            void cutComponent(Vertex v, WordGraph out)
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
            WordGraph component = new WordGraph();
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
        LinkedList<Edge> list = new LinkedList<Edge>(this.edgeSet());
        Collections.sort(list, new EdgeComparator(reverse));
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
