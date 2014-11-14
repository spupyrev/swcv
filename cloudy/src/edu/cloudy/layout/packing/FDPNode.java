package edu.cloudy.layout.packing;

import edu.cloudy.geom.SWCPoint;
import edu.cloudy.geom.SWCRectangle;

/**
 * @author spupyrev
 * Nov 13, 2014
 * 
 * a warpper of word rectangle for force-directed packing
 * (used to cache forces between objects) 
 */
public class FDPNode
{
    private SWCRectangle rect;
    private int index;

    public FDPNode(SWCRectangle rect, int index)
    {
        this.rect = rect;
        this.index = index;
    }

    public double getCenterX()
    {
        return rect.getCenterX();
    }

    public double getCenterY()
    {
        return rect.getCenterY();
    }

    public double getArea()
    {
        return rect.getArea();
    }

    public SWCPoint getCenter()
    {
        return rect.getCenter();
    }

    public double getHeight()
    {
        return rect.getHeight();
    }

    public double getWidth()
    {
        return rect.getWidth();
    }

    public void move(double x, double y)
    {
        rect.move(x, y);
    }

    public void setCenter(double x, double y)
    {
        rect.setCenter(x, y);
    }

    public double boundaryCost(SWCRectangle bbox)
    {
        SWCRectangle bbRect = new SWCRectangle(bbox);
        bbRect.shrink(rect.getWidth(), rect.getHeight());
        SWCPoint closestPoint = PackingCostCalculator.findProjectionOnRectanglePoint(bbRect, rect.getCenter());

        double fr = closestPoint.distanceSquared(rect.getCenter());
        return fr * PackingCostCalculator.BOUNDARY_IMPORTANCE;
    }

    public SWCPoint boundaryForce(SWCRectangle bbox)
    {
        SWCRectangle bbRect = new SWCRectangle(bbox);
        bbRect.shrink(rect.getWidth(), rect.getHeight());
        SWCPoint closestPoint = PackingCostCalculator.findProjectionOnRectanglePoint(bbRect, rect.getCenter());

        SWCPoint force = new SWCPoint(closestPoint.x() - rect.getCenterX(), closestPoint.y() - rect.getCenterY());
        force.scale(PackingCostCalculator.BOUNDARY_IMPORTANCE);

        return force;
    }
    
    public double centerCost(SWCRectangle bbox)
    {
        SWCPoint closestPoint = bbox.getCenter();

        double fr = closestPoint.distanceSquared(rect.getCenter());
        return fr * PackingCostCalculator.CENTER_IMPORTANCE;
    }

    public SWCPoint centerForce(SWCRectangle bbox)
    {
        SWCPoint closestPoint = bbox.getCenter();

        SWCPoint force = new SWCPoint(closestPoint.x() - rect.getCenterX(), closestPoint.y() - rect.getCenterY());
        force.scale(PackingCostCalculator.CENTER_IMPORTANCE);

        return force;
    }

    public double repulsiveCost(FDPNode[] x)
    {
        double energy = 0;
        for (int i = 0; i < x.length; i++)
            if (i != index)
            {
                double dx = x[i].getWidth() / 2 + rect.getWidth() / 2 - Math.abs(x[i].getCenterX() - rect.getCenterX());
                double dy = x[i].getHeight() / 2 + rect.getHeight() / 2 - Math.abs(x[i].getCenterY() - rect.getCenterY());
                if (dx < 0 || dy < 0)
                    continue;

                double mn = Math.min(dx, dy);
                SWCPoint fr = new SWCPoint(mn, mn);
                energy += fr.lengthSquared() * PackingCostCalculator.REPULSIVE_IMPORTANCE;
            }

        return energy;
    }

    public SWCPoint repulsiveForce(FDPNode[] x)
    {
        SWCPoint force = new SWCPoint();
        for (int i = 0; i < x.length; i++)
            if (i != index)
            {
                double dx = x[i].getWidth() / 2 + x[index].getWidth() / 2 - Math.abs(x[i].getCenterX() - x[index].getCenterX());
                double dy = x[i].getHeight() / 2 + x[index].getHeight() / 2 - Math.abs(x[i].getCenterY() - x[index].getCenterY());
                if (dx < 0 || dy < 0)
                    continue;

                double mn = Math.min(dx, dy);
                double frx = (x[index].getCenterX() - x[i].getCenterX());// * mn;
                double fry = (x[index].getCenterY() - x[i].getCenterY());// * mn;

                SWCPoint fr = new SWCPoint(frx, fry);
                fr.normalize();
                fr.scale(mn);
                force.add(fr);
            }

        force.scale(PackingCostCalculator.REPULSIVE_IMPORTANCE);
        return force;
    }
    
    public double dependencyCost(FDPNode[] x, int prevX, int nextX, int prevY, int nextY)
    {
        double energy = 0;
        if (prevX != -1)
        {
            double dx = x[prevX].getCenterX() - rect.getCenterX();
            if (dx > 0)
                energy += dx * dx * PackingCostCalculator.DEPENDENCY_IMPORTANCE;
        }
        if (nextX != -1)
        {
            double dx = rect.getCenterX() - x[nextX].getCenterX();
            if (dx > 0)
                energy += dx * dx * PackingCostCalculator.DEPENDENCY_IMPORTANCE;
        }
        
        
        if (prevY != -1)
        {
            double dy = x[prevY].getCenterY() - rect.getCenterY();
            if (dy > 0)
                energy += dy * dy * PackingCostCalculator.DEPENDENCY_IMPORTANCE;
        }
        if (nextY != -1)
        {
            double dy = rect.getCenterY() - x[nextY].getCenterY();
            if (dy > 0)
                energy += dy * dy * PackingCostCalculator.DEPENDENCY_IMPORTANCE;
        }

        return energy;
    }

    public SWCPoint dependencyForce(FDPNode[] x, int[][] depXGraph, int[][] depYGraph)
    {
        !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
        
        
        SWCPoint force = new SWCPoint();
        for (int i = 0; i < depXGraph.length; i++)
        {
            int i1 = depXGraph[i][0];
            int i2 = depXGraph[i][1];
            if (index != i1 && index != i2)
                continue;

            double dx = x[i1].getCenterX() - x[i2].getCenterX();

            if (dx > 0)
            {
                if (index == i1)
                    force.add(new SWCPoint(dx, 0));
                else
                    force.add(new SWCPoint(-dx, 0));
            }
        }

        for (int i = 0; i < depYGraph.length; i++)
        {
            int i1 = depYGraph[i][0];
            int i2 = depYGraph[i][1];
            if (index != i1 && index != i2)
                continue;

            double dy = x[i1].getCenterY() - x[i2].getCenterY();

            if (dy > 0)
            {
                if (index == i1)
                    force.add(new SWCPoint(0, dy));
                else
                    force.add(new SWCPoint(0, -dy));
            }
        }

        force.scale(PackingCostCalculator.DEPENDENCY_IMPORTANCE);

        return force;
    }

}
