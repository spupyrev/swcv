package edu.cloudy.layout;

import edu.cloudy.graph.Edge;
import edu.cloudy.graph.Vertex;
import edu.cloudy.graph.WordGraph;
import edu.cloudy.layout.packing.FixedPositionWordPlacer;
import edu.cloudy.layout.packing.MapWordPlacer;
import edu.cloudy.layout.packing.WordPlacer;
import edu.cloudy.metrics.AdjacenciesMetric;
import edu.cloudy.nlp.Word;
import edu.cloudy.nlp.WordPair;
import edu.cloudy.utils.BoundingBoxGenerator;
import edu.cloudy.utils.Logger;
import edu.cloudy.utils.SWCRectangle;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import knapsack.core.KnapsackInstance;
import knapsack.strategies.KnapsackFPTAS;

public class SingleStarAlgo implements LayoutAlgo
{
    private Map<Word, SWCRectangle> wordToRect;

    private BoundingBoxGenerator bbGenerator;

    private static final double BETA = 1.01;
    private static final double EPS = 0.01;
    private static final double SIZE_SCALING = 4000;
    private static final double VALUE_SCALING = 400000;

    private Set<Vertex> boxesLeft;
    private Set<Vertex> boxesRight;
    private Set<Vertex> boxesTop;
    private Set<Vertex> boxesBottom;
    private Set<Vertex> boxesNotRealized;

    private Vertex center;
    private WordGraph graph;

    private Map<WordPair, Double> similarity;

    @Override
    public void setConstraints(BoundingBoxGenerator bbGenerator)
    {
        this.bbGenerator = bbGenerator;
    }

    public void setGraph(WordGraph g)
    {
        this.graph = g;
    }

    @Override
    public void setData(List<Word> words, Map<WordPair, Double> similarity)
    {
        this.similarity = similarity;
    }

    public double getRealizedWeight()
    {
        double ret = 0.0;

        for (Word w1 : wordToRect.keySet())
            for (Word w2 : wordToRect.keySet())
            {
                if (w1.equals(w2))
                    continue;

                SWCRectangle rect1 = wordToRect.get(w1);
                SWCRectangle rect2 = wordToRect.get(w2);
                if (!AdjacenciesMetric.close(rect1, rect2))
                    continue;

                Edge edge = graph.getEdge((Vertex)w1, (Vertex)w2);
                ret += graph.getEdgeWeight(edge);
            }

        return ret / 2.0;
    }

    public List<Vertex> getRealizedVertices()
    {
        List<Vertex> result = new ArrayList<Vertex>();

        for (Word w : wordToRect.keySet())
        {
            Vertex v = (Vertex)w;

            if (v.equals(center))
            {
                result.add(center);
                continue;
            }

            if (!graph.containsEdge(center, v))
                continue;

            result.add(v);
        }

        return result;
    }

    @Override
    public void run()
    {
        computeKnapsacks();

        // get the four corner boxes
        Iterator<Edge> it = graph.weightOrderedEdgeIterator(true);
        int found = 0;
        Vertex[] cornerVertices = new Vertex[4];
        while (found < 4 && it.hasNext())
        {
            Edge e = it.next();
            Vertex child = graph.getOtherSide(e, center);

            if (boxesNotRealized.contains(child))
            {
                cornerVertices[found++] = child;
                boxesNotRealized.remove(child);
            }
        }

        wordToRect = new HashMap<Word, SWCRectangle>();
        // center box, position 0,0
        SWCRectangle centerRect = new SWCRectangle(bbGenerator.getBoundingBox(center, center.weight));
        wordToRect.put(center, centerRect);

        //sides
        layoutTop(centerRect, found, cornerVertices);
        layoutBottom(centerRect, found, cornerVertices);
        layoutRight(centerRect, found, cornerVertices);
        layoutLeft(centerRect, found, cornerVertices);

        //checkSanity();
    }

    private void layoutLeft(SWCRectangle centerRect, int found, Vertex[] cornerVertices)
    {
        double taken = 0;
        for (Vertex v : sortBoxes(boxesLeft))
        {
            SWCRectangle wordRect = new SWCRectangle(bbGenerator.getBoundingBox(v, v.weight));
            // translate to left and up
            wordRect.move(-1 * wordRect.getWidth(), taken);
            taken += wordRect.getHeight();

            wordToRect.put(v, wordRect);
        }
        assert (taken <= centerRect.getHeight());
    }

