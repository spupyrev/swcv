package edu.webapp.server.readers;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;

/**
 * @author spupyrev
 * Oct 25, 2014
 */
public class HTMLTest
{
    public static void main(String[] args)
    {
        String input = "http://en.wikipedia.org/wiki/Obancea_River";

        WikipediaReader reader = new WikipediaReader();
        System.out.println(reader.isConnected(input));
        System.out.println(reader.getText(input));

        try
        {
            Document doc = Jsoup.connect(input).get();

            fixSentences(doc);

            //System.out.println(doc.html());
            System.out.println(doc.text());
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    private static void fixSentences(Document doc)
    {
        for (Element t : doc.select("div"))
            t.append(".");

        for (Element t : doc.select("span"))
            t.append(".");

        for (Element t : doc.select("br"))
            t.append(".");

        for (Element t : doc.select("li"))
            t.append(".");
    }
}
