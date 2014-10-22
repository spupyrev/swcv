package edu.webapp.server.readers;

/**
 * @author spupyrev
 * Nov 23, 2013
 */
public class DefaultReader implements IDocumentReader, ISentimentReader
{
    public boolean isConnected(String input)
    {
        return true;
    }

    public String getText(String input)
    {
        return input;
    }

}
