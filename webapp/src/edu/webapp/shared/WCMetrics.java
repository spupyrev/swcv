package edu.webapp.shared;

import java.io.Serializable;

/**
 * @author spupyrev
 * Jan 14, 2014
 */
public class WCMetrics implements Serializable
{
    private static final long serialVersionUID = -7224326913714675767L;

    private double adjacencies;
    private double distortion;
    private double space;
    private double uniformity;
    private double aspectRatio;

    public WCMetrics()
    {
    }

    public double getAdjacencies()
    {
        return adjacencies;
    }

    public void setAdjacencies(double adjacencies)
    {
        this.adjacencies = adjacencies;
    }

    public double getDistortion()
    {
        return distortion;
    }

    public void setDistortion(double distortion)
    {
        this.distortion = distortion;
    }

    public double getSpace()
    {
        return space;
    }

    public void setSpace(double space)
    {
        this.space = space;
    }

    public double getUniformity()
    {
        return uniformity;
    }

    public void setUniformity(double uniformity)
    {
        this.uniformity = uniformity;
    }

    public double getAspectRatio()
    {
        return aspectRatio;
    }

    public void setAspectRatio(double aspectRatio)
    {
        this.aspectRatio = aspectRatio;
    }

}
