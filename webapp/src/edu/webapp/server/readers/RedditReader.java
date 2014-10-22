package edu.webapp.server.readers;

import edu.cloudy.nlp.ContextDelimiter;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RedditReader implements IDocumentReader, ISentimentReader
{
    private String url;
    private String regex = "https?://(?:[0-9A-Za-z-]+\\.)?reddit\\.com/r/\\S+/comments/(\\S+)(/\\?limit=\\d+)?";
    private String text;
    private Document doc;

    public RedditReader()
    {
    }

    public boolean isConnected(String input)
    {
        this.url = input;
        Pattern regpat = Pattern.compile(regex);
        Matcher matcher = regpat.matcher(this.url);
        if (matcher.find())
        {
            modifyRequest(500);
            try
            {
                doc = Jsoup.connect(url).timeout(15000).get();
                this.getComments();
                return true;
            }
            catch (IOException e)
            {
                return false;
            }
        }
        else
        {
            return false;
        }
    }

    private void modifyRequest(int count)
    {
        if (url.endsWith("/"))
        {
            url += "?limit=500";
        }
        else
        {
            int index;
            if ((index = url.indexOf("?limit=")) == -1)
            {
                url += "/?limit=500";
            }
            else
            {
                url = url.substring(0, index);
                url += "?limit=500";
            }
        }

    }

    public String getText(String input)
    {
        return text;
    }

    public void getComments()
    {
        StringBuilder sb = new StringBuilder();
        Elements els = doc.getElementsByClass("comment");
        String html = new String();
        for (Element e : els)
        {
            html += e.html();
        }
        doc = Jsoup.parse(html);
        els = doc.getElementsByClass("md");
        for (Element e : els)
        {
            String tmp = e.text() + "." + ContextDelimiter.SENTIMENT_DELIMITER_TEXT + "\n";
            sb.append(tmp);
        }
        text = sb.toString();
    }
}
