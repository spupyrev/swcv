package com.swcwebapp.server.readers;

/**
 * @author spupyrev
 * Nov 23, 2013
 */
public class DefaultReader implements IDocumentReader
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
