package edu.cloudy.geom;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * @author spupyrev
 * May 3, 2013
 */
public class GeometryUtils
{
    public static int Cross(SWCPoint p0, SWCPoint p1, SWCPoint p2)
    {
        double cr = (p1.x() - p0.x()) * (p2.y() - p0.y()) - (p2.x() - p0.x()) * (p1.y() - p0.y());
        if (cr > 1e-8)
            return 1;
        if (cr < -1e-8)
            return -1;
        return 0;
    }

    public static List<SWCPoint> computeConvexHull(List<SWCPoint> points)
    {
        int n = points.size();

        SWCPoint[] p = points.toArray(new SWCPoint[points.size()]);
        int minI = 0;
        for (int i = 1; i < n; i++)
            if (p[i].y() < p[minI].y() || p[i].y() == p[minI].y() && p[i].x() < p[minI].x())
                minI = i;

        final SWCPoint p0 = p[minI];
        p[minI] = p[0];
        p[0] = p0;

        Arrays.sort(p, 1, n, new Comparator<SWCPoint>()
        {
            @Override
            public int compare(SWCPoint p1, SWCPoint p2)
            {
                int cr = Cross(p0, p1, p2);
                if (cr != 0)
                    return -cr;
                Double d1 = p0.distance(p1);
                Double d2 = p0.distance(p2);
                return d1.compareTo(d2);
            }
        });

        SWCPoint[] hull = new SWCPoint[n];
        hull[0] = p[0];
        hull[1] = p[1];
        int top = 2;
        for (int i = 2; i < n; i++)
        {
            while (top >= 2 && Cross(hull[top - 2], hull[top - 1], p[i]) < 0)
                top--;
            if (top >= 2 && Cross(hull[top - 2], hull[top - 1], p[i]) == 0)
                top--;
            hull[top++] = p[i];
        }

        List<SWCPoint> res = new ArrayList<SWCPoint>();
        for (int i = 0; i < top; i++)
            res.add(hull[i]);
        return res;
    }

    /**
     * Computes DelaunayTriangulation
     * input: words + wordPositions
     * result: points + edges
     * @param l2 Line point 2
     * @param x The point to get the distance of
     * @return Distance of x from l1->l2
     */
    public static void computeDelaunayTriangulation(List<? extends Object> words, Map<? extends Object, SWCRectangle> wordPositions, SWCPoint[] points, List<List<Integer>> edges)
    {
        for (int i = 0; i < words.size(); i++)
        {
            edges.add(i, new ArrayList<Integer>());
        }

        // System.out.println("Triangulation");
        for (int i = 0; i < words.size(); i++)
        {
            SWCRectangle temp = wordPositions.get(words.get(i));
            points[i] = new SWCPoint(temp.getCenterX(), temp.getCenterY());
        }

        // determine if i-j-k is a circle with no interior points
        for (int i = 0; i < words.size(); i++)
        {
            for (int j = i + 1; j < words.size(); j++)
            {
                for (int k = j + 1; k < words.size(); k++)
                {
                    boolean isTriangle = true;
                    for (int a = 0; a < words.size(); a++)
                    {
                        if (a == i || a == j || a == k)
                            continue;

                        if (pointInsideCircle(points[a], points[i], points[j], points[k]))
                        {
                            isTriangle = false;
                            break;
                        }
                    }

                    if (isTriangle)
                    {
                        if (!edges.get(i).contains(j))
                            edges.get(i).add(j);
                        if (!edges.get(i).contains(k))
                            edges.get(i).add(k);
                        if (!edges.get(j).contains(i))
                            edges.get(j).add(i);
                        if (!edges.get(j).contains(k))
                            edges.get(j).add(k);
                        if (!edges.get(k).contains(i))
                            edges.get(k).add(i);
                        if (!edges.get(k).contains(j))
                            edges.get(k).add(j);
                    }
                }
            }
        }
    }

    public static double computeArea(List<SWCPoint> points)
    {
        double a = 0, b = 0;
        int n = points.size();
        for (int i = 0; i < n; ++i)
        {
            a += points.get(i).x() * points.get((i + 1) % n).y();
            b += points.get(i).y() * points.get((i + 1) % n).x();
        }
        return (a - b) / 2;
    }

    // Is p inside the circle formed by triangle a-b-c?
    // TODO: merge with CustomPoint.in
    public static boolean pointInsideCircle(SWCPoint p, SWCPoint a, SWCPoint b, SWCPoint c)
    {
        //find center
        SWCPoint bb = new SWCPoint(b.x() - a.x(), b.y() - a.y());
        SWCPoint cc = new SWCPoint(c.x() - a.x(), c.y() - a.y());

        double delta = 2.0 * (bb.x() * cc.y() - bb.y() * cc.x());
        if (Math.abs(delta) < 1e-6)
            return false;

        double bdot = bb.x() * bb.x() + bb.y() * bb.y();
        double cdot = cc.x() * cc.x() + cc.y() * cc.y();
        double delta1 = cc.y() * bdot - bb.y() * cdot;
        double delta2 = bb.x() * cdot - cc.x() * bdot;

        SWCPoint cen = new SWCPoint(a.x() + delta1 / delta, a.y() + delta2 / delta);

        return cen.distance(p) <= cen.distance(a);
    }

    /**
     * Computes the distance of point x from the line through l1, l2
     * @param l1 Line point 1
     * @param l2 Line point 2
     * @param x The point to get the distance of
     * @return Distance of x from l1->l2
     */
    public static double pointToLineDistance(SWCPoint l1, SWCPoint l2, SWCPoint x)
    {
        SWCPoint bc = new SWCPoint(l2.x(), l2.y());
        bc.subtract(l1);
        SWCPoint ba = new SWCPoint(x.x(), x.y());
        ba.subtract(l1);

        double c1 = SWCPoint.VectorProduct(bc, ba);
        double c2 = SWCPoint.VectorProduct(bc, bc);
        double parameter = c1 / c2;
        SWCPoint res = new SWCPoint(ba.x(), ba.y());
        bc.scale(parameter);
        res.subtract(bc);
        return res.distance(0, 0);

        /*double normalLength = Math.hypot((l2.x() - l1.x()), (l2.y() - l1.y()));
        if (normalLength == 0) {
        	normalLength = 0.00001;
        }
        return Math.abs((x.x() - l1.x()) * (l2.y() - l1.y()) - (x.y() - l1.y()) * (l2.x() - l1.x())) / normalLength;*/
    }

    /**
     * Computes the distance of point x from the line through l1, l2
     * @param l1 Line point 1
     * @param l2 Line point 2
     * @param x The point to get the distance of
     * @return Distance of x from l1->l2
     */
    public static double rectToRectDistance(SWCRectangle rect1, SWCRectangle rect2)
    {
        double dx = difference(rect1.getMinX(), rect1.getMaxX(), rect2.getMinX(), rect2.getMaxX());
        double dy = difference(rect1.getMinY(), rect1.getMaxY(), rect2.getMinY(), rect2.getMaxY());

        assert (dx >= 0);
        assert (dy >= 0);

        return Math.sqrt(dx * dx + dy * dy);
    }

    private static double difference(double m1, double M1, double m2, double M2)
    {
        if (M1 <= m2)
            return m2 - M1;
        if (M2 <= m1)
            return m1 - M2;

        //return Math.min(M1, M2) - Math.max(m1, m2);
        return 0;
    }
}
