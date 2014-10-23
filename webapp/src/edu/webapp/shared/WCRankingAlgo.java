package edu.webapp.shared;

import java.io.Serializable;

/**
 * @author spupyrev
 * Oct 18, 2014
 */
public class WCRankingAlgo implements Serializable
{
    private static final long serialVersionUID = 5057110536867524542L;

    private String id;
    private String description;
    private boolean englishOnly;

    public WCRankingAlgo()
    {
    }

    public WCRankingAlgo(String id, String description, boolean englishOnly)
    {
        this.id = id;
        this.description = description;
        this.englishOnly = englishOnly;
    }

    public String getId()
    {
        return id;
    }

    public String getDescription()
    {
        return description;
    }

    public boolean isEnglishOnly()
    {
        return englishOnly;
    }

}
