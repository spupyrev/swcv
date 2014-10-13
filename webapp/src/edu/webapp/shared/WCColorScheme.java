package edu.webapp.shared;

import java.io.Serializable;

/**
 * @author spupyrev
 * Oct 11, 2014
 */
public class WCColorScheme implements Serializable
{
    private static final long serialVersionUID = 6330917225300922664L;
    
    private String name;
    private String description;
    private String type;

    public WCColorScheme()
    {
    }

    public WCColorScheme(String name, String type, String description)
    {
        this.name = name;
        this.type = type;
        this.description = description;
    }

    public String getName()
    {
        return name;
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
