package edu.cloudy.layout;

import edu.cloudy.layout.packing.ClusterWordPlacer;

public interface ClusterLayoutAlgo extends LayoutAlgo
{
    public ClusterWordPlacer getPlacer();
}
