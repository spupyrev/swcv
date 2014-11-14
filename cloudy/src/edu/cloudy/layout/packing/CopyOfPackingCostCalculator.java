package edu.cloudy.layout.packing;

import edu.cloudy.geom.SWCPoint;
import edu.cloudy.geom.SWCRectangle;

/**
 * @author spupyrev
 * Jun 22, 2014
 */
public class CopyOfPackingCostCalculator
{
    public static final double REPULSIVE_IMPORTANCE = 10;
    public static final double BOUNDARY_IMPORTANCE = 100;
    public static double DEPENDENCY_IMPORTANCE = 1;
    public static final double CENTER_IMPORTANCE = 0.01;

    public static SWCRectangle bbox;
    public static int[][] depXGraph;
    public static int[][] depYGraph;

    public static double cost(SWCRectangle[] x)
    {
        double cost = 0;

        //dependency
        cost += dependencyCost(x);

        //boundary
        cost += boundaryCost(x);

        //repulsive
        cost += repulsiveCost(x);

        //center
        cost += centerCost(x);

        return cost;
    }

    private static double repulsiveCost(SWCRectangle[] x)
    {
        double energy = 0;
        for (int i = 0; i < x.length; i++)
            for (int j = i + 1; j < x.length; j++)
            {
                double dx = x[i].getWidth() / 2 + x[j].getWidth() / 2 - Math.abs(x[i].getCenterX() - x[j].getCenterX());
                double dy = x[i].getHeight() / 2 + x[j].getHeight() / 2 - Math.abs(x[i].getCenterY() - x[j].getCenterY());
                if (dx < 0 || dy < 0)
                    continue;

                double mn = Math.min(dx, dy);
                SWCPoint fr = new SWCPoint(mn, mn);
                energy += fr.lengthSquared() * REPULSIVE_IMPORTANCE;
            }

        return energy;
    }

    public static double repulsiveCostGain(SWCRectangle[] x, int wordIndex, SWCPoint newPosition)
    {
        double oldCost = repulsiveCost(x);
        SWCPoint oldPosition = x[wordIndex].getCenter();
        x[wordIndex].setCenter(newPosition.x(), newPosition.y());

        double newCost = repulsiveCost(x);
        x[wordIndex].setCenter(oldPosition.x(), oldPosition.y());
        return oldCost - newCost;
    }

    public static SWCPoint repulsiveForce(SWCRectangle[] x, int wordIndex)
    {
        SWCPoint force = new SWCPoint();
        for (int i = 0; i < x.length; i++)
            if (i != wordIndex)
            {
                double dx = x[i].getWidth() / 2 + x[wordIndex].getWidth() / 2 - Math.abs(x[i].getCenterX() - x[wordIndex].getCenterX());
                double dy = x[i].getHeight() / 2 + x[wordIndex].getHeight() / 2 - Math.abs(x[i].getCenterY() - x[wordIndex].getCenterY());
                if (dx < 0 || dy < 0)
                    continue;

                double mn = Math.min(dx, dy);
                double frx = (x[wordIndex].getCenterX() - x[i].getCenterX());// * mn;
                double fry = (x[wordIndex].getCenterY() - x[i].getCenterY());// * mn;

                SWCPoint fr = new SWCPoint(frx, fry);
                fr.normalize();
                fr.scale(mn);
                force.add(fr);
            }

        force.scale(REPULSIVE_IMPORTANCE);
        return force;
    }

    private static double boundaryCost(SWCRectangle[] x)
    {
        double energy = 0;
        for (int i = 0; i < x.length; i++)
        {
            SWCRectangle rect = new SWCRectangle(bbox);
            rect.shrink(x[i].getWidth(), x[i].getHeight());
            SWCPoint closestPoint = findProjectionOnRectanglePoint(rect, x[i].getCenter());

            double fr = closestPoint.distanceSquared(x[i].getCenter());
            energy += fr * BOUNDARY_IMPORTANCE;
        }

        return energy;
    }

    public static double boundaryCostGain(SWCRectangle[] x, int wordIndex, SWCPoint newPosition)
    {
        double oldCost = boundaryCost(x);
        SWCPoint oldPosition = x[wordIndex].getCenter();
        x[wordIndex].setCenter(newPosition.x(), newPosition.y());

        double newCost = boundaryCost(x);
        x[wordIndex].setCenter(oldPosition.x(), oldPosition.y());
        return oldCost - newCost;
    }

    public static SWCPoint boundaryForce(SWCRectangle[] x, int wordIndex)
    {
        SWCRectangle rect = new SWCRectangle(bbox);
        rect.shrink(x[wordIndex].getWidth(), x[wordIndex].getHeight());

        SWCPoint closestPoint = findProjectionOnRectanglePoint(rect, x[wordIndex].getCenter());

        SWCPoint force = new SWCPoint(closestPoint.x() - x[wordIndex].getCenterX(), closestPoint.y() - x[wordIndex].getCenterY());
        //force.normalize();

        force.scale(BOUNDARY_IMPORTANCE);

        return force;
    }

