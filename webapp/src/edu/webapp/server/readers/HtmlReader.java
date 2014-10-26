package edu.webapp.server.readers;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * HTML reader
 */
public class HtmlReader implements IDocumentReader
{
    private Document doc;

    public HtmlReader()
    {
    }

    public boolean isConnected(String input)
    {
        input = input.trim();
        input = fixUrlWithoutProtocol(input);

        URL url = null;
        try
        {
            url = new URL(input);
        }
        catch (MalformedURLException e)
        {
            return false;
        }

        return connect(url);
    }

    /**
     * try to connect by Jsoup to the current URL.
     * and set the connection status
     */
    private boolean connect(URL url)
    {
        try
        {
            doc = Jsoup.parse(url, 60000);

            //adding this to separate sentences in the document
            for (Element t : doc.select("div"))
                t.append(".");

            for (Element t : doc.select("span"))
                t.append(".");

            for (Element t : doc.select("br"))
                t.append(".");

            for (Element t : doc.select("li"))
                t.append(".");

            return true;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return false;
        }
    }

    public String getText(String url)
    {
        return doc.text();
    }

    private String fixUrlWithoutProtocol(String url)
    {
        if (!url.toLowerCase().matches("^\\w+://.*"))
            return "http://" + url;
        return url;
    }

}
