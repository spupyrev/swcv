package edu.cloudy.layout.packing;

import edu.cloudy.geom.SWCPoint;
import edu.cloudy.geom.SWCRectangle;
import edu.cloudy.layout.BaseLayoutAlgo;
import edu.cloudy.layout.LayoutResult;
import edu.cloudy.layout.MDSAlgo;
import edu.cloudy.layout.overlaps.ForceDirectedOverlapRemoval;
import edu.cloudy.layout.overlaps.ForceDirectedUniformity;
import edu.cloudy.utils.Logger;

import java.util.stream.IntStream;

/**
 * June 21, 2014
 * @author spupyrev 
 */
public class ForceDirectedPackingAlgo extends BaseLayoutAlgo
{
    private static final int MAX_ITERATIONS = 500;

    private static final double MAX_STEP = 500;
    private static final double MIN_STEP = 1;
    private static final double MIN_RELATIVE_POSITION_CHANGE = 0.00005;
    private static final double MIN_RELATIVE_ENERGY_CHANGE = 0.00001;
    private static final double COOLING_FACTOR = 0.9;

    public ForceDirectedPackingAlgo()
    {
    }

    @Override
    protected void run()
    {
        //initial layout
        LayoutResult initialLayout = new MDSAlgo(false).layout(wordGraph);
        IntStream.range(0, words.length).forEach(i -> wordPositions[i] = initialLayout.getWordPosition(words[i]));

        runFDAdjustments();

        //postprocessing
        new ForceDirectedOverlapRemoval<SWCRectangle>(5000).run(wordPositions);
        new ForceDirectedUniformity<SWCRectangle>().run(wordPositions);
    }

    private PackingCostCalculator costCalculator;

    private void runFDAdjustments()
    {
        double step = MAX_STEP;
        double energy = Double.MAX_VALUE;

        FDPNode[] x = new FDPNode[words.length];
        for (int i = 0; i < x.length; i++)
            x[i] = new FDPNode(wordPositions[i], i);

        costCalculator = new PackingCostCalculator(computeBoundingBox(x), similarity);

        int iter = 0;
        while (iter++ < MAX_ITERATIONS)
        {
            SWCPoint[] oldX = new SWCPoint[x.length];
            for (int i = 0; i < x.length; i++)
                oldX[i] = new SWCPoint(x[i].getCenter());

            boolean progress = tryMoveNodes(x, step);
            if (!progress)
            {
                Logger.println("no progress after iteration " + iter);
                break;
            }

            if (iter % 3 == 0)
            {
                double oldEnergy = energy;
                energy = costCalculator.cost(x);
                step = updateMaxStep(step, oldEnergy, energy);
            }

            /*if (iter % 50 == 0)
            {
                Logger.println("energy after " + iter + " iteration: " + energy);
                Logger.println("max step: " + step);
            }*/

            if (step < MIN_STEP || converged(step, oldX, x))
            {
                Logger.println("FDPacking converged after iteration " + iter);
                break;
            }
        }

        /*Logger.println("FDPacking done " + iter + " iterations");
        Logger.println("final energy " + costCalculator.cost(x));
        Logger.println("final Repulsive " + costCalculator.repulsiveCost(x));
        Logger.println("final Center " + costCalculator.centerCost(x));
        Logger.println("final Boundary " + costCalculator.boundaryCost(x));
        Logger.println("final Semantic " + costCalculator.semanticCost(x));*/
    }

    private SWCRectangle computeBoundingBox(FDPNode[] x)
    {
        double area = 0;
        double sumx = 0, sumy = 0;
        for (FDPNode rect : x)
        {
            area += rect.getArea();
            sumx += rect.getCenterX();
            sumy += rect.getCenterY();
        }

        sumx /= x.length;
        sumy /= x.length;
        area *= 2.25;

        double width = Math.sqrt(aspectRatio * area);
        double height = area / width;

        return new SWCRectangle(sumx - width / 2, sumy - height / 2, width, height);
    }