    private void layoutRight(SWCRectangle centerRect, int found, Vertex[] cornerVertices)
    {
        double taken = 0;
        for (Vertex v : sortBoxes(boxesRight))
        {
            SWCRectangle wordRect = new SWCRectangle(bbGenerator.getBoundingBox(v, v.weight));
            // translate to right and up
            wordRect.move(centerRect.getWidth(), taken);
            taken += wordRect.getHeight();

            wordToRect.put(v, wordRect);
        }
        assert (taken <= centerRect.getHeight());
    }

    private void layoutBottom(SWCRectangle centerRect, int found, Vertex[] cornerVertices)
    {
        double taken = 0;
        for (Vertex v : sortBoxes(boxesBottom))
        {
            SWCRectangle wordRect = new SWCRectangle(bbGenerator.getBoundingBox(v, v.weight));
            // translate to bottom and right
            wordRect.move(taken, -1 * wordRect.getHeight());
            taken += wordRect.getWidth();

            wordToRect.put(v, wordRect);
        }
        assert (taken <= centerRect.getWidth());

        // bottom-right
        if (found > 3)
        {
            Vertex v = cornerVertices[3];
            SWCRectangle wordRect = new SWCRectangle(bbGenerator.getBoundingBox(v, v.weight));
            wordRect.move(taken, -1 * wordRect.getHeight());

            wordToRect.put(v, wordRect);
        }

        // bottom-left
        if (found > 0)
        {
            Vertex v = cornerVertices[0];
            SWCRectangle wordRect = new SWCRectangle(bbGenerator.getBoundingBox(v, v.weight));
            wordRect.move(-1 * wordRect.getWidth(), -1 * wordRect.getHeight());

            wordToRect.put(v, wordRect);
        }
    }

    private void layoutTop(SWCRectangle centerRect, int found, Vertex[] cornerVertices)
    {
        double taken = 0;
        for (Vertex v : sortBoxes(boxesTop))
        {
            SWCRectangle wordRect = new SWCRectangle(bbGenerator.getBoundingBox(v, v.weight));
            // translate to top and right
            wordRect.move(taken, centerRect.getHeight());
            taken += wordRect.getWidth();

            assert (wordRect.getWidth() > 0);

            wordToRect.put(v, wordRect);
        }
        assert (taken <= centerRect.getWidth());

        // top-right
        if (found > 2)
        {
            Vertex v = cornerVertices[2];
            SWCRectangle wordRect = new SWCRectangle(bbGenerator.getBoundingBox(v, v.weight));
            wordRect.move(taken, centerRect.getHeight());

            wordToRect.put(v, wordRect);
        }

        // top-left
        if (found > 1)
        {
            Vertex v = cornerVertices[1];
            SWCRectangle wordRect = new SWCRectangle(bbGenerator.getBoundingBox(v, v.weight));
            wordRect.move(-1 * wordRect.getWidth(), centerRect.getHeight());

            wordToRect.put(v, wordRect);
        }
    }

    /**
     * find an optimal order of boxes 
     * Aug 29, 2013 
     */
    private List<Vertex> sortBoxes(Set<Vertex> boxes)
    {
        List<Vertex> list = new ArrayList<Vertex>(boxes);
        int n = list.size();
        if (n <= 2)
            return list;

        if (n >= 8)
            return list;

        //just try all possible permutations and choose the best
        int[] perm = new int[n];
        for (int i = 0; i < n; i++)
            perm[i] = i;

        bestWeight = -1;
        shuffleBoxes(0, perm, 0, list);

        assert (bestWeight >= 0);

        List<Vertex> result = new ArrayList<Vertex>();
        for (int i = 0; i < n; i++)
            result.add(list.get(bestOrder[i]));

        return result;
    }

    private double bestWeight;
    private int[] bestOrder;

    private void shuffleBoxes(int curIndex, int[] curOrder, double curWeight, List<Vertex> list)
    {
        if (curIndex >= list.size())
        {
            if (bestWeight >= curWeight)
                return;

            bestOrder = Arrays.copyOf(curOrder, curOrder.length);
            bestWeight = curWeight;
            return;
        }

        for (int i = curIndex; i < list.size(); i++)
        {
            swap(curOrder, i, curIndex);
            double deltaWeight = 0;
            if (curIndex > 0)
            {
                WordPair wp = new WordPair(list.get(curOrder[curIndex - 1]), list.get(curOrder[curIndex]));
                if (similarity.containsKey(wp))
                    deltaWeight += similarity.get(wp);
            }

            shuffleBoxes(curIndex + 1, curOrder, curWeight + deltaWeight, list);
            swap(curOrder, i, curIndex);
        }
    }

