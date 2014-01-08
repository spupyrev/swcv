package de.tinloaf.cloudy.algos;

import de.tinloaf.cloudy.algos.packing.ClusterWordPlacer;

public interface ClusterLayoutAlgo extends LayoutAlgo
{
    public ClusterWordPlacer getPlacer();
}