    private boolean tryMoveNodes(FDPNode[] x, double step)
    {
        boolean progress = false;
        for (int i = 0; i < words.length; i++)
        {
            if (tryMoveNode(x, i, step))
                progress = true;
        }

        return progress;
    }

    private boolean tryMoveNode(FDPNode[] x, int index, double maxStep)
    {
        SWCPoint direction = buildDirection(x, index);
        if (direction.length() < 1e-6)
            return false;

        double stepLength = buildStepLength(x, index, direction, maxStep);
        if (stepLength < MIN_STEP)
        {
            //try random direction
            direction = SWCPoint.random();
            stepLength = buildStepLength(x, index, direction, maxStep);
            if (stepLength < MIN_STEP)
                return false;
        }

        direction.scale(stepLength);
        x[index].move(direction.x(), direction.y());
        return true;
    }

    /** 
     * Calculates the optimal direction to improve the layout energy
     */
    private SWCPoint buildDirection(FDPNode[] x, int index)
    {
        SWCPoint force = new SWCPoint();
        force.add(costCalculator.boundaryForce(x, index));
        force.add(costCalculator.repulsiveForce(x, index));
        force.add(costCalculator.centerForce(x, index));
        force.add(costCalculator.semanticForce(x, index));

        if (force.length() < 0.1)
            return new SWCPoint();
        force.normalize();

        return force;
    }

    /** 
     * Calculates the optimal length of a movement along the given direction to improve the layout energy
     */
    private double buildStepLength(FDPNode[] x, int index, SWCPoint direction, double maxStep)
    {
        double stepLength = MIN_STEP;

        SWCPoint np = new SWCPoint(x[index].getCenter());
        np.add(new SWCPoint(direction).scale(stepLength));
        double costGain = costGain(x, index, np);
        if (costGain < 0.01)
            return 0;

        while (2 * stepLength <= MAX_STEP)
        {
            SWCPoint np2 = new SWCPoint(x[index].getCenter());
            np2.add(new SWCPoint(direction).scale(stepLength * 2));
            double newCostGain = costGain(x, index, np2);
            if (newCostGain <= costGain)
                break;

            stepLength *= 2;
            costGain = newCostGain;
        }

        return stepLength;
    }

    /** 
     * Computes cost delta when moving the node
     * the cost is negative if the new position overlaps obstacles
     */
    private double costGain(FDPNode[] x, int index, SWCPoint newPosition)
    {
        double MInf = -12345678.0;
        double boundGain = costCalculator.boundaryCostGain(x, index, newPosition);
        if (boundGain < MInf)
            return MInf;
        double centerGain = costCalculator.centerCostGain(x, index, newPosition);
        if (centerGain < MInf)
            return MInf;
        double repGain = costCalculator.repulsiveCostGain(x, index, newPosition);
        if (repGain < MInf)
            return MInf;
        double semGain = costCalculator.semanticCostGain(x, index, newPosition);

        return repGain + boundGain + centerGain + semGain;
    }

    int stepsWithProgress = 0;

    private double updateMaxStep(double step, double oldEnergy, double newEnergy)
    {
        if (newEnergy < oldEnergy && (oldEnergy - newEnergy) / oldEnergy > MIN_RELATIVE_ENERGY_CHANGE)
        {
            stepsWithProgress++;
            if (stepsWithProgress >= 5)
            {
                stepsWithProgress = 0;
                step = Math.min(MAX_STEP, step / COOLING_FACTOR);
            }
        }
        else
        {
            stepsWithProgress = 0;
            step *= COOLING_FACTOR;
        }

        return step;
    }

    /// stop SA if relative changes are small
    private boolean converged(double step, SWCPoint[] oldx, FDPNode[] newx)
    {
        double num = 0, den = 0;
        for (int i = 0; i < oldx.length; i++)
        {
            SWCPoint p = new SWCPoint(oldx[i]);
            p.subtract(newx[i].getCenter());

            num += p.lengthSquared();
            den += oldx[i].lengthSquared();
        }

        double res = Math.sqrt(num / den);
        return (res < MIN_RELATIVE_POSITION_CHANGE);
    }

}