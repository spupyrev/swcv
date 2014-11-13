package edu.test;

import edu.cloudy.geom.SWCRectangle;

import org.junit.Test;

import junit.framework.Assert;

/**
 * @author spupyrev
 * Nov 13, 2014
 */
public class GeometryTest
{
    @Test
    public void testIntersections()
    {
        SWCRectangle rect1 = new SWCRectangle(0, 0, 4, 3);
        
        Assert.assertTrue(rect1.intersects(new SWCRectangle(3, 2, 3, 2)));
        Assert.assertTrue(rect1.intersects(new SWCRectangle(3, 2, 0.1, 0.1)));
        Assert.assertFalse(rect1.intersects(new SWCRectangle(4.01, 2, 0.1, 0.1)));
        Assert.assertFalse(rect1.intersects(new SWCRectangle(3, 3.01, 10, 5)));
        Assert.assertTrue(rect1.intersects(new SWCRectangle(-1, -1, 1.01, 1.01)));
        Assert.assertFalse(rect1.intersects(new SWCRectangle(-1, -1, 0.99, 1.01)));
        
        Assert.assertFalse(rect1.intersects(new SWCRectangle(3, 2, 3, 2), 1));
        Assert.assertTrue(rect1.intersects(new SWCRectangle(3, 2, 3, 2), 0.99));
    }
}
