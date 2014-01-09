package de.tinloaf.cloudy.algos.packing;

import java.util.Observer;

public interface ClusterWordPlacer extends WordPlacer
{
    public void expandStars(Observer observer);

}
