package edu.webapp.shared;

/**
 * @author spupyrev
 * Oct 11, 2014
 */
public class WCColorScheme
{
    private String name;
    private String description;

    public WCColorScheme(String name, String description)
    {
        this.name = name;
        this.description = description;
    }

    public String getName()
    {
        return name;
    }

    public String getDescription()
    {
        return description;
    }
}
