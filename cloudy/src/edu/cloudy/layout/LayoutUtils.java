package edu.cloudy.layout;

/**
 * @author spupyrev
 * Jan 11, 2014
 */
public class LayoutUtils
{
    public static double idealDistanceConverter(double similarity)
    {
        return -Math.log(0.9 * similarity + 0.1);
    }
}
