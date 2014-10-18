package edu.webapp.shared;

import java.io.Serializable;

/**
 * @author spupyrev
 * Oct 18, 2014
 */
public class WCSimilarityAlgo implements Serializable
{
    private static final long serialVersionUID = -2409769922300198356L;
    
    private String id;
    private String description;

    public WCSimilarityAlgo()
    {
    }

    public WCSimilarityAlgo(String id, String description)
    {
        this.id = id;
        this.description = description;
    }

    public String getId()
    {
        return id;
    }

    public String getDescription()
    {
        return description;
    }
}
