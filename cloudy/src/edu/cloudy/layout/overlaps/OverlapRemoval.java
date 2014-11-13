package edu.cloudy.layout.overlaps;

import edu.cloudy.geom.SWCRectangle;

/**
 * @author spupyrev
 * May 12, 2013
 */
public interface OverlapRemoval<T extends SWCRectangle>
{
    public void run(T[] wordPositions);
}
