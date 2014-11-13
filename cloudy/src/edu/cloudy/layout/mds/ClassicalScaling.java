package edu.cloudy.layout.mds;

/**
 * @author spupyrev
 * Nov 12, 2014
 * 
 * Computes multidimensional scaling by fitting inner products
 */
public class ClassicalScaling
{
    private static final int MAX_ITERATIONS = 50;
    private static final double EPS = 1e-6;

    public double[][] mds(double[][] d, int dimensions)
    {
        assert (d.length == d[0].length);
        int n = d.length;

        for (int i = 0; i < n; i++)
            for (int j = 0; j < n; j++)
            {
                assert (Math.abs(d[i][j] - d[j][i]) < 1e-6);
                assert (d[i][i] == 0);
                assert (d[i][j] >= 0);
            }

        double[][] B = MathUtils.copy(d);
        MathUtils.squareEntries(B);
        MathUtils.doubleCenter(B);
        MathUtils.scale(B, -0.5);

        double[][] res = new double[dimensions][B.length];
        int uIndex = 0;
        for (int k = 0; k <= dimensions; k++)
        {
            double[] u = MathUtils.randomUnitLengthVector(n, k);
            double lambda = powerIteration(B, u);
            //System.out.println("\u019B_" + (k + 1) + " = " + lambda);

            if (k < dimensions)
            {
                if (lambda >= 0.0)
                {
                    double s = Math.sqrt(lambda);
                    for (int j = 0; j < B.length; j++)
                        res[uIndex][j] = u[j] * s;
                    uIndex++;
                }
                else
                {
                    dimensions++;
                }

            }
            for (int i = 0; i < n; i++)
                for (int j = 0; j < n; j++)
                {
                    B[i][j] -= lambda * u[i] * u[j];
                }
        }

        return res;
    }

    private double powerIteration(double[][] B, double[] u)
    {
        double lambda = 0;

        double r = 0;
        double limit = 1.0 - EPS;
        //iterate until convergence but at most 'maxIterations' steps
        for (int i = 0; (i < MAX_ITERATIONS) && (r < limit); i++)
        {
            double[] x = MathUtils.multiply(B, u);
            lambda = MathUtils.normalize(x);
            r = Math.abs(MathUtils.dot(u, x));
            System.arraycopy(x, 0, u, 0, x.length);
        }

        return lambda;
    }

}
