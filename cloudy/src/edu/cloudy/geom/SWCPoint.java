package edu.cloudy.geom;

import java.util.Random;

/*************************************************************************
 *  Compilation:  javac Point.java
 *  Execution:    java Point
 *
 *  Implementation of 2D point using rectangular coordinates.
 *
 *************************************************************************/

public class SWCPoint
{
    private static final Random rnd = new Random(2);

    private double x;
    private double y;

    // create and initialize a point
    public SWCPoint()
    {
        this.x = 0;
        this.y = 0;
    }

    // create and initialize a point with given (x, y)
    public SWCPoint(double x, double y)
    {
        this.x = x;
        this.y = y;
    }

    // create and initialize a point with given (x, y)
    public static SWCPoint random()
    {
        return new SWCPoint(-1 + 2 * rnd.nextDouble(), -1 + 2 * rnd.nextDouble());
    }

    // create and initialize a point with given (x, y)
    public SWCPoint(SWCPoint p)
    {
        this.x = p.x();
        this.y = p.y();
    }

    // accessor methods
    public double x()
    {
        return x;
    }

    public double y()
    {
        return y;
    }

    // return signed area of triangle a->b->c
    public static double area(SWCPoint a, SWCPoint b, SWCPoint c)
    {
        return 0.5 * (a.x * b.y - a.y * b.x + a.y * c.x - a.x * c.y + b.x * c.y - c.x * b.y);
    }

    // is a->b->c a counterclockwise turn
    // +1 (yes), -1 (no), 0 (collinear)
    public static int ccw(SWCPoint a, SWCPoint b, SWCPoint c)
    {
        double area = area(a, b, c);
        if (area > 0)
            return +1;
        else if (area < 0)
            return -1;
        else
            return 0;
    }

    // is invoking point inside circle defined by a-b-c
    // if circle is degenerate, return true
    public boolean inside(SWCPoint a, SWCPoint b, SWCPoint c)
    {
        if (ccw(a, b, c) > 0)
            return (in(a, b, c) > 0);
        else if (ccw(a, b, c) < 0)
            return (in(a, b, c) < 0);
        return true;
    }

    // return positive, negative, or zero depending on whether
    // invoking point is inside circle defined by a, b, and c
    // assumes a-b-c are counterclockwise
    private double in(SWCPoint a, SWCPoint b, SWCPoint c)
    {
        SWCPoint d = this;
        double adx = a.x - d.x;
        double ady = a.y - d.y;
        double bdx = b.x - d.x;
        double bdy = b.y - d.y;
        double cdx = c.x - d.x;
        double cdy = c.y - d.y;

        double abdet = adx * bdy - bdx * ady;
        double bcdet = bdx * cdy - cdx * bdy;
        double cadet = cdx * ady - adx * cdy;
        double alift = adx * adx + ady * ady;
        double blift = bdx * bdx + bdy * bdy;
        double clift = cdx * cdx + cdy * cdy;

        return alift * bcdet + blift * cadet + clift * abdet;
    }

    // return string representation of this point
    public String toString()
    {
        return "(" + x + ", " + y + ")";
    }

    public double distance(SWCPoint p2)
    {
        return distance(p2.x, p2.y);
    }

    public double distanceSquared(SWCPoint p2)
    {
        return distanceSquared(p2.x, p2.y);
    }

    public double distance(double x, double y)
    {
        double dx = this.x - x;
        double dy = this.y - y;
        return Math.sqrt(dx * dx + dy * dy);
    }

    public double distanceSquared(double x, double y)
    {
        double dx = this.x - x;
        double dy = this.y - y;
        return dx * dx + dy * dy;
    }

    public double length()
    {
        return distance(0, 0);
    }

    public double lengthSquared()
    {
        return x * x + y * y;
    }

    public void normalize()
    {
        double len = distance(0, 0);
        if (len < 1e-6)
            return;
        x /= len;
        y /= len;
    }

    public SWCPoint scale(double scaleFactor)
    {
        x *= scaleFactor;
        y *= scaleFactor;
        return this;
    }

    public void add(SWCPoint p)
    {
        x += p.x;
        y += p.y;
    }

    public void subtract(SWCPoint p)
    {
        x -= p.x;
        y -= p.y;
    }

    static double VectorProduct(SWCPoint p0, SWCPoint p1)
    {
        return p0.x * p1.x + p0.y * p1.y;
    }

}
