package edu.cloudy.layout;

import edu.cloudy.graph.Edge;
import edu.cloudy.graph.Vertex;
import edu.cloudy.graph.WordGraph;
import edu.cloudy.layout.overlaps.ForceDirectedUniformity;
import edu.cloudy.layout.packing.ClusterForceDirectedPlacer;
import edu.cloudy.layout.packing.WordPlacer;
import edu.cloudy.nlp.Word;
import edu.cloudy.nlp.WordPair;
import edu.cloudy.utils.BoundingBoxGenerator;
import edu.cloudy.utils.SWCRectangle;

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
public class StarForestAlgoNew implements LayoutAlgo
{
    private List<Word> words;
    private Map<WordPair, Double> similarity;
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
        this.words = words;
        this.similarity = similarity;

    }

    @Override
    public void run()
    {
        WordGraph g = new WordGraph(words, similarity);

        List<SingleStarAlgo> forest = greedyExtractStarForest(g);

        WordPlacer wordPlacer = new ClusterForceDirectedPlacer(words, similarity, forest, bbGenerator);

        for (Word w : words)
        {
            wordPositions.put(w, wordPlacer.getRectangleForWord(w));
        }

        new ForceDirectedUniformity<SWCRectangle>().run(words, wordPositions);
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
        return wordPositions.get(w);
    }

}
