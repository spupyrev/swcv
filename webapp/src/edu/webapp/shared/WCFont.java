package edu.webapp.shared;

import java.io.Serializable;

/**
 * @author spupyrev
 * Oct 6, 2014
 */
public class WCFont implements Serializable
{
    private static final long serialVersionUID = -3220087903777521831L;
    
    private String name;
    private String description;
    private boolean englishOnly;
    // Is it installed in the system by default?
    private boolean webSafe;

    public WCFont()
    {
    }

    public WCFont(String name, String description, boolean webSafe, boolean englishOnly)
    {
        this.name = name;
        this.description = description;
        this.webSafe = webSafe;
        this.englishOnly = englishOnly;
    }

    public String getName()
    {
        return name;
    }
    
    public String getDescription()
    {
        return description;
    }
    
    public boolean isEnglishOnly()
    {
        return englishOnly;
    }
    
    public boolean isWebSafe()
    {
        return webSafe;
    }
}
