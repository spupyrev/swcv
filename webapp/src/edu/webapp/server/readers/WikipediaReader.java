package edu.webapp.server.readers;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * @author spupyrev
 * Oct 25, 2014
 */
public class WikipediaReader implements IDocumentReader
{
    private String text;

    public WikipediaReader()
    {
    }

    public boolean isConnected(String input)
    {
        input = input.trim();
        input = fixUrlWithoutProtocol(input);

        URL url = null;
        String page = "";
        try
        {
            url = new URL(input);
            String host = url.getHost();
            if (!host.endsWith("wikipedia.org"))
                return false;

            String path = url.getPath();
            if (!path.startsWith("/wiki/"))
                return false;
            page = path.substring(6);
        }
        catch (MalformedURLException e)
        {
            return false;
        }

        return connect(page);
    }

    private boolean connect(String page)
    {
        String query = constructQuery(page);
        return downloadContent(query);
    }

    private boolean downloadContent(String query)
    {
        try
        {
            InputStream is = new URL(query).openStream();
            try
            {
                StringWriter writer = new StringWriter();
                IOUtils.copy(is, writer);
                JSONObject json = new JSONObject(writer.toString());
                json = json.getJSONObject("query");
                json = json.getJSONObject("pages");
                
                //System.out.println(json.names());

                JSONArray arr = json.names();
                StringBuffer sb = new StringBuffer();
                for (int i = 0; i < arr.length(); i++)
                {
                    JSONObject tmp = json.getJSONObject(arr.getString(i)); 
                    String en = tmp.getString("extract");
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

    public String getText(String url)
    {
        return text;
    }

    private String constructQuery(String wikiPage)
    {
        return "http://en.wikipedia.org/w/api.php?action=query&prop=extracts&format=json&exsectionformat=plain&explaintext&titles=" + wikiPage;
    }

    private String fixUrlWithoutProtocol(String url)
    {
        if (!url.toLowerCase().matches("^\\w+://.*"))
            return "http://" + url;
        return url;
    }
}
