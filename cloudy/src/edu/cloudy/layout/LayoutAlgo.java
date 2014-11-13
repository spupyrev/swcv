package edu.cloudy.layout;

import edu.cloudy.geom.BoundingBoxGenerator;

/**
 * Algorithm for embedding words (rectangles) in the plane
 */
public interface LayoutAlgo
{
    public void setBoundingBoxGenerator(BoundingBoxGenerator bbGenerator);

    public void setAspectRatio(double aspectRatio);

    public LayoutResult layout(WordGraph wordGraph);
}
