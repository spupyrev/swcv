package edu.cloudy.layout.mds;

import edu.cloudy.utils.Logger;

/**
 * @author spupyrev
 * 22.11.2009
 * 
 * Computes the mds by distance scaling
 */
public class DistanceScaling
{
    private static final int MAX_ITERATIONS = 1000;
    private static final double EPS = 1e-5;
    private static final double MIN_PROGRESS = 1e-2;

    public double[][] mds(double[][] d, int dim)
    {
        return mds(d, dim, true);
    }

    public double[][] mds(double[][] d, int dim, boolean useClassicalScaling)
    {
        assert (d.length == d[0].length);

        int n = d.length;
        double[][] res;
        if (useClassicalScaling)
        {
            res = new ClassicalScaling().mds(d, dim);
        }
        else
        {
            res = new double[dim][n];
            initCoordinates(res);
            scaleToAverageDistance(d, res);
        }

        //Logger.println("initial stress: " + stress(d, res));

        for (int iter = 0; iter < MAX_ITERATIONS; iter++)
        {
            if (!doIteration(d, res))
            {
                Logger.println("mds converged after " + iter + " iterations");
                break;
            }
        }

        //Logger.println("final stress: " + stress(d, res));
        return res;
    }

    private boolean doIteration(double[][] d, double[][] res)
    {
        boolean progress = false;

        int n = d.length;
        int dim = res.length;
        for (int i = 0; i < n; i++)
        {
            double[] resNew = new double[dim];
            for (int j = 0; j < n; j++)
            {
                double inv = 0;
                for (int k = 0; k < dim; k++)
                {
                    double value = res[k][i] - res[k][j];
                    inv += value * value;
                }

                inv = Math.sqrt(inv);
                if (inv > EPS)
                {
                    inv = 1.0 / inv;
                    for (int k = 0; k < dim; k++)
                    {
                        double value = (res[k][j] + d[i][j] * (res[k][i] - res[k][j]) * inv);
                        resNew[k] += value;
                    }
                }
            }

            for (int k = 0; k < dim; k++)
            {
                resNew[k] = resNew[k] / (n - 1.0);
                if (Math.abs(resNew[k] - res[k][i]) > MIN_PROGRESS)
                {
                    res[k][i] = resNew[k];
                    progress = true;
                }
            }
        }

        return progress;
    }

    private void initCoordinates(double[][] res)
    {
        for (int k = 0; k < res.length; k++)
        {
            double[] v = MathUtils.randomUnitLengthVector(res[k].length, k);
            for (int i = 0; i < res[k].length; i++)
                res[k][i] = v[i];
        }
    }

    public static void scaleToAverageDistance(double[][] d, double[][] res)
    {
        double sumD = 0;
        double sumRes = 0;
        for (int i = 0; i < d.length; i++)
            for (int j = i + 1; j < d.length; j++)
            {
                double distRes = 0;
                for (int k = 0; k < res.length; k++)
                    distRes += (res[k][i] - res[k][j]) * (res[k][i] - res[k][j]);
                distRes = Math.sqrt(distRes);

                sumRes += distRes;
                sumD += d[i][j];
            }

        double s = sumD / sumRes;
        for (int i = 0; i < d.length; i++)
            for (int k = 0; k < res.length; k++)
                res[k][i] *= s;

    }

    public static double stress(double[][] d, double[][] res)
    {
        int n = d.length;
        double sum = 0;
        for (int i = 0; i < n; i++)
            for (int j = i + 1; j < n; j++)
            {
                double dist = 0;
                for (int k = 0; k < res.length; k++)
                {
                    double value = res[k][i] - res[k][j];
                    dist += value * value;
                }

                dist = Math.sqrt(dist);

                double diff = d[i][j] - dist;
                sum += diff * diff;
            }

        return sum;
    }
}