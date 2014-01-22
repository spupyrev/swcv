package edu.webapp.server.readers;

/* LISCENSE INFO:
 * This program is used for wordcloud project
 *  (http://wordcloud.cs.arizona.edu)
 * @ University of Arizona.
 * This is under whatever liscense wordcloud project uses.
 * 
 * This program use Jsoup (http://jsoup.org/) which is open source under
 * MIT license (http://jsoup.org/license).
 * 
 * jsoup License:
 * The jsoup code-base (include source and compiled packages) are 
 * distributed under the open source MIT license as described below.
 * 
 * MIT License:
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 * 
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS
 * BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN
 * ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

/**
 * HTML reader for wordcloud (http://wordcloud.cs.arizona.edu)
 * 
 * @author O.W.O.D <team1@cs.arizona.edu>
 * 
 *         Constructors:
 *         HtmlReader() -- default constructor, initialize variables. use
 *         HtmlReader(url:String) if possible
 *         HtmlReader(url:String) -- take a URL and try to make connection to
 *         the URL.
 * 
 *         public methods:
 *         getUrl():String -- return the current URL used by program
 *         setUrl(url:String):void -- take a URL and try to make connection
 *         isConnected():boolean -- return the status of the connection
 *         getText():String -- return the text inside HTML from document got
 *         from the given URL
 * 
 *         Usage:
 *         1. use HtmlReader(String) to initialize the connection with a valid
 *         URL
 *         2. use isConnected() to check if the connection is made
 *         2-1. WARNING: Anything (except set/get URL) you might want to do
 *         after this step is depend on isConnected() returning true.
 *         3. getText() get the text in the HTML
 *         4. When needed use setUrl(String) to set a new URL
 *         5. Every time you change URL, run isConnected()!!!
 * 
 *         FOR developers:
 *         1. getDocument() returns Jsoup Document object got from the URL
 *         2. getHTML() returns string contains all the HTML from the URL
 * 
 */
public class HtmlReader implements IDocumentReader
{
    private Document doc;

    public HtmlReader()
    {
    }

    public boolean isConnected(String input)
    {
        if (!startsWithIgnoreCase(input, "http://") && !startsWithIgnoreCase(input, "https://"))
            return false;

        try
        {
            String url = new String();
            if (input.indexOf(' ') > 0)
            {
                url = input.substring(0, input.indexOf(' '));
            }
            if (input.indexOf('\n') > 0)
            {
                url = input.substring(0, input.indexOf(' '));
            }

            if (url.length() == 0)
                url = input;

            url = parseUrl(url);
            return connect(url);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * try to connect by Jsoup to the current URL.
     * and set the connection status
     * @param url 
     */
    private boolean connect(String url)
    {
        try
        {
            doc = Jsoup.connect(url).get();

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

    /**
     * WARNING: any connections you may want to make must be through HTTP/HTTPS
     * protocols, Make sure your URL begins with http/https. if the given URL
     * doesn't begin with http:// or https://, this method will try to fix it by
     * adding http:// in the front. But if the URL begin with ftp:// or other
     * protocols. this method will also try to add http:// in the front, which
     * means the URL won't lead to a correct connection. So look @ the NOTE!!!
     * 
     * NOTE: CHECK the connection using isConnected() before you do anything
     * after giving a URL!!!
     * 
     * @param url
     *            - begins with http/https
     */
    private String parseUrl(String url)
    {
        String s = url.trim();
        if (startsWithIgnoreCase(s, "http://") || startsWithIgnoreCase(s, "https://"))
        {
            return s;
        }
        else
        {
            return "http://" + s;
        }
    }

    /**
     * 
     * @param str
     * @param prefix
     * @return
     */
    private boolean startsWithIgnoreCase(String str, String prefix)
    {
        return startsWith(str, prefix, true);
    }

    /**
     * Use regionMatches instead of startWith
     * @param str
     * @param prefix
     * @param ignoreCase
     * @return
     */
    private static boolean startsWith(String str, String prefix, boolean ignoreCase)
    {
        if (str == null || prefix == null)
        {
            return (str == null && prefix == null);
        }
        if (prefix.length() > str.length())
        {
            return false;
        }
        return str.regionMatches(ignoreCase, 0, prefix, 0, prefix.length());
    }
}
