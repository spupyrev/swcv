package edu.webapp.shared;

import java.io.Serializable;

/**
 * @author spupyrev
 * Oct 18, 2014
 */
public class WCLayoutAlgo implements Serializable
{
    private static final long serialVersionUID = -2160648925353629200L;
    
    private String id;
    private String description;
    private String type;

    public WCLayoutAlgo()
    {
    }

    public WCLayoutAlgo(String id, String description, String type)
    {
        this.id = id;
        this.type = type;
        this.description = description;
    }

    public String getId()
    {
        return id;
    }

    public String getType()
    {
        return type;
    }

    public String getDescription()
    {
        return description;
    }
}
