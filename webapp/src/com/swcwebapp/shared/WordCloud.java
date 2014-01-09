package com.swcwebapp.shared;

import java.io.Serializable;

/**
 * @author spupyrev
 * Aug 17, 2013
 */
public class WordCloud implements Serializable
{
    private static final long serialVersionUID = 8810113025963123088L;

    private String name;
    private String svg;
    private int width;
    private int height;

    private String settings;
    private int wordCount;
    private double adjacencies;
    private double distortion;
    private double space;
    private double uniformity;
    private double aspectRatio;

    public WordCloud()
    {

    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getName()
    {
        return this.name;
    }

    public int getWordCount()
    {
        return wordCount;
    }

    public void setWordCount(int wordCount)
    {
        this.wordCount = wordCount;
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

    public String getSvg()
    {
        return svg;
    }

    public void setSvg(String svg)
    {
        this.svg = svg;
    }

    public int getWidth()
    {
        return width;
    }

    public void setWidth(int width)
    {
        this.width = width;
    }

    public int getHeight()
    {
        return height;
    }

    public void setHeight(int height)
    {
        this.height = height;
    }

    public String getSettings()
    {
        return settings;
    }

    public void setSettings(String settings)
    {
        this.settings = settings;
    }

}
