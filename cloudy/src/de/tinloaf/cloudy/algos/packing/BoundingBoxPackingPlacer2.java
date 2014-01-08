package de.tinloaf.cloudy.algos.packing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.tinloaf.cloudy.algos.LayoutAlgo;
import de.tinloaf.cloudy.algos.packing.RectanglePacker.Rectangle;
import de.tinloaf.cloudy.text.Word;
import de.tinloaf.cloudy.utils.BoundingBoxGenerator;
import de.tinloaf.cloudy.utils.Cluster;
import de.tinloaf.cloudy.utils.SWCRectangle;

/**
 * @author spupyrev
 * May 3, 2013
 */
public class BoundingBoxPackingPlacer2 implements WordPlacer
{
    private List<Word> words;
    private Map<Word, SWCRectangle> wordPositions = new HashMap<Word, SWCRectangle>();
    private List<? extends LayoutAlgo> singlePlacers;

    private BoundingBoxGenerator bbGenerator;
    private double weightToAreaFactor;

    private double MAX_WIDTH;
    private double MAX_HEIGHT;

    public BoundingBoxPackingPlacer2(List<Word> words, List<? extends LayoutAlgo> singlePlacers, double weightToAreaFactor, BoundingBoxGenerator bbGenerator)
    {
        this.words = words;
        this.singlePlacers = singlePlacers;
        this.bbGenerator = bbGenerator;
        this.weightToAreaFactor = weightToAreaFactor;

        run();
    }

    @Override
    public SWCRectangle getRectangleForWord(Word w)
    {
        assert (wordPositions.containsKey(w));
        return wordPositions.get(w);
    }

    @Override
    public boolean contains(Word w)
    {
        // TODO Auto-generated method stub
        return false;
    }

    private void run()
    {
        //get the groups of words: stars, cycles etc
        List<Cluster> clusters = createClusters();

        //compute MAX_WIDTH and MAX_HEIGHT
        computeCloudDimensions(clusters);

        double scale = 1.05;
        //try to layout words
        while (!doPacking(clusters))
        {
            //increase cloud dimensions
            MAX_WIDTH *= scale;
            MAX_HEIGHT *= scale;
        }

        doPacking(clusters);
    }

    private boolean doPacking(List<Cluster> clusters)
    {
        //TODO: what should be these values??
        RectanglePacker<Cluster> packer = new RectanglePacker<Cluster>((int)MAX_WIDTH, (int)MAX_HEIGHT, 0);

        for (Cluster cluster : clusters)
        {
            SWCRectangle bbox = cluster.getBoundingBox();
            RectanglePacker.Rectangle res = packer.insert((int)bbox.getWidth(), (int)bbox.getHeight(), cluster);

            //unable to pack rectangles
            if (res == null)
                return false;
        }

        //fill out wordPositions
        for (Cluster cluster : clusters)
        {
            SWCRectangle bbox = cluster.getBoundingBox();
            Rectangle rect = packer.findRectangle(cluster);

            for (Word w : cluster.wordPositions.keySet())
            {
                SWCRectangle r = cluster.wordPositions.get(w);
                wordPositions.put(w, new SWCRectangle(r.getX() + rect.x - bbox.getX(), r.getY() + rect.y - bbox.getY(), r.getWidth(), r.getHeight()));
            }
        }

        return true;
    }

    private List<Cluster> createClusters()
    {
        List<Cluster> result = new ArrayList<Cluster>();
        for (int i = 0; i < singlePlacers.size(); i++)
            result.add(new Cluster());

        for (Word w : words)
        {
            SWCRectangle rect = null;
            for (int i = 0; i < singlePlacers.size(); i++)
            {
                SWCRectangle tmp = singlePlacers.get(i).getWordRectangle(w);
                if (tmp != null)
                {
                    result.get(i).wordPositions.put(w, tmp);
                    rect = tmp;
                    break;
                }
            }

            //create its own cluster
            if (rect == null)
            {
                Cluster c = new Cluster();
                c.wordPositions.put(w, bbGenerator.getBoundingBox(w, weightToAreaFactor * w.weight));
                result.add(c);
            }
        }

        return result;
    }

    private void computeCloudDimensions(List<Cluster> clusters)
    {
        double area = 0;
        for (Cluster c : clusters)
        {
            SWCRectangle bb = c.getBoundingBox();
            area += bb.getHeight() * bb.getWidth();
        }

        double aspectRatio = 4.0 / 3.0;
        MAX_HEIGHT = Math.sqrt(1.5 * area / aspectRatio);
        MAX_WIDTH = MAX_HEIGHT * aspectRatio;
    }

    @Override
    public Set<Word> getWords()
    {
        return this.wordPositions.keySet();
    }
}
