package edu.webapp.server.readers;

import java.io.InputStream;
import java.io.StringWriter;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * @author spupyrev
 * Nov 23, 2013
 */
public class YouTubeReader implements IDocumentReader
{
    private static final String pattern = "https?://(?:[0-9A-Z-]+\\.)?(?:youtu\\.be/|youtube\\.com\\S*[^\\w\\-\\s])([\\w\\-]{11})(?=‌​[^\\w\\-]|$)(?![?=&+%\\w]*(?:['\"][^<>]*>|</a>))[?=&+%\\w]*";

    private String text;

    public boolean isConnected(String input)
    {
        // test if is a youtube id
        if (input.trim().length() == 11 && input.trim().indexOf(' ') == -1)
            return downloadComments(input);

        //test for a link
        Pattern compiledPattern = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);
        Matcher matcher = compiledPattern.matcher(input);
        if (matcher.find())
        {
            return downloadComments(matcher.group(1));
        }
        return false;
    }

    private boolean downloadComments(String id)
    {
        try
        {
            String url = "https://gdata.youtube.com/feeds/api/videos/" + id + "/comments?v=2&alt=json&max-results=50";
            InputStream is = new URL(url).openStream();
            try
            {
                StringWriter writer = new StringWriter();
                IOUtils.copy(is, writer);
                JSONObject json = new JSONObject(writer.toString());
                json = json.getJSONObject("feed");

                JSONArray arr = json.getJSONArray("entry");
                StringBuffer sb = new StringBuffer();
                for (int i = 0; i < arr.length(); i++)
                {
                    JSONObject tmp = arr.getJSONObject(i).getJSONObject("content");
                    String en = tmp.getString("$t");
                    if (en.length() > 0)
                        sb.append(en + ".\n");
                }

                text = sb.toString();
                return true;
            }
            finally
            {
                is.close();
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return false;
        }
    }

    public String getText(String input)
    {
        return text;
    }
}
