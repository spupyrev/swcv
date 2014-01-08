package de.tinloaf.cloudy.algos;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import de.tinloaf.cloudy.algos.packing.ClusterForceDirectedPlacer;
import de.tinloaf.cloudy.algos.packing.WordPlacer;
import de.tinloaf.cloudy.graph.CycleDetector;
import de.tinloaf.cloudy.graph.StarForest;
import de.tinloaf.cloudy.graph.StarForestExtractor;
import de.tinloaf.cloudy.graph.WordGraph;
import de.tinloaf.cloudy.text.Word;
import de.tinloaf.cloudy.utils.BoundingBoxGenerator;
import de.tinloaf.cloudy.utils.SWCRectangle;
import de.tinloaf.cloudy.utils.WordPair;

public class StarForestAlgo implements LayoutAlgo
{
    private List<Word> words;
    private Map<WordPair, Double> similarity;
    private SubSolution bestSolution;
    private WordPlacer wordPlacer;

    private BoundingBoxGenerator bbGenerator;

    @Override
    public void setConstraints(BoundingBoxGenerator bbGenerator)
    {
        this.bbGenerator = bbGenerator;
    }

    private class SubSolution
    {
        List<SingleStarAlgo> stars;
        double value;

        SubSolution()
        {
            stars = new ArrayList<SingleStarAlgo>();
            value = 0;
        }
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

        List<StarForest> forests = new StarForestExtractor(g).run();

        SubSolution max = null;
        for (StarForest sf : forests)
        {
            SubSolution cur = solveStarForest(sf);
            if (max == null || cur.value > max.value)
                max = cur;
        }

        bestSolution = max;

        //System.out.println("#stars: " + bestSolution.stars.size());
        //System.out.println("#value: " + bestSolution.value);
        //wordPlacer = new BoundingBoxPackingPlacer(words, bestSolution.stars, weightToAreaFactor, bbGenerator);
        wordPlacer = new ClusterForceDirectedPlacer(words, similarity, bestSolution.stars, bbGenerator);
    }

    private SubSolution solveStarForest(final StarForest starForest)
    {
        SubSolution solution = new SubSolution();

        for (WordGraph star : starForest.getStars())
        {
            assert (!new CycleDetector(star).hasCycle());

            SingleStarAlgo ssa = new SingleStarAlgo();
            ssa.setConstraints(bbGenerator);
            ssa.setData(words, similarity);
            ssa.setGraph(star);
            ssa.run();

            solution.stars.add(ssa);
            solution.value += ssa.getRealizedWeight();
        }

        return solution;
    }

    public double getRealizedWeight()
    {
        return this.bestSolution.value;
    }

    @Override
    public SWCRectangle getWordRectangle(Word w)
    {
        return wordPlacer.getRectangleForWord(w);
    }

    /**
     * perform several iterations
     * returns 'true' iff the last iteration moves rectangles 'alot'
     */
    public boolean doIteration(int iters)
    {
        return ((ClusterForceDirectedPlacer)wordPlacer).doIteration(iters);
    }

}
