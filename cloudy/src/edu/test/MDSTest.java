package edu.test;

import edu.cloudy.geom.SWCPoint;
import edu.cloudy.layout.mds.ClassicalScaling;
import edu.cloudy.layout.mds.DistanceScaling;

import org.junit.Test;

import junit.framework.Assert;

/**
 * @author spupyrev
 * Nov 13, 2014
 */
public class MDSTest
{
    @Test
    public void testClassicalScaling1()
    {
        double[][] d = new double[2][2];
        d[0][0] = d[1][1] = 0;
        d[0][1] = d[1][0] = 2.25;

        double[][] output = new ClassicalScaling().mds(d, 2);

        checkOutput(d, output);
        checkDistance(output, 0, 1, 2.25);
    }

    @Test
    public void testClassicalScaling2()
    {
        double[][] d = new double[3][3];
        d[0][0] = d[1][1] = d[2][2] = 0;
        d[0][1] = d[1][0] = 3;
        d[0][2] = d[2][0] = 4;
        d[1][2] = d[2][1] = 5;

        double[][] output = new ClassicalScaling().mds(d, 2);

        checkOutput(d, output);
        checkDistance(output, 0, 1, 3);
        checkDistance(output, 0, 2, 4);
        checkDistance(output, 1, 2, 5);
    }

    @Test
    public void testClassicalScaling3()
    {
        double[][] d = randomDistances(50);

        double[][] output = new ClassicalScaling().mds(d, 2);

        checkOutput(d, output);
        double stress = DistanceScaling.stress(d, output);
        Assert.assertTrue(Double.isFinite(stress));
    }

    @Test
    public void testDistanceScaling1()
    {
        double[][] d = new double[2][2];
        d[0][0] = d[1][1] = 0;
        d[0][1] = d[1][0] = 1;

        double[][] output = new DistanceScaling().mds(d, 2, true);

        checkOutput(d, output);
        checkDistance(output, 0, 1, 1);
    }

    @Test
    public void testDistanceScaling2()
    {
        double[][] d = new double[3][3];
        d[0][0] = d[1][1] = d[2][2] = 0;
        d[0][1] = d[1][0] = 3;
        d[0][2] = d[2][0] = 4;
        d[1][2] = d[2][1] = 5;

        double[][] output = new DistanceScaling().mds(d, 2);

        checkOutput(d, output);
        checkDistance(output, 0, 1, 3);
        checkDistance(output, 0, 2, 4);
        checkDistance(output, 1, 2, 5);
    }

    @Test
    public void testDistanceScaling3()
    {
        double[][] d = new double[3][3];
        d[0][0] = d[1][1] = d[2][2] = 0;
        d[0][1] = d[1][0] = 0.1;
        d[0][2] = d[2][0] = 10;
        d[1][2] = d[2][1] = 9.9;

        double[][] output = new DistanceScaling().mds(d, 2);

        checkOutput(d, output);
        checkDistance(output, 0, 1, 0.1);
        checkDistance(output, 0, 2, 10);
        checkDistance(output, 1, 2, 9.9);
    }

    @Test
    public void testDistanceScaling4()
    {
        double[][] d = randomDistances(50);

        double[][] output = new DistanceScaling().mds(d, 2);

        checkOutput(d, output);
        double stress = DistanceScaling.stress(d, output);
        Assert.assertTrue(Double.isFinite(stress));
    }

    private double[][] randomDistances(int n)
    {
        double[][] d = new double[n][n];
        for (int i = 0; i < n; i++)
        {
            d[i][i] = 0;
            for (int j = i + 1; j < n; j++)
            {
                double r = Math.random();
                d[i][j] = d[j][i] = r;
            }
        }
        return d;
    }

    private void checkDistance(double[][] output, int i, int j, double desiredDist)
    {
        double actualDist = distance(output, i, j);
        Assert.assertTrue(Math.abs(actualDist - desiredDist) < 1e-3);
    }

    private void checkOutput(double[][] d, double[][] output)
    {
        for (int i = 0; i < d[0].length; i++)
        {
            Assert.assertFalse(Double.isNaN(output[0][i]));
            Assert.assertFalse(Double.isNaN(output[1][i]));
        }
    }

    private double distance(double[][] output, int i, int j)
    {
        SWCPoint p1 = new SWCPoint(output[0][i], output[1][i]);
        SWCPoint p2 = new SWCPoint(output[0][j], output[1][j]);

        return p1.distance(p2);
    }
}
