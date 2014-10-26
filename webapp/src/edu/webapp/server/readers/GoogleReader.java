package edu.webapp.server.readers;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;

import scala.collection.mutable.StringBuilder;

import java.io.InputStream;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Reads google search results
 * Example:
 *   google: search_term1 search_term2 size:100
 */
public class GoogleReader implements IDocumentReader, ISentimentReader
{
    private static final String UNWANTED_PATTERN = "\\s*http[s]?://\\S+\\s*";
    private static final int MAX_NUMBER_OF_RESULTS_PER_QUERY = 8;
    private static final int DEFAULT_NUMBER_OF_RESULTS = 32;

    private String text;

    public boolean isConnected(String input)
    {
        if (!input.startsWith("google:"))
            return false;

        SearchQuery sq = new SearchQuery(input.substring(8).trim());
        sq.parse();
        if (!sq.isValidQuery())
            return false;

        try
        {
            text = extractSearchResults(sq);
            return true;
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

    private String extractSearchResults(SearchQuery sq)
    {
        StringBuilder res = new StringBuilder();

        int downloadCount = 0;
        while (downloadCount < sq.getSize())
        {
            String sr = downloadSearchResuts(sq.getSearchPhrase(), downloadCount);
            if (sr.length() == 0)
                break;

            res.append(removeLinks(sr));
            downloadCount += MAX_NUMBER_OF_RESULTS_PER_QUERY;
        }

        return res.toString();
    }

    private String downloadSearchResuts(String queryPhrase, int start)
    {
        try
        {
            String query = constructQuery(queryPhrase, start);
            InputStream is = new URL(query).openStream();
            try
            {
                StringWriter writer = new StringWriter();
                IOUtils.copy(is, writer);
                JSONObject json = new JSONObject(writer.toString());
                json = json.getJSONObject("responseData");

                JSONArray arr = json.getJSONArray("results");
                StringBuffer sb = new StringBuffer();
                for (int i = 0; i < arr.length(); i++)
                {
                    JSONObject tmp = arr.getJSONObject(i);
                    String en = tmp.getString("content");

                    en = Jsoup.parse(en).text();

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
            e.printStackTrace();
            return "";
        }
    }

    private String constructQuery(String query, int start) throws UnsupportedEncodingException
    {
        query = URLEncoder.encode(query.trim(), "UTF-8");
        return "http://ajax.googleapis.com/ajax/services/search/web?v=1.0&q=" + query + "&rsz=" + MAX_NUMBER_OF_RESULTS_PER_QUERY + "&start=" + start;
    }

    private String removeLinks(String tweet)
    {
        return tweet.replaceAll(UNWANTED_PATTERN, "");
    }

    private static class SearchQuery
    {
        private String input;
        private String searchPhrase;

        private int size = DEFAULT_NUMBER_OF_RESULTS;

        public SearchQuery(String input)
        {
            this.input = input;
        }

        public void parse()
        {
            //ex: uofa size:500
            String sz = extractOption("size:(\\d+)");
            if (sz != null)
            {
                size = Integer.valueOf(sz);
                size = Math.min(size, 5000);
            }

            searchPhrase = input.trim();
        }

        private String extractOption(String pattern)
        {
            Pattern typePattern = Pattern.compile(pattern);
            Matcher typeMatcher = typePattern.matcher(input);
            if (typeMatcher.find())
            {
                String res = typeMatcher.group(1);
                input = typeMatcher.replaceAll("");
                return res;
            }

            return null;
        }

        public int getSize()
        {
            return size;
        }

        public String getSearchPhrase()
        {
            return searchPhrase;
        }

        public boolean isValidQuery()
        {
            return searchPhrase.length() > 0;
        }
    }
}
