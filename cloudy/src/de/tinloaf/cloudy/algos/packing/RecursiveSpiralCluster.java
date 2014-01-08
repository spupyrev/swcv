package de.tinloaf.cloudy.algos.packing;

import de.tinloaf.cloudy.algos.LayoutAlgo;
import de.tinloaf.cloudy.algos.overlaps.ForceDirectedOverlapRemoval;
import de.tinloaf.cloudy.text.Word;
import de.tinloaf.cloudy.utils.BoundingBoxGenerator;
import de.tinloaf.cloudy.utils.Cluster;
import de.tinloaf.cloudy.utils.SWCPoint;
import de.tinloaf.cloudy.utils.SWCRectangle;
import de.tinloaf.cloudy.utils.StarExpander;
import de.tinloaf.cloudy.utils.WordPair;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Observer;
import java.util.Set;

public class RecursiveSpiralCluster implements ClusterWordPlacer, LayoutAlgo
{
    private static final int NUM_CLUSTER_DIVISIONS = 10;

    boolean debug = false;
    boolean animated;

    private List<Word> words;
    private Map<WordPair, Double> similarities;
    private Map<Word, SWCRectangle> wordPositions = new HashMap<Word, SWCRectangle>();

    private List<Cluster> clusters;

    private BoundingBoxGenerator bbGenerator;

    public RecursiveSpiralCluster(List<Word> words, Map<WordPair, Double> similarities, BoundingBoxGenerator bbGenerator, List<Cluster> initialClusters, boolean animated)
    {
        this.words = words;
        this.similarities = similarities;
        this.bbGenerator = bbGenerator;
        this.clusters = initialClusters;
        this.animated = animated;

        //run();
    }

    public RecursiveSpiralCluster()
    {
    }

    public String toString()
    {
        return "RecSpiralClust";
    }

    public SWCRectangle getRectangleForWord(Word w)
    {
        assert (wordPositions.containsKey(w));
        if (debug)
            System.out.println("Word : " + w + " position : " + wordPositions.get(w));
        return wordPositions.get(w);
    }

    public boolean contains(Word w)
    {
        return words.contains(w);
    }

    public Set<Word> getWords()
    {
        return wordPositions.keySet();
    }

    public void run()
    {

        if (clusters == null)
        {
            clusters = createInitialClusters();
        }

        if (debug)
        {
            System.out.println("num initial clusters: " + clusters.size());

            for (Cluster c : clusters)
            {
                System.out.println("on cluster: " + c);
            }
        }

        Cluster allWords = createCluster(clusters);

        /* change class word positions to our master clutster positions, since
         * the class variable is used for placement
         */

        restoreWordPositions(allWords);

        wordPositions = allWords.wordPositions;

        new ForceDirectedOverlapRemoval<SWCRectangle>().run(words, wordPositions);

    }

    private Cluster spiralPlace(List<Cluster> clusts)
    {
        boolean isFirstCluster = true;
        double spiralConstant = 1;
        List<Cluster> placedClusters = new LinkedList<Cluster>();
        for (Cluster c : clusts)
        {

            if (debug)
                System.out.println("on cluster: " + c);

            if (isFirstCluster)
            {
                c.center = new SWCPoint(0, 0);
                placedClusters.add(c);
                isFirstCluster = false;
            }
            else
            {
                c.center = new SWCPoint(0, 0);

                int spiralPosition = 0;

                while (c.overlap(placedClusters))
                {
                    spiralOut(c, spiralPosition, spiralConstant);
                    spiralPosition++;
                    //System.out.println(c.center);
                }
                spiralConstant += 0.5;
                placedClusters.add(c);
            }
        }
        //restoreWordPositions(clusts);

        Cluster combined = new Cluster();
        combined.center = new SWCPoint(0, 0);
        for (Cluster c : clusts)
        {
            if (debug)
                System.out.println("Clusts.center : " + c.center);
            combined.wordPositions.putAll(c.wordPositions);
        }

        if (debug)
        {
            for (Word w : combined.wordPositions.keySet())
            {
                System.out.println("Word : " + w + " position : " + combined.wordPositions.get(w));
            }
        }

        return combined;

    }

    private static void spiralOut(Cluster c, int spiralValue, double constant)
    {
        SWCPoint p = c.center;

        double deltaX = constant * Math.sqrt(spiralValue) * Math.cos(spiralValue);
        double deltaY = constant * Math.sqrt(spiralValue) * Math.sin(spiralValue);

        for (SWCRectangle r : c.wordPositions.values())
        {
            r.setX(r.getX() + deltaX * 10);
            r.setY(r.getY() + deltaY * 10);
        }

        c.center = new SWCPoint(p.x() + deltaX, p.y() + deltaY);

    }

