package edu.test;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.io.StringWriter;
import java.net.URL;

/**
 * @author spupyrev
 * Jan 11, 2014
 */
public class YoutubeCommentsReaderTest
{

    public static void main(String[] args)
    {
        System.out.println(getComments("VW3vgJYYIok"));
    }

    public static String getComments(String id)
    {
        try
        {
            String url = "https://gdata.youtube.com/feeds/api/videos/" + id + "/comments?v=2&alt=json&max-results=50";
            InputStream is = new URL(url).openStream();
            try
            {
                StringWriter writer = new StringWriter();
                IOUtils.copy(is, writer);
                String text = writer.toString();
                JSONObject json = new JSONObject(text);

                JSONObject js = json.getJSONObject("feed");
                JSONArray arr = js.getJSONArray("entry");
                StringBuffer sb = new StringBuffer();
                for (int i = 0; i < arr.length(); i++)
                {
                    JSONObject tmp = arr.getJSONObject(i);
                    JSONObject tmp2 = tmp.getJSONObject("content");
                    String en = tmp2.getString("$t");
                    if (en.length() > 0)
                        sb.append(en + ".\n");
                }

                return sb.toString();
            }
            finally
            {
                is.close();
            }
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }
}
