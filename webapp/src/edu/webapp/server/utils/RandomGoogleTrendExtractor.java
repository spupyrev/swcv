package edu.webapp.server.utils;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.InputStream;
import java.io.StringWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * @author spupyrev
 * Jan 11, 2014
 * 
 * gets a random trend from the webpage "http://www.google.com/trends/hottrends/atom/hourly"
 */
public class RandomGoogleTrendExtractor
{
    private static final Random rnd = new Random();

    public static String getRandomTrend()
    {
        try
        {
            String[] trends = downloadTrends();
            String trend = extractRandomTrend(trends);
            return "google: " + trend;
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    private static String[] downloadTrends() throws Exception
    {
        String query = constructQuery();
        InputStream is = new URL(query).openStream();
        try
        {
            StringWriter writer = new StringWriter();
            IOUtils.copy(is, writer);
            JSONObject json = new JSONObject(writer.toString());
            json = json.getJSONObject("responseData");
            json = json.getJSONObject("feed");

            JSONArray arr = json.getJSONArray("entries");
            List<String> res = new ArrayList();
            for (int i = 0; i < arr.length(); i++)
            {
                JSONObject tmp = arr.getJSONObject(i);
                String en = tmp.getString("content");

                Document doc = Jsoup.parse(en);
                for (Element t : doc.select("a"))
                    t.append(".");
                
                en = doc.text();
                res.addAll(Arrays.asList(en.split("\\.")));
            }

            return getNonEmptyTrends(res);
        }
        finally
        {
            is.close();
        }
    }

    private static String[] getNonEmptyTrends(List<String> res)
    {
        List<String> nl = new ArrayList();
        for (String s : res)
            if (s.length() > 0)
                nl.add(s);
        
        return nl.toArray(new String[] {});
    }

    private static String constructQuery()
    {
        return "https://ajax.googleapis.com/ajax/services/feed/load?v=1.0&q=http%3A%2F%2Fwww.google.com%2Ftrends%2Fhottrends%2Fatom%2Fhourly";
    }

    private static String extractRandomTrend(String[] trends)
    {
        int id = rnd.nextInt(trends.length);
        return trends[id];
    }
}
