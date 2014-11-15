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
    public static final double CENTER_IMPORTANCE = 0.00001;
    public static final double SEMANTIC_IMPORTANCE = 0.001;

    private SWCRectangle bbox;
    
    private double[][] similarity;

    public PackingCostCalculator(SWCRectangle bbox, double[][] similarity)
    {
        this.bbox = bbox;
        this.similarity = similarity;
    }

    public double cost(FDPNode[] x)
    {
        double cost = 0;

        //boundary
        cost += boundaryCost(x);

        //repulsive
        cost += repulsiveCost(x);

        //center
        cost += centerCost(x);

        //semantic
        cost += semanticCost(x);

        return cost;
    }

    public double semanticCost(FDPNode[] x)
    {
        double cost = 0;
        for (int i = 0; i < x.length; i++)
            cost += x[i].semanticCost(x, similarity);

        return cost;
    }

    public double semanticCostGain(FDPNode[] x, int index, SWCPoint newPosition)
    {
        SWCPoint oldPosition = x[index].getCenter();

        double oldCost = x[index].semanticCost(x, similarity);
        x[index].setCenter(newPosition.x(), newPosition.y());

        double newCost = x[index].semanticCost(x, similarity);
        x[index].setCenter(oldPosition.x(), oldPosition.y());

        return oldCost - newCost;
    }

    public SWCPoint semanticForce(FDPNode[] x, int index)
    {
        return x[index].semanticForce(x, similarity);
    }

    public double repulsiveCost(FDPNode[] x)
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

    public double boundaryCost(FDPNode[] x)
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

    public double centerCost(FDPNode[] x)
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

}