    private static double centerCost(SWCRectangle[] x)
    {
        double energy = 0;
        for (int i = 0; i < x.length; i++)
        {
            SWCRectangle rect = new SWCRectangle(bbox);
            rect.shrink(x[i].getWidth(), x[i].getHeight());
            SWCPoint closestPoint = bbox.getCenter();

            double fr = closestPoint.distanceSquared(x[i].getCenter());
            energy += fr * CENTER_IMPORTANCE;
        }

        return energy;
    }

    public static double centerCostGain(SWCRectangle[] x, int wordIndex, SWCPoint newPosition)
    {
        double oldCost = centerCost(x);
        SWCPoint oldPosition = x[wordIndex].getCenter();
        x[wordIndex].setCenter(newPosition.x(), newPosition.y());

        double newCost = centerCost(x);
        x[wordIndex].setCenter(oldPosition.x(), oldPosition.y());
        return oldCost - newCost;
    }

    public static SWCPoint centerForce(SWCRectangle[] x, int wordIndex)
    {
        SWCRectangle rect = new SWCRectangle(bbox);
        rect.shrink(x[wordIndex].getWidth(), x[wordIndex].getHeight());

        SWCPoint closestPoint = bbox.getCenter();

        SWCPoint force = new SWCPoint(closestPoint.x() - x[wordIndex].getCenterX(), closestPoint.y() - x[wordIndex].getCenterY());

        force.scale(CENTER_IMPORTANCE);

        return force;
    }

    private static SWCPoint findProjectionOnRectanglePoint(SWCRectangle rect, SWCPoint center)
    {
        double x = 0, y = 0;

        if (rect.getMaxX() < center.x())
            x = rect.getMaxX();
        else if (rect.getMinX() > center.x())
            x = rect.getMinX();
        else
            x = center.x();

        if (rect.getMaxY() < center.y())
            y = rect.getMaxY();
        else if (rect.getMinY() > center.y())
            y = rect.getMinY();
        else
            y = center.y();

        return new SWCPoint(x, y);
    }

    private static double dependencyCost(SWCRectangle[] x)
    {
        double energy = 0;
        for (int i = 0; i < depXGraph.length; i++)
        {
            int i1 = depXGraph[i][0];
            int i2 = depXGraph[i][1];

            double dx = x[i1].getCenterX() - x[i2].getCenterX();

            if (dx > 0)
            {
                energy += dx * dx * DEPENDENCY_IMPORTANCE;
            }
        }

        for (int i = 0; i < depYGraph.length; i++)
        {
            int i1 = depYGraph[i][0];
            int i2 = depYGraph[i][1];

            double dy = x[i1].getCenterY() - x[i2].getCenterY();

            if (dy > 0)
            {
                energy += dy * dy * DEPENDENCY_IMPORTANCE;
            }
        }

        return energy;
    }

    public static double dependencyCostGain(SWCRectangle[] x, int wordIndex, SWCPoint newPosition)
    {
        double oldCost = dependencyCost(x);
        SWCPoint oldPosition = x[wordIndex].getCenter();
        x[wordIndex].setCenter(newPosition.x(), newPosition.y());

        double newCost = dependencyCost(x);
        x[wordIndex].setCenter(oldPosition.x(), oldPosition.y());
        return oldCost - newCost;
    }

    public static SWCPoint dependencyForce(SWCRectangle[] x, int wordIndex)
    {
        SWCPoint force = new SWCPoint();
        for (int i = 0; i < depXGraph.length; i++)
        {
            int i1 = depXGraph[i][0];
            int i2 = depXGraph[i][1];
            if (wordIndex != i1 && wordIndex != i2)
                continue;

            double dx = x[i1].getCenterX() - x[i2].getCenterX();

            if (dx > 0)
            {
                if (wordIndex == i1)
                    force.add(new SWCPoint(dx, 0));
                else
                    force.add(new SWCPoint(-dx, 0));
            }
        }
        
        for (int i = 0; i < depYGraph.length; i++)
        {
            int i1 = depYGraph[i][0];
            int i2 = depYGraph[i][1];
            if (wordIndex != i1 && wordIndex != i2)
                continue;

            double dy = x[i1].getCenterY() - x[i2].getCenterY();

            if (dy > 0)
            {
                if (wordIndex == i1)
                    force.add(new SWCPoint(0, dy));
                else
                    force.add(new SWCPoint(0, -dy));
            }
        }

        force.scale(DEPENDENCY_IMPORTANCE);

        return force;
    }

}
