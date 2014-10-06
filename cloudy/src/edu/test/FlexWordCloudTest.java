package edu.test;

import edu.cloudy.ui.FlexWordlePanel;
import edu.cloudy.ui.WordCloudFrame;

import org.apache.batik.dom.GenericDOMImplementation;
import org.apache.batik.svggen.SVGGraphics2D;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;

import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

/**
 * @author spupyrev
 * Nov 8, 2013
 */
@SuppressWarnings("all")
public class FlexWordCloudTest
{
    public static void main(String[] argc)
    {
        new WordCloudFrame(new FlexWordlePanel());

        //buildSVG("cloud.svg");
    }

    private static void buildSVG(String selectedFile)
    {
        FlexWordlePanel panel = new FlexWordlePanel();
        // Get a DOMImplementation.
        DOMImplementation domImpl = GenericDOMImplementation.getDOMImplementation();

        // Create an instance of org.w3c.dom.Document.
        String svgNS = "http://www.w3.org/2000/svg";
        Document document = domImpl.createDocument(svgNS, "svg", null);

        // Create an instance of the SVG Generator.
        SVGGraphics2D svgGenerator = new SVGGraphics2D(document);

        // Ask to render into the SVG Graphics2D implementation.
        panel.draw(svgGenerator, 1024, 768);

        // Finally, stream out SVG to the standard output using
        // UTF-8 encoding.
        boolean useCSS = true; // we want to use CSS style attributes
        Writer out;
        try
        {
            out = new OutputStreamWriter(new FileOutputStream(selectedFile), "UTF-8");
            svgGenerator.stream(out, useCSS);
            out.close();
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }
}
