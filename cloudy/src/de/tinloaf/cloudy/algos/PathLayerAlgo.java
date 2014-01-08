package de.tinloaf.cloudy.algos;

import de.tinloaf.cloudy.graph.GreedyHamiltonianHeuristic;
import de.tinloaf.cloudy.graph.Path;
import de.tinloaf.cloudy.graph.WordGraph;
import de.tinloaf.cloudy.text.Word;
import de.tinloaf.cloudy.utils.BoundingBoxGenerator;
import de.tinloaf.cloudy.utils.InconsistencyException;
import de.tinloaf.cloudy.utils.SWCRectangle;
import de.tinloaf.cloudy.utils.WordPair;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class PathLayerAlgo implements LayoutAlgo
{
    private Double fieldWidth;
    private Double boxHeight;
    private Double weightToWidth;

    private List<Word> words;
    private Map<WordPair, Double> similarity;

    private Map<Word, SWCRectangle> wordToRect;

    public PathLayerAlgo(Double fieldWidth, Double boxHeight)
    {
        this.fieldWidth = fieldWidth;
        this.boxHeight = boxHeight;
    }

    @Override
    public void setConstraints(BoundingBoxGenerator bbGenerator)
    {
        this.weightToWidth = bbGenerator.getWeightToAreaFactor() / boxHeight;
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
        WordGraph g = new WordGraph(this.words, this.similarity);
        GreedyHamiltonianHeuristic ghh = new GreedyHamiltonianHeuristic(g);
        ghh.run();

        Collection<Path> paths = ghh.getPaths();
        if (paths.size() != 1)
        {
            System.out.println("Got " + paths.size() + " paths.");
            throw new InconsistencyException();
        }

        Path p = paths.iterator().next();

        this.wordToRect = new HashMap<Word, SWCRectangle>();
        List<List<SWCRectangle>> layers = new LinkedList<List<SWCRectangle>>();

        Iterator<Word> wordIt = p.wordIterator();

        // TODO remove
        //p.print();

        List<SWCRectangle> layer = null;
        boolean odd = false;
        double nextX = this.fieldWidth + 1; // force it to create a new layer right away
        double curY = 0.0;
        while (wordIt.hasNext())
        {
            Word w = wordIt.next();

            System.out.println("Processing: " + w);

            double wordWidth = w.weight * this.weightToWidth;
            if (nextX + wordWidth > this.fieldWidth)
            {
                // start a new layer
                if (layer != null)
                {
                    if (odd)
                    {
                        Collections.reverse(layer);

                        // reverse the x-coordinate!
                        for (SWCRectangle rect : layer)
                        {
                            double cx = rect.getCenterX();
                            rect.setCenter(this.fieldWidth - cx, rect.getCenterY());
                        }
                    }
                    layers.add(layer);
                }

                odd = !odd;
                layer = new LinkedList<SWCRectangle>();
                nextX = 0.0;

                curY = layers.size() * this.boxHeight;
            }

            SWCRectangle wordRect = new SWCRectangle(nextX, curY, wordWidth, this.boxHeight);
            this.wordToRect.put(w, wordRect);
            layer.add(wordRect);
            nextX += wordWidth;
        }

        for (Word w : this.words)
        {
            System.out.println(w);
            assert (this.wordToRect.containsKey(w));
        }

    }

    @Override
    public SWCRectangle getWordRectangle(Word w)
    {
        return wordToRect.get(w);
    }

}
