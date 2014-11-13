package edu.cloudy.clustering;

import edu.cloudy.layout.WordGraph;

/**
 * @author spupyrev
 * Nov 28, 2013
 */
public interface IClusterAlgo
{
    ClusterResult run(WordGraph wordGraph);
}