    private Cluster createCluster(List<Cluster> clusts)
    {

        /*
         * base case: seen as many clusters as we would have liked to 
         */
        if (clusts.size() <= NUM_CLUSTER_DIVISIONS)
            return spiralPlace(clusts);

        /*
         * recursive step:
         * split into 4 groups, recursively structure each group, then
         * combine the 4 groups.
         *
         * we split into 4 groups in the following way:
         *      get a normalized relatedness between a current word
         *      and a cluster
         */
        else
        {
            Collections.shuffle(clusts);

            ArrayList<ArrayList<Cluster>> manyClusters = new ArrayList<ArrayList<Cluster>>(NUM_CLUSTER_DIVISIONS);

            /* make NUM_CLUSTER_DIVISIONS groups out of our current
             * list of clusters
             */
            for (int i = 0; i < NUM_CLUSTER_DIVISIONS; i++)
            {
                ArrayList<Cluster> c = new ArrayList<Cluster>();
                c.add(clusts.remove(0));
                manyClusters.add(c);
            }

            for (Cluster c : clusts)
            {
                addToMostRelated(c, manyClusters);
            }

            ArrayList<Cluster> packedClusters = new ArrayList<Cluster>(NUM_CLUSTER_DIVISIONS);
            for (ArrayList<Cluster> alc : manyClusters)
            {
                packedClusters.add(createCluster(alc));
            }

            if (debug)
                System.out.println("packedClusters: " + packedClusters);

            return spiralPlace(packedClusters);

        }

    }

    private void addToMostRelated(Cluster toAdd, ArrayList<ArrayList<Cluster>> options)
    {
        // start assuming we add to the first list
        ArrayList<Cluster> addTo = options.get(0);
        double relatedness = clusterToClusterListRelatedness(toAdd, options.get(0));

        for (int i = 1; i < options.size(); i++)
        {
            if (relatedness < clusterToClusterListRelatedness(toAdd, options.get(i)))
            {
                relatedness = clusterToClusterListRelatedness(toAdd, options.get(i));
                addTo = options.get(i);
            }
        }

        addTo.add(toAdd);

    }

    private double clusterToClusterListRelatedness(Cluster c, List<Cluster> clusters)
    {
        int normalizationFactor = 0;
        double relatedness = 0;

        for (Word w : c.wordPositions.keySet())
        {
            for (Cluster c2 : clusters)
            {
                for (Word wc : c2.wordPositions.keySet())
                {
                    normalizationFactor++;
                    WordPair wp = new WordPair(wc, w);
                    if (similarities.get(wp) == null)
                    {
                        wp = new WordPair(w, wc);
                    }
                    if (similarities.get(wp) != null)
                    {
                        relatedness += similarities.get(wp);
                    }
                }
            }
        }

        return relatedness / normalizationFactor;

    }

    public List<Cluster> createInitialClusters()
    {
        List<Cluster> result = new ArrayList<Cluster>();

        //clusters start out as 1 word / cluster
        for (Word w : words)
        {
            Cluster c = new Cluster();
            Map<Word, SWCRectangle> newWordPosition = new HashMap<Word, SWCRectangle>();
            newWordPosition.put(w, bbGenerator.getBoundingBox(w, w.weight));
            c.wordPositions = newWordPosition;
            result.add(c);
        }

        return result;
    }

    private void restoreWordPositions(Cluster clust)
    {
        List<Cluster> temp = new LinkedList<Cluster>();

        temp.add(clust);

        restoreWordPositions(temp);
    }

    private void restoreWordPositions(List<Cluster> clusts)
    {
        for (Cluster c : clusts)
        {
            Map<Word, SWCRectangle> newWordPositions = new HashMap<Word, SWCRectangle>();
            for (Word w : c.wordPositions.keySet())
            {
                newWordPositions.put(w, c.actualWordPosition(w));
            }
            c.wordPositions = newWordPositions;
        }

        //new ForceDirectedOverlapRemoval<SWCRectangle>().run(words, newWordPositions);
        if (!animated)
            expandStars(null);
    }

    @Override
    public void setConstraints(BoundingBoxGenerator bbGenerator)
    {
        this.bbGenerator = bbGenerator;
    }

    @Override
    public void setData(List<Word> words, Map<WordPair, Double> similarity)
    {
        this.words = words;
        this.similarities = similarity;
    }

    public void setInitialClusters(List<Cluster> clusts)
    {
        this.clusters = clusts;
    }

    @Override
    public SWCRectangle getWordRectangle(Word w)
    {
        return getRectangleForWord(w);
    }

    @Override
    public void expandStars(Observer observer)
    {
        StarExpander se = new StarExpander(clusters, wordPositions, words, animated);
        if (observer != null)
        {
            se.addObserver(observer);
        }
        se.expandStars();

    }

}
