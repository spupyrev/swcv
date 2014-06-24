package edu.cloudy.layout;

import edu.cloudy.nlp.Word;
import edu.cloudy.nlp.WordPair;
import edu.cloudy.utils.SWCPoint;
import edu.cloudy.utils.SWCRectangle;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * June 21, 2014
 * 
 * @author spupyrev 
 */
public class MDSWithFDPackingAlgo extends BaseLayoutAlgo
{
    private static final int MAX_ITERATIONS = 500;
    private static final double MAX_STEP = 500;
    private static final double MIN_STEP = 1;
    private static final double MIN_RELATIVE_CHANGE = 0.00005;

    private Map<Word, SWCRectangle> wordPositions = new HashMap<Word, SWCRectangle>();

    public MDSWithFDPackingAlgo(List<Word> words, Map<WordPair, Double> similarity)
    {
        super(words, similarity);
    }

    @Override
    public SWCRectangle getWordPosition(Word w)
    {
        return wordPositions.get(w);
    }

    @Override
    public void run()
    {
        //TODO: change scaling!!!!!!!!!!!!!!!!!!!!!!
        
        //initial layout
        MDSAlgo algo = new MDSAlgo(words, similarity, false);
        algo.run();

        wordPositions = new HashMap<Word, SWCRectangle>();
        for (Word w : words)
        {
            SWCRectangle rect = algo.getWordPosition(w);
            wordPositions.put(w, rect);
        }

        runFDAdjustments();
    }

    private void runFDAdjustments()
    {
        double step = MAX_STEP;
        double energy = Double.MAX_VALUE;

        SWCRectangle[] x = new SWCRectangle[words.size()];
        for (int i = 0; i < x.length; i++)
            x[i] = wordPositions.get(words.get(i));

        SWCRectangle bbox = computeBoundingBox(x);
        PackingCostCalculator.bbox = bbox;

        int iteration = 0;
        while (iteration++ < MAX_ITERATIONS)
        {
            SWCRectangle[] oldX = new SWCRectangle[x.length];
            for (int i = 0; i < x.length; i++)
                oldX[i] = new SWCRectangle(x[i]);

            boolean coordinatesChanged = tryMoveNodes(x, step);
            if (!coordinatesChanged)
            {
                System.out.println("no changes in coordinates");
                break;
            }

            double oldEnergy = energy;
            energy = PackingCostCalculator.cost(x);
            step = updateMaxStep(step, oldEnergy, energy);

            System.out.println("energy after " + iteration + " iteration: " + energy);
            System.out.println("max step: " + step);

            if (step < MIN_STEP || converged(step, oldX, x))
            {
                System.out.println("step: " + step);
                break;
            }
        }

        System.out.println("FD done " + iteration + " iterations");
        //System.out.println("final energy: " + PackingCostCalculator.cost(x));
        //System.out.println("last step: " + tryMoveNodes(x, step));
        
        for (int i = 0; i < x.length; i++)
            wordPositions.put(words.get(i), x[i]);
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
        area *= 1.75;

        double width = Math.sqrt(1.61 * area);
        double height = area / width;

        SWCRectangle bb = new SWCRectangle(sumx - width / 2, sumy - height / 2, width, height);

        return bb;
    }

    private boolean tryMoveNodes(SWCRectangle[] x, double step)
    {
        boolean coordinatesChanged = false;
        for (int i = 0; i < words.size(); i++)
        {
            if (tryMoveNode(x, i, step))
            {
                coordinatesChanged = true;
            }
        }

        return coordinatesChanged;
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
        SWCPoint dependencyForce = PackingCostCalculator.dependencyForce(x, wordIndex);
        SWCPoint boundaryForce = PackingCostCalculator.boundaryForce(x, wordIndex);
        SWCPoint repulsiveForce = PackingCostCalculator.repulsiveForce(x, wordIndex);

        SWCPoint force = dependencyForce;
        force.add(boundaryForce);
        force.add(repulsiveForce);
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
        double depGain = PackingCostCalculator.dependencyCostGain(x, wordIndex, newPosition);
        if (depGain < MInf)
            return MInf;
        double boundGain = PackingCostCalculator.boundaryCostGain(x, wordIndex, newPosition);
        if (boundGain < MInf)
            return MInf;
        double repGain = PackingCostCalculator.repulsiveCostGain(x, wordIndex, newPosition);

        return depGain + repGain + boundGain;
    }

    int stepsWithProgress = 0;

    private double updateMaxStep(double step, double oldEnergy, double newEnergy)
    {
        //cooling factor
        double T = 0.8;
        if (newEnergy + 1.0 < oldEnergy)
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