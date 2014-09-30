package edu.cloudy.utils;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class WikipediaXMLReader
{
    private String filename;
    private List<String> texts;
    private Document dom;

    public WikipediaXMLReader(String filename)
    {
        this.filename = filename;
        this.texts = new ArrayList<String>();
    }

    public Iterator<String> getTexts()
    {
        return this.texts.iterator();
    }

    public void read()
    {
        parseXmlFile();
        parseDocument();
    }

    private void parseXmlFile()
    {
        //get the factory
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

        try
        {
            //Using factory get an instance of document builder
            DocumentBuilder db = dbf.newDocumentBuilder();

            File f = readUTF(filename);
            //parse using builder to get DOM representation of the XML file
            dom = db.parse(f);

        }
        catch (ParserConfigurationException pce)
        {
            pce.printStackTrace();
        }
        catch (SAXException se)
        {
            se.printStackTrace();
        }
        catch (IOException ioe)
        {
            ioe.printStackTrace();
        }
    }

    private File readUTF(String filename) throws IOException
    {
        File outputFile = new File("tmp.xml");
        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(filename), "UTF-8"));
        PrintWriter writer = new PrintWriter(outputFile);
        int read;
        char buf[] = new char[4096];
        while ((read = reader.read(buf)) != -1)
        {
            writer.write(buf, 0, read);
        }
        reader.close();
        writer.close();
        return outputFile;
    }

    private void parseDocument()
    {
        //get the root element
        Element docEle = dom.getDocumentElement();

        //get a nodelist of elements
        NodeList nl = docEle.getElementsByTagName("doc");
        if (nl != null && nl.getLength() > 0)
        {
            for (int i = 0; i < nl.getLength(); i++)
            {
                Element el = (Element)nl.item(i);

                texts.add(el.getFirstChild().getNodeValue());
            }
        }
    }
}
