package edu.cloudy.layout.mds;

import java.util.Random;

/**
 * @author spupyrev
 * 22.11.2009
 * 
 * Computes the mds by distance scaling
 */
public class DistanceScaling
{
    private static final int MAX_ITERATIONS = 1000;
    private static final double EPS = 1e-10;

    //TODO: no weights in the implementation
    public double[][] mds(double[][] d, int dimensions)
    {
        assert (d.length == d[0].length);

        int n = d.length;
        double[][] res = new double[2][n];
        for (int k = 0; k < dimensions; k++)
        {
            double[] v = randomUnitLengthVector(n, k);
            for (int i = 0; i < n; i++)
                res[k][i] = v[i];
        }

        for (int iter = 0; iter < MAX_ITERATIONS; iter++)
        {
            boolean changed = false;
            for (int i = 0; i < n; i++)
            {
                double[] resNew = new double[2];
                for (int j = 0; j < n; j++)
                {
                    double inv = 0;
                    for (int k = 0; k < dimensions; k++)
                    {
                        double value = res[k][i] - res[k][j];
                        inv += value * value;
                    }

                    inv = Math.sqrt(inv);
                    if (inv > EPS)
                    {
                        inv = 1.0 / inv;
                        for (int k = 0; k < dimensions; k++)
                        {
                            double value = res[k][j] + d[i][j] * (res[k][i] - res[k][j]) * inv;
                            resNew[k] += value;
                        }
                    }
                }

                for (int k = 0; k < dimensions; k++)
                    resNew[k] = resNew[k] / n;

                for (int k = 0; k < dimensions; k++)
                {
                    if (Math.abs(resNew[k] - res[k][i]) > 1e-3)
                    {
                        res[k][i] = resNew[k];
                        changed = true;
                    }
                }
            }

            if (!changed)
                break;
        }

        return res;
    }

    private double[] randomUnitLengthVector(int n, int k)
    {
        Random rnd = new Random(k);
        double[] v = new double[n];
        double sum = 0;
        for (int i = 0; i < n; i++)
        {
            v[i] = rnd.nextDouble();
            sum += v[i];
        }

        for (int i = 0; i < n; i++)
        {
            v[i] /= sum;
        }
        return v;
    }

    @SuppressWarnings("unused")
    private double stress(double[][] d, double[][] res)
    {
        int n = d.length;
        double sum = 0;
        for (int i = 0; i < n; i++)
            for (int j = 0; j < n; j++)
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