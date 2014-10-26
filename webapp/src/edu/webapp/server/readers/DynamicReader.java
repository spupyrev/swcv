package edu.webapp.server.readers;

import edu.cloudy.nlp.ContextDelimiter;

/**
 * @author spupyrev
 * 
 * Check if the input text contains two inputs for the "contrast 2 clouds" feature
 * Example Input:
 *   dynamic: google.com
 *   dynamic: bing.com
 */
public class DynamicReader implements IDocumentReader
{
    private String text1;
    private String text2;

    public boolean isConnected(String input)
    {
        if (input.startsWith("dynamic:"))
        {
            String[] sources = input.split("dynamic:");
            if (sources.length != 3)// empty first, source1 second, source2 third
                return false;

            text1 = new DocumentExtractor(sources[1]).getReader().getText(sources[1]);
            text2 = new DocumentExtractor(sources[2]).getReader().getText(sources[2]);

            return true;
        }
        return false;
    }

    public String getText(String input)
    {
        return text1 + ContextDelimiter.DYNAMIC_DELIMITER_TEXT + text2;
    }

    public String getText1()
    {
        return text1;
    }

    public String getText2()
    {
        return text2;
    }
}
