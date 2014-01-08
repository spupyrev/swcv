package com.swcwebapp.server.readers;

import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import java.io.StringWriter;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(new URL("https://gdata.youtube.com/feeds/api/videos/" + id + "/comments").openStream());

            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer transformer = tf.newTransformer();
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            StringWriter writer = new StringWriter();
            transformer.transform(new DOMSource(doc), new StreamResult(writer));

            text = writer.getBuffer().toString().replaceAll("\n|\r", " ").replaceAll("\\<.*?\\>", "");
            return true;
        }
        catch (Exception e)
        {
            //e.printStackTrace();
            return false;
        }
    }

    public String getText(String input)
    {
        return text;
    }

}
