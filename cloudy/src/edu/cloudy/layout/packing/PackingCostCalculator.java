package edu.cloudy.layout.packing;

import edu.cloudy.geom.SWCPoint;
import edu.cloudy.geom.SWCRectangle;

/**
 * @author spupyrev
 * Jun 22, 2014
 */
public class PackingCostCalculator
{
    public static final double REPULSIVE_IMPORTANCE = 10;
    public static final double BOUNDARY_IMPORTANCE = 100;
    public static final double DEPENDENCY_IMPORTANCE = 1;
    public static final double CENTER_IMPORTANCE = 0.01;

    private SWCRectangle bbox;
    
    private int[][] depXGraph;
    private int[][] depYGraph;

    public PackingCostCalculator(SWCRectangle bbox, int[][] depXGraph, int[][] depYGraph)
    {
        this.bbox = bbox;
        this.depXGraph = depXGraph;
        this.depYGraph = depYGraph;
    }

    public double cost(FDPNode[] x)
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

    private double repulsiveCost(FDPNode[] x)
    {
        double cost = 0;
        for (int i = 0; i < x.length; i++)
            cost += x[i].repulsiveCost(x);

        return cost;
    }

    public double repulsiveCostGain(FDPNode[] x, int index, SWCPoint newPosition)
    {
        SWCPoint oldPosition = x[index].getCenter();

        double oldCost = x[index].repulsiveCost(x);
        x[index].setCenter(newPosition.x(), newPosition.y());

        double newCost = x[index].repulsiveCost(x);
        x[index].setCenter(oldPosition.x(), oldPosition.y());

        return oldCost - newCost;
    }

    public SWCPoint repulsiveForce(FDPNode[] x, int index)
    {
        return x[index].repulsiveForce(x);
    }

    private double boundaryCost(FDPNode[] x)
    {
        double cost = 0;
        for (int i = 0; i < x.length; i++)
            cost += x[i].boundaryCost(bbox);

        return cost;
    }

    public double boundaryCostGain(FDPNode[] x, int index, SWCPoint newPosition)
    {
        SWCPoint oldPosition = x[index].getCenter();

        double oldCost = x[index].boundaryCost(bbox);
        x[index].setCenter(newPosition.x(), newPosition.y());

        double newCost = x[index].boundaryCost(bbox);
        x[index].setCenter(oldPosition.x(), oldPosition.y());

        return oldCost - newCost;
    }

    public SWCPoint boundaryForce(FDPNode[] x, int index)
    {
        return x[index].boundaryForce(bbox);
    }

    private double centerCost(FDPNode[] x)
    {
        double cost = 0;
        for (int i = 0; i < x.length; i++)
            cost += x[i].centerCost(bbox);

        return cost;
    }

    public double centerCostGain(FDPNode[] x, int index, SWCPoint newPosition)
    {
        SWCPoint oldPosition = x[index].getCenter();

        double oldCost = x[index].centerCost(bbox);
        x[index].setCenter(newPosition.x(), newPosition.y());

        double newCost = x[index].centerCost(bbox);
        x[index].setCenter(oldPosition.x(), oldPosition.y());

        return oldCost - newCost;
    }

    public SWCPoint centerForce(FDPNode[] x, int index)
    {
        return x[index].centerForce(bbox);
    }

    private double dependencyCost(FDPNode[] x)
    {
        double cost = 0;
        for (int i = 0; i < x.length; i++)
            cost += x[i].dependencyCost(x, depXGraph[i][0], depXGraph[i][1], depYGraph[i][0], depYGraph[i][1]);

        return cost;
    }

    public double dependencyCostGain(FDPNode[] x, int index, SWCPoint newPosition)
    {
        SWCPoint oldPosition = x[index].getCenter();

        double oldCost = x[index].dependencyCost(x, depXGraph[index][0], depXGraph[index][1], depYGraph[index][0], depYGraph[index][1]);
        x[index].setCenter(newPosition.x(), newPosition.y());

        double newCost = x[index].dependencyCost(x, depXGraph[index][0], depXGraph[index][1], depYGraph[index][0], depYGraph[index][1]);
        x[index].setCenter(oldPosition.x(), oldPosition.y());

        return oldCost - newCost;
    }

    public SWCPoint dependencyForce(FDPNode[] x, int index)
    {
        return x[index].dependencyForce(x, depXGraph, depYGraph);
    }

    public static SWCPoint findProjectionOnRectanglePoint(SWCRectangle rect, SWCPoint center)
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