    private void swap(int[] array, int i, int j)
    {
        int tmp = array[i];
        array[i] = array[j];
        array[j] = tmp;
    }

    public void computeKnapsacks()
    {
        Vertex center = null;

        for (Vertex v : graph.vertexSet())
        {
            if (graph.edgesOf(v).size() > 1)
            {
                if (center == null)
                {
                    // found the center
                    center = v;
                }
                else
                {
                    // TODO remove
                    Logger.log("Star\n");
                    for (Edge e : graph.edgeSet())
                    {
                        Logger.print("(" + graph.getEdgeSource(e).word + ")");
                        Logger.print("-");
                        Logger.print("(" + graph.getEdgeTarget(e).word + ")");
                        Logger.print("\n");
                    }
                    Logger.log("=========");

                    throw new IllegalArgumentException("Graph is not a star");
                }
            }
        }

        if (center == null)
        {
            // pair or single vertex
            center = graph.vertexSet().iterator().next();
        }

        this.center = center;
        SWCRectangle centerRect = new SWCRectangle(bbGenerator.getBoundingBox(center, center.weight));

        final int horizontalSize = (int)Math.round(centerRect.getWidth() * (SIZE_SCALING));
        final int verticalSize = (int)Math.round(centerRect.getHeight() * (SIZE_SCALING));

        assert (horizontalSize > 0);
        assert (verticalSize > 0);

        final HashSet<Vertex> left = new HashSet<Vertex>();
        final HashSet<Vertex> right = new HashSet<Vertex>();
        final HashSet<Vertex> top = new HashSet<Vertex>();
        final HashSet<Vertex> bottom = new HashSet<Vertex>();

        int valueLeft = 0, valueRight = 0, valueTop = 0, valueBottom = 0;

        final ArrayList<Vertex> children = new ArrayList<Vertex>(graph.vertexSet().size());
        //		HashMap<Vertex,Integer> heights = new HashMap<Vertex,Integer>();
        //		HashMap<Vertex,Integer> widths = new HashMap<Vertex,Integer>();
        final HashMap<Vertex, Integer> value = new HashMap<Vertex, Integer>();

        final int[] heights = new int[graph.vertexSet().size() - 1];
        final int[] widths = new int[graph.vertexSet().size() - 1];

        int k = 0;
        for (Vertex v : graph.vertexSet())
        {
            if (v == center)
                continue;

            heights[k] = (int)Math.round(bbGenerator.getBoundingBox(v, v.weight).getHeight() * (SIZE_SCALING)) + 1;
            widths[k] = (int)Math.round(bbGenerator.getBoundingBox(v, v.weight).getWidth() * (SIZE_SCALING)) + 1;

            //Logger.log("Putting width: " + widths[k]);
            //Logger.log("Pixel width: " + this.bbGenerator.getBoundingBox(v, v.weight).getWidth());

            assert (heights[k] > 0);
            assert (widths[k] > 0);

            if (graph.getEdgeWeight(graph.getEdge(v, center)) > 0)
            {
                value.put(v, (int)Math.round(graph.getEdgeWeight(graph.getEdge(v, center)) * (VALUE_SCALING)));
                //Logger.log("Putting value: " + (int) Math.round(graph.getEdgeWeight(graph.getEdge(v, center)) * (VALUE_SCALING)));
            }
            else
            {
                // also try to pack 
                value.put(v, 1);
            }
            children.add(v);
            k++;
        }

        class KnapsackSolver
        {
            KnapsackFPTAS solveFor(Set<Vertex> solveSet, boolean horizontal)
            {
                KnapsackInstance instance;

                int[] marginalValues = new int[children.size()];

                for (int j = 0; j < children.size(); j++)
                {
                    Vertex v = children.get(j);

                    if ((left.contains(v)) || (right.contains(v)) || (bottom.contains(v)) || (top.contains(v)))
                    {
                        // element contained. value only, if in current set.
                        if (solveSet.contains(v))
                        {
                            marginalValues[j] = value.get(v);
                        }
                        else
                        {
                            marginalValues[j] = 0; // no advantage repacking it
                        }
                    }
                    else
                    {
                        // element is nto contained. Full value.
                        marginalValues[j] = value.get(v);
                    }

                }

                if (horizontal)
                {
                    int[] widthCopy = widths.clone();
                    instance = new KnapsackInstance(0, children.size(), horizontalSize, widthCopy, marginalValues);
                }
                else
                {
                    int[] heightCopy = heights.clone();
                    instance = new KnapsackInstance(0, children.size(), verticalSize, heightCopy, marginalValues);
                }

                KnapsackFPTAS fptas;
                try
                {
                    fptas = new KnapsackFPTAS(instance, (int)Math.round((1 / (BETA - 1))));
                }
                catch (CloneNotSupportedException e)
                {
                    e.printStackTrace();
                    throw new RuntimeException(e);
                }

                fptas.run();

                return fptas;
            }
        }

        long iterations = Math.round((1.0 / BETA) * graph.vertexSet().size() * Math.log(1 / EPS));
        //Logger.log("" + ((double) 1.0 / BETA) * graph.vertexSet().size());
        //Logger.log("Solver iterations: " + iterations + " (" + graph.vertexSet().size() + " elements)");
        KnapsackSolver solver = new KnapsackSolver();
        for (long i = 0; i < iterations; i++)
        {
            // =========
            // top
            // =========
            int topDelta;

            KnapsackFPTAS topFPTAS = solver.solveFor(top, true);
            topDelta = topFPTAS.getKnapsackCost() - valueTop;

            // =========
            // bottom
            // =========
            int bottomDelta;

            KnapsackFPTAS bottomFPTAS = solver.solveFor(bottom, true);
            bottomDelta = bottomFPTAS.getKnapsackCost() - valueBottom;

            // =========
            // left
            // =========
            int leftDelta;

            KnapsackFPTAS leftFPTAS = solver.solveFor(left, false);
            leftDelta = leftFPTAS.getKnapsackCost() - valueLeft;

            // =========
            // right
            // =========
            int rightDelta;

            KnapsackFPTAS rightFPTAS = solver.solveFor(right, false);
            rightDelta = rightFPTAS.getKnapsackCost() - valueRight;

            // Let's see who won!
            int maxDelta = Math.max(Math.max(topDelta, bottomDelta), Math.max(leftDelta, rightDelta));
            if (maxDelta <= 0)
            {
                // No improvement!
                //Logger.log("Top value: " + topFPTAS.getKnapsackCost());
                //Logger.log("Bottom value: " + bottomFPTAS.getKnapsackCost());
                //Logger.log("Left value: " + leftFPTAS.getKnapsackCost());
                //Logger.log("Right value: " + rightFPTAS.getKnapsackCost());
                //Logger.log("Quitting because of lack of improvement.");
                break;
            }

            HashSet<Vertex> winningSet = new HashSet<Vertex>();
            int[] winningElements;
            HashSet<Vertex> setToReplace;

            if (maxDelta == topDelta)
            {
                // top won!
                setToReplace = top;
                valueTop = topFPTAS.getKnapsackCost();
                winningElements = topFPTAS.getKnapsackConfiguration();
            }
            else if (maxDelta == bottomDelta)
            {
                setToReplace = bottom;
                valueBottom = bottomFPTAS.getKnapsackCost();
                winningElements = bottomFPTAS.getKnapsackConfiguration();
            }
            else if (maxDelta == leftDelta)
            {
                setToReplace = left;
                valueLeft = leftFPTAS.getKnapsackCost();
                winningElements = leftFPTAS.getKnapsackConfiguration();
            }
            else if (maxDelta == rightDelta)
            {
                setToReplace = right;
                valueRight = rightFPTAS.getKnapsackCost();
                winningElements = rightFPTAS.getKnapsackConfiguration();
            }
            else
            {
                throw new IllegalStateException("Something very strange has happened.");
            }

            for (int j = 0; j < winningElements.length; j++)
            {
                winningSet.add(children.get(winningElements[j]));
            }

            //Logger.log(" Winning set size: " + winningSet.size());

            top.removeAll(winningSet);
            bottom.removeAll(winningSet);
            left.removeAll(winningSet);
            right.removeAll(winningSet);

            setToReplace.clear(); // this empties the set;
            setToReplace.addAll(winningSet);
            /*Logger.log("Top set size: " + top.size());
            Logger.log("Top value: " + valueTop);
            for (Vertex v : top) {
            	Logger.log(v.word);
            }
            Logger.log("Bottom set size: " + bottom.size());
            Logger.log("Bottom value: " + valueBottom);
            for (Vertex v : bottom) {
            	Logger.log(v.word);
            }
            Logger.log("Left set size: " + left.size());
            Logger.log("Left value: " + valueLeft);
            for (Vertex v : left) {
            	Logger.log(v.word);
            }
            Logger.log("Right set size: " + right.size());
            Logger.log("Right value: " + valueRight);
            for (Vertex v : right) {
            	Logger.log(v.word);
            }*/
        }

        boxesBottom = bottom;
        boxesLeft = left;
        boxesRight = right;
        boxesTop = top;

        boxesNotRealized = new HashSet<Vertex>();

        for (Vertex v : graph.vertexSet())
        {
            if (boxesBottom.contains(v) || boxesLeft.contains(v) || boxesRight.contains(v) || boxesTop.contains(v))
                continue;

            if (center.equals(v))
                continue;

            boxesNotRealized.add(v);
        }
    }

