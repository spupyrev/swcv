package edu.webapp.shared;

import java.io.Serializable;

/**
 * @author spupyrev
 * Oct 18, 2014
 */
public class WCAspectRatio implements Serializable
{
    private static final long serialVersionUID = 7156735727410825303L;
    
    private String id;
    private String description;
    private double value;

    public WCAspectRatio()
    {
    }

    public WCAspectRatio(String id, String description, double value)
    {
        this.id = id;
        this.description = description;
        this.value = value;
    }

    public String getId()
    {
        return id;
    }

    public String getDescription()
    {
        return description;
    }

    public double getValue()
    {
        return value;
    }
}
