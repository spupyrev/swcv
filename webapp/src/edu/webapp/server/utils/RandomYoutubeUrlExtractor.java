package edu.webapp.server.utils;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.io.StringWriter;
import java.net.URL;
import java.util.Random;

/**
 * @author spupyrev
 * Nov 23, 2013
 */
public class RandomYoutubeUrlExtractor
{
    private static final Random rnd = new Random();

    public static String getRandomUrl()
    {
        try
        {
            String url = "https://gdata.youtube.com/feeds/api/standardfeeds/most_popular?v=2&alt=json&max-results=10";
            InputStream is = new URL(url).openStream();
            try
            {
                StringWriter writer = new StringWriter();
                IOUtils.copy(is, writer);
                JSONObject json = new JSONObject(writer.toString());
                json = json.getJSONObject("feed");

                JSONArray arr = json.getJSONArray("entry");
                JSONObject entry = extractRandomEntry(arr);
                entry = entry.getJSONObject("media$group").getJSONObject("yt$videoid");
                String id = entry.getString("$t");

                return "https://www.youtube.com/watch?v=" + id;
            }
            finally
            {
                is.close();
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        
        return "https://www.youtube.com";
    }

    private static JSONObject extractRandomEntry(JSONArray arr) throws JSONException
    {
        int id = rnd.nextInt(arr.length());
        return arr.getJSONObject(id);
    }

}
