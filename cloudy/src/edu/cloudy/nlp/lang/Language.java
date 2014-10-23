package edu.cloudy.nlp.lang;

/**
 * @author spupyrev
 * Oct 22, 2014
 */
public class Language
{
    private String id;
    private String tokenFile;
    private String sentFile;
    private String stopwordsFile;

    public Language(String id, String tokenFile, String sentFile, String stopwordsFile)
    {
        this.id = id;
        this.tokenFile = tokenFile;
        this.sentFile = sentFile;
        this.stopwordsFile = stopwordsFile;
    }

    public String getId()
    {
        return id;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    public String getTokenFile()
    {
        return tokenFile;
    }

    public String getSentFile()
    {
        return sentFile;
    }

    public String getStopwordsFile()
    {
        return stopwordsFile;
    }

}
