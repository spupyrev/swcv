package edu.cloudy.layout.overlaps;

import edu.cloudy.geom.SWCPoint;
import edu.cloudy.geom.SWCRectangle;

import java.util.Arrays;

/**
 * @author spupyrev
 * May 12, 2013
 */
public class ForceDirectedUniformity<T extends SWCRectangle> implements OverlapRemoval<T>
{
    private static double SCALE_FACTOR = 0.1;
    private static double KR = 0.3;
    private int MAX_ITER = 500;

    public ForceDirectedUniformity()
    {
    }

    public ForceDirectedUniformity(int maxIterations)
    {
        MAX_ITER = maxIterations;
    }

    @Override
    public void run(T[] wordPositions)
    {
        int iter = 0;

        int n = wordPositions.length;
        SWCRectangle[] rect = new SWCRectangle[n];
        double maxHeight = Arrays.stream(wordPositions).mapToDouble(pos -> pos.getHeight()).max().orElse(0.0);
        double delta = maxHeight * SCALE_FACTOR;

        for (int i = 0; i < n; i++)
        {
            rect[i] = new SWCRectangle(wordPositions[i]);
            rect[i].setWidth(rect[i].getWidth() + delta);
            rect[i].setHeight(rect[i].getHeight() + delta);
        }

        while (overlaps(rect) && iter++ < MAX_ITER)
        {
            // compute the displacement for all words in this time step
            for (int i = 0; i < n; i++)
            {
                SWCPoint dxy = new SWCPoint();
                double cnt = 0;

                for (int j = 0; j < n; j++)
                {
                    if (i == j)
                        continue;

                    if (rect[i].getMaxX() - 1 < rect[j].getMinX())
                        continue;
                    if (rect[i].getMinX() + 1 > rect[j].getMaxX())
                        continue;
                    if (rect[i].getMaxY() - 1 < rect[j].getMinY())
                        continue;
                    if (rect[i].getMinY() + 1 > rect[j].getMaxY())
                        continue;

                    //if (rect[i].intersects(rect[j])) {
                    SWCPoint dir = computeForceIJ(rect[i], rect[j]);
                    dxy.subtract(dir);
                    cnt++;
                    //}
                }

                if (cnt == 0)
                    continue;

                // move the word
                assert (!Double.isNaN(dxy.x()));
                assert (!Double.isNaN(dxy.y()));

                dxy.scale(1.0 / cnt);
                rect[i].setRect(rect[i].getX() + dxy.x(), rect[i].getY() + dxy.y(), rect[i].getWidth(), rect[i].getHeight());
            }
        }

        for (int i = 0; i < n; i++)
        {
            wordPositions[i].moveTo(rect[i].getX(), rect[i].getY());
        }
    }

    private SWCPoint computeForceIJ(SWCRectangle rectI, SWCRectangle rectJ)
    {
        double hix = Math.min(rectI.getMaxX(), rectJ.getMaxX());
        double lox = Math.max(rectI.getMinX(), rectJ.getMinX());
        double hiy = Math.min(rectI.getMaxY(), rectJ.getMaxY());
        double loy = Math.max(rectI.getMinY(), rectJ.getMinY());
        double dx = (hix - lox) * 1.1; // hi > lo
        double dy = (hiy - loy) * 1.1;
        assert (dx >= -1e-5);
        assert (dy >= -1e-5);

        // use force-directed to get rid of overlaps
        // f(a,b) = kr * min{dx, dy}
        double force = KR * Math.min(dx, dy);
        SWCPoint dir = new SWCPoint(rectJ.getCenterX() - rectI.getCenterX(), rectJ.getCenterY() - rectI.getCenterY());
        double len = dir.length();
        if (len < 1e-5)
            dir = SWCPoint.random();
        else
            dir.scale(1.0 / len);

        dir.scale(force);
        return dir;
    }

    private boolean overlaps(SWCRectangle[] pos)
    {
        return ForceDirectedOverlapRemoval.overlaps(pos);
    }

}
