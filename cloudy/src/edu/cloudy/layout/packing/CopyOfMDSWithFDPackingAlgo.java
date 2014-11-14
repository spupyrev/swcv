package edu.cloudy.layout.packing;

import edu.cloudy.geom.SWCPoint;
import edu.cloudy.geom.SWCRectangle;
import edu.cloudy.layout.BaseLayoutAlgo;
import edu.cloudy.layout.LayoutResult;
import edu.cloudy.layout.MDSAlgo;
import edu.cloudy.utils.Logger;

import java.util.Arrays;
import java.util.stream.IntStream;

/**
 * June 21, 2014
 * @author spupyrev 
 */
public class CopyOfMDSWithFDPackingAlgo extends BaseLayoutAlgo
{
    private static final int MAX_ITERATIONS = 500;

    private static final double MAX_STEP = 500;
    private static final double MIN_STEP = 1;
    private static final double MIN_RELATIVE_CHANGE = 0.00005;

    public CopyOfMDSWithFDPackingAlgo()
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
        //new ForceDirectedOverlapRemoval<SWCRectangle>(5000).run(wordPositions);
        //new ForceDirectedUniformity<SWCRectangle>().run(wordPositions);
    }

    private void runFDAdjustments()
    {
        double step = MAX_STEP;
        double energy = Double.MAX_VALUE;

        SWCRectangle[] x = new SWCRectangle[words.length];
        for (int i = 0; i < x.length; i++)
            x[i] = wordPositions[i];

        CopyOfPackingCostCalculator.bbox = computeBoundingBox(x);
        CopyOfPackingCostCalculator.depXGraph = computeDependencyGraph(x, true);
        CopyOfPackingCostCalculator.depYGraph = computeDependencyGraph(x, false);
        //PackingCostCalculator.depYGraph = new int[0][2];

        int iter = 0;
        while (iter++ < MAX_ITERATIONS)
        {
            //if (iter == 100)
            //    PackingCostCalculator.DEPENDENCY_IMPORTANCE = 5.0;
            
            SWCRectangle[] oldX = new SWCRectangle[x.length];
            for (int i = 0; i < x.length; i++)
                oldX[i] = new SWCRectangle(x[i]);

            boolean progress = tryMoveNodes(x, step);
            if (!progress)
            {
                Logger.println("no progress after iteration " + iter);
                break;
            }

            double oldEnergy = energy;
            energy = CopyOfPackingCostCalculator.cost(x);
            step = updateMaxStep(step, oldEnergy, energy);

            if (iter % 10 == 0)
            {
                Logger.println("energy after " + iter + " iteration: " + energy);
                Logger.println("max step: " + step);
            }

            if (step < MIN_STEP || converged(step, oldX, x))
            {
                Logger.println("FDPacking converged after iteration " + iter);
                break;
            }
        }

        Logger.println("FDPacking done " + iter + " iterations");
        //System.out.println("final energy: " + PackingCostCalculator.cost(x));
        //System.out.println("last step: " + tryMoveNodes(x, step));

        //for (int i = 0; i < x.length; i++)
        //    wordPositions[i] = x[i];
    }

    private SWCRectangle computeBoundingBox(SWCRectangle[] x)
    {
        double area = 0;
        double sumx = 0, sumy = 0;
        for (SWCRectangle rect : x)
        {
            area += rect.getWidth() * rect.getHeight();
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

    private int[][] computeDependencyGraph(final SWCRectangle[] x, final boolean isX)
    {
        Integer[] order = new Integer[x.length];
        for (int i = 0; i < x.length; i++)
        {
            order[i] = i;
        }

        Arrays.sort(order, (Integer o1, Integer o2) ->
        {
            Double x1 = (isX ? Double.valueOf(x[o1].getCenterX()) : Double.valueOf(x[o1].getCenterY()));
            Double x2 = (isX ? Double.valueOf(x[o2].getCenterX()) : Double.valueOf(x[o2].getCenterY()));
            return x1.compareTo(x2);
        });

        int cnt = (x.length - 1);
        int[][] res = new int[cnt][2];

        for (int i = 0; i + 1 < x.length; i++)
        {
            res[i][0] = order[i];
            res[i][1] = order[i + 1];
        }

        return res;
    }

    private boolean tryMoveNodes(SWCRectangle[] x, double step)
    {
        boolean progress = false;
        for (int i = 0; i < words.length; i++)
        {
            if (tryMoveNode(x, i, step))
                progress = true;
        }

        return progress;
    }

    private boolean tryMoveNode(SWCRectangle[] x, int wordIndex, double maxStep)
    {
        SWCPoint direction = buildDirection(x, wordIndex);
        if (direction.length() < 1e-6)
            return false;

        double stepLength = buildStepLength(x, wordIndex, direction, maxStep);
        if (stepLength < MIN_STEP)
        {
            //try random direction
            direction = SWCPoint.random();
            stepLength = buildStepLength(x, wordIndex, direction, maxStep);
            if (stepLength < MIN_STEP)
                return false;
        }

        direction.scale(stepLength);
        x[wordIndex].move(direction.x(), direction.y());
        return true;
    }

    /// Calculate the direction to improve the ink function
    private SWCPoint buildDirection(SWCRectangle[] x, int wordIndex)
    {
        SWCPoint dependencyForce = CopyOfPackingCostCalculator.dependencyForce(x, wordIndex);
        SWCPoint boundaryForce = CopyOfPackingCostCalculator.boundaryForce(x, wordIndex);
        SWCPoint repulsiveForce = CopyOfPackingCostCalculator.repulsiveForce(x, wordIndex);
        SWCPoint centerForce = CopyOfPackingCostCalculator.centerForce(x, wordIndex);

        SWCPoint force = dependencyForce;
        force.add(boundaryForce);
        force.add(repulsiveForce);
        force.add(centerForce);
        if (force.length() < 0.1)
            return new SWCPoint();
        force.normalize();

        return force;
    }

    private double buildStepLength(SWCRectangle[] x, int wordIndex, SWCPoint direction, double maxStep)
    {
        double stepLength = MIN_STEP;

        SWCPoint np = new SWCPoint(x[wordIndex].getCenter());
        np.add(new SWCPoint(direction).scale(stepLength));
        double costGain = costGain(x, wordIndex, np);
        if (costGain < 0.01)
            return 0;

        while (2 * stepLength <= MAX_STEP)
        {
            SWCPoint np2 = new SWCPoint(x[wordIndex].getCenter());
            np2.add(new SWCPoint(direction).scale(stepLength * 2));
            double newCostGain = costGain(x, wordIndex, np2);
            if (newCostGain <= costGain)
                break;

            stepLength *= 2;
            costGain = newCostGain;
        }

        return stepLength;
    }

    /// Computes cost delta when moving the node
    /// the cost will be negative if a new position overlaps obstacles
    private double costGain(SWCRectangle[] x, int wordIndex, SWCPoint newPosition)
    {
        double MInf = -12345678.0;
        double depGain = CopyOfPackingCostCalculator.dependencyCostGain(x, wordIndex, newPosition);
        double boundGain = CopyOfPackingCostCalculator.boundaryCostGain(x, wordIndex, newPosition);
        if (boundGain < MInf)
            return MInf;
        double repGain = CopyOfPackingCostCalculator.repulsiveCostGain(x, wordIndex, newPosition);
        double centerGain = CopyOfPackingCostCalculator.centerCostGain(x, wordIndex, newPosition);

        return depGain + repGain + boundGain + centerGain;
    }

    int stepsWithProgress = 0;

    private double updateMaxStep(double step, double oldEnergy, double newEnergy)
    {
        //cooling factor
        double T = 0.8;
        if (newEnergy < oldEnergy && (oldEnergy - newEnergy) / oldEnergy > 0.0001)
        {
            stepsWithProgress++;
            if (stepsWithProgress >= 5)
            {
                stepsWithProgress = 0;
                step = Math.min(MAX_STEP, step / T);
            }
        }
        else
        {
            stepsWithProgress = 0;
            step *= T;
        }

        return step;
    }

    /// stop SA if relative changes are small
    private boolean converged(double step, SWCRectangle[] oldx, SWCRectangle[] newx)
    {
        double num = 0, den = 0;
        for (int i = 0; i < oldx.length; i++)
        {
            SWCPoint p = oldx[i].getCenter();
            p.subtract(newx[i].getCenter());
            num += p.lengthSquared();
            den += oldx[i].getCenter().lengthSquared();
        }
        double res = Math.sqrt(num / den);
        return (res < MIN_RELATIVE_CHANGE);
    }

}