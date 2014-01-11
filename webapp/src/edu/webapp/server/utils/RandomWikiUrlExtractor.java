package edu.webapp.server.utils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

/**
 * @author spupyrev
 * Jan 11, 2014
 */
public class RandomWikiUrlExtractor
{
    public static String getRandomWikiPage()
    {
        String url = "http://en.wikipedia.org/wiki/Special:random";

        try
        {
            Document doc = Jsoup.connect(url).get();
            return doc.baseUri();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return url;
        }
    }
}
