package edu.cloudy.layout.mds;

import java.util.Random;

/**
 * @author spupyrev
 * Nov 12, 2014
 */
public class MathUtils
{
    public static double[][] copy(double[][] m)
    {
        double[][] res = new double[m.length][m[0].length];
        for (int i = 0; i < m.length; i++)
            for (int j = 0; j < m[i].length; j++)
                res[i][j] = m[i][j];

        return res;
    }

    public static double[] randomUnitLengthVector(int n, int randomSeed)
    {
        Random rnd = new Random(randomSeed);
        double[] x = new double[n];
        for (int i = 0; i < n; i++)
            x[i] = rnd.nextDouble();

        normalize(x);

        return x;
    }

    public static void randomize(double[][] m)
    {
        Random rnd = new Random(1L);
        for (int i = 0; i < m.length; i++)
            for (int j = 0; j < m[i].length; j++)
                m[i][j] = (0.5 - rnd.nextDouble());
    }

    public static void scale(double[] x, double s)
    {
        for (int i = 0; i < x.length; i++)
            x[i] *= s;
    }

    public static double normalize(double[] x)
    {
        double len = 0;
        for (int i = 0; i < x.length; i++)
            len += x[i] * x[i];

        len = Math.sqrt(len);

        if (len <= 0.0)
            return 0.0;
        scale(x, 1.0 / len);

        return len;
    }

    public static double dot(double[] u, double[] v)
    {
        assert (u.length == v.length);
        double res = 0;
        for (int i = 0; i < u.length; i++)
            res += u[i] * v[i];

        return res;
    }

    public static void scale(double[][] m, double s)
    {
        for (int i = 0; i < m.length; i++)
            scale(m[i], s);
    }

    public static void squareEntries(double[][] m)
    {
        for (int i = 0; i < m.length; i++)
            for (int j = 0; j < m[i].length; j++)
                m[i][j] *= m[i][j];
    }

    /**
     * Double-centers a matrix in such a way that the center of gravity is zero
       After double-centering, each row and each column sums up to zero
     */
    public static void doubleCenter(double[][] m)
    {
        double[] rowMean = new double[m.length];
        double[] colMean = new double[m[0].length];
        double mean = 0;
        for (int i = 0; i < m.length; i++)
        {
            for (int j = 0; j < m[0].length; j++)
            {
                rowMean[i] += m[i][j];
                colMean[j] += m[i][j];
                mean += m[i][j];
            }
        }
        for (int i = 0; i < m.length; i++)
            rowMean[i] /= m.length;
        for (int j = 0; j < m[0].length; j++)
            colMean[j] /= m[0].length;
        mean /= m.length;
        mean /= m[0].length;
        for (int i = 0; i < m.length; i++)
        {
            for (int j = 0; j < m[0].length; j++)
            {
                m[i][j] -= rowMean[i] + colMean[j] - mean;
            }
        }
    }

    /**
     *  Multiply a square matrix and a vector. 
        Note that matrix width and vector length have to be equal, otherwise null is returned.
     */
    public static double[] multiply(double[][] m, double[] x)
    {
        assert (m[0].length == x.length);
        double[] y = new double[m.length];
        for (int i = 0; i < m.length; i++)
            for (int j = 0; j < x.length; j++)
            {
                y[i] += m[i][j] * x[j];
            }

        return y;
    }

    /**
     *  Multiply a square matrix by itself 
     */
    public static double[][] square(double[][] m)
    {
        assert (m[0].length == m.length);

        int n = m.length;
        double[][] res = new double[n][n];
        for (int i = 0; i < n; i++)
        {
            for (int j = 0; j <= i; j++)
            {
                double sum = 0.0;
                for (int k = 0; k < n; k++)
                    sum += m[i][k] * m[j][k];

                res[i][j] = sum;
                res[j][i] = sum;
            }
        }

        return res;
    }

}