    private void checkSanity()
    {
        // make sure that all the sets sum up
        HashSet<Vertex> sum = new HashSet<Vertex>(boxesBottom);
        sum.addAll(boxesLeft);
        assert (sum.size() == boxesBottom.size() + boxesLeft.size());
        sum.addAll(boxesNotRealized);
        assert (sum.size() == boxesBottom.size() + boxesLeft.size() + boxesNotRealized.size());
        sum.addAll(boxesRight);
        assert (sum.size() == boxesBottom.size() + boxesLeft.size() + boxesNotRealized.size() + boxesRight.size());
        sum.addAll(boxesTop);
        assert (sum.size() == boxesBottom.size() + boxesLeft.size() + boxesNotRealized.size() + boxesRight.size() + boxesTop.size());
        sum.add(center);

        // OK, sets are disjunct
        assert (sum.size() == graph.vertexSet().size());
        // Same number
        sum.removeAll(graph.vertexSet());
        assert (sum.size() == 0);
        // And equal!

        double usedBottom = 0;
        for (Vertex v : this.boxesBottom)
        {
            usedBottom += this.wordToRect.get(v).getWidth();
        }
        double usedTop = 0;
        for (Vertex v : this.boxesTop)
        {
            usedTop += this.wordToRect.get(v).getWidth();
        }
        double usedRight = 0;
        for (Vertex v : this.boxesRight)
        {
            usedRight += this.wordToRect.get(v).getHeight();
        }
        double usedLeft = 0;
        for (Vertex v : this.boxesLeft)
        {
            usedLeft += this.wordToRect.get(v).getHeight();
        }

        Logger.log("Used: " + Double.toString(usedTop) + "/" + Double.toString(usedBottom) + "/" + Double.toString(usedLeft) + "/"
                + Double.toString(usedRight));

        if (boxesNotRealized.size() > 0)
        {
            Vertex first = boxesNotRealized.iterator().next();
            double neededWidth = bbGenerator.getBoundingBox(first, first.weight).getWidth();
            double neededHeight = bbGenerator.getBoundingBox(first, first.weight).getHeight();

            Vertex minWidth = first, minHeight = first;

            for (Vertex v : boxesNotRealized)
            {
                double width = bbGenerator.getBoundingBox(v, v.weight).getWidth();
                double height = bbGenerator.getBoundingBox(v, v.weight).getHeight();

                if (width < neededWidth)
                {
                    neededWidth = width;
                    minWidth = v;
                }

                if (height < neededHeight)
                {
                    neededHeight = height;
                    minHeight = v;
                }
            }

            double freeWidth = this.wordToRect.get(this.center).getWidth() - Math.min(usedBottom, usedTop);
            double freeHeight = this.wordToRect.get(this.center).getHeight() - Math.min(usedLeft, usedRight);

            Logger.log("Total width: " + Double.toString(this.wordToRect.get(this.center).getWidth()));
            Logger.log("Width optimality: " + Double.toString(neededWidth) + " > " + Double.toString(freeWidth));
            Logger.log("Minimum width vertex: " + minWidth.word);
            // compensate for rounding errors...
            assert (neededWidth * 1.01 > freeWidth);
            Logger.log("Total height: " + Double.toString(this.wordToRect.get(this.center).getHeight()));
            Logger.log("Height optimality: " + Double.toString(neededHeight) + " > " + Double.toString(freeHeight));
            Logger.log("Minimum height vertex: " + minHeight.word);
            assert (neededHeight * 1.01 > freeHeight);
        }
    }

    @Override
    public SWCRectangle getWordRectangle(Word w)
    {
        if (boxesNotRealized.contains(w))
            return null;

        return wordToRect.get(w);
    }

    public List<WordPlacer> getPlacers()
    {
        List<WordPlacer> placers = new ArrayList<WordPlacer>();

        placers.add(new MapWordPlacer(wordToRect));

        for (Word w : boxesNotRealized)
        {
            SWCRectangle rect = bbGenerator.getBoundingBox(w, w.weight);
            placers.add(new FixedPositionWordPlacer(w, rect));
        }

        return placers;
    }
}
