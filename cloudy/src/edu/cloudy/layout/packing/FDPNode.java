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
        SWCPoint closestPoint = findProjectionOnRectanglePoint(bbRect, rect.getCenter());

        double fr = closestPoint.distanceSquared(rect.getCenter());
        return fr * PackingCostCalculator.BOUNDARY_IMPORTANCE;
    }

    public SWCPoint boundaryForce(SWCRectangle bbox)
    {
        SWCRectangle bbRect = new SWCRectangle(bbox);
        bbRect.shrink(rect.getWidth(), rect.getHeight());
        SWCPoint closestPoint = findProjectionOnRectanglePoint(bbRect, rect.getCenter());

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
        double cost = 0;
        for (int i = 0; i < x.length; i++)
            if (i != index)
            {
                double dx = x[i].getWidth() / 2 + rect.getWidth() / 2 - Math.abs(x[i].getCenterX() - rect.getCenterX());
                double dy = x[i].getHeight() / 2 + rect.getHeight() / 2 - Math.abs(x[i].getCenterY() - rect.getCenterY());
                if (dx < 0 || dy < 0)
                    continue;

                double mn = Math.min(dx, dy);
                //SWCPoint fr = new SWCPoint(mn, mn);
                //cost += fr.lengthSquared();
                cost += 2 * mn * mn;
            }

        cost *= PackingCostCalculator.REPULSIVE_IMPORTANCE;
        return cost;
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
                double frx = (x[index].getCenterX() - x[i].getCenterX());
                double fry = (x[index].getCenterY() - x[i].getCenterY());

                SWCPoint fr = new SWCPoint(frx, fry);
                fr.normalize();
                fr.scale(mn);
                force.add(fr);
            }

        force.scale(PackingCostCalculator.REPULSIVE_IMPORTANCE);
        return force;
    }

    public double semanticCost(FDPNode[] x, double[][] similarity)
    {
        double cost = 0;
        for (int i = 0; i < x.length; i++)
            if (i != index)
            {
                double dx = Math.abs(x[i].getCenterX() - rect.getCenterX()) - x[i].getWidth() / 2 - rect.getWidth() / 2;
                double dy = Math.abs(x[i].getCenterY() - rect.getCenterY()) - x[i].getHeight() / 2 - rect.getHeight() / 2;
                if (dx <= 0 && dy <= 0)
                    continue;
                
                double mx = Math.max(dx, dy);
                double w = similarity[index][i];
                //cost += 2 * mx * mx * w * w;
                cost += 2 * mx * w * w;
            }

        cost *= PackingCostCalculator.SEMANTIC_IMPORTANCE;
        return cost;
    }

    public SWCPoint semanticForce(FDPNode[] x, double[][] similarity)
    {
        SWCPoint force = new SWCPoint();
        for (int i = 0; i < x.length; i++)
            if (i != index)
            {
                double dx = Math.abs(x[i].getCenterX() - rect.getCenterX()) - x[i].getWidth() / 2 - rect.getWidth() / 2;
                double dy = Math.abs(x[i].getCenterY() - rect.getCenterY()) - x[i].getHeight() / 2 - rect.getHeight() / 2;
                if (dx <= 0 && dy <= 0)
                    continue;

                //double mx = Math.max(dx, dy);
                double frx = (x[index].getCenterX() - x[i].getCenterX());
                double fry = (x[index].getCenterY() - x[i].getCenterY());

                SWCPoint fr = new SWCPoint(frx, fry);
                fr.normalize();
                
                double w = similarity[index][i];
                //fr.scale(mx);
                fr.scale(w);
                fr.scale(w);
                force.add(fr);
            }

        force.scale(PackingCostCalculator.SEMANTIC_IMPORTANCE);
        return force;
    }

    private SWCPoint findProjectionOnRectanglePoint(SWCRectangle rect, SWCPoint center)
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
}
