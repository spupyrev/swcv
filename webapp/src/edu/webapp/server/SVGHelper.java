package edu.webapp.server;

import de.tinloaf.cloudy.algos.LayoutAlgo;
import de.tinloaf.cloudy.text.Word;
import de.tinloaf.cloudy.utils.SWCRectangle;

import org.apache.batik.dom.svg.SVGDOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.svg.SVGTextElement;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class SVGHelper
{
    private static final double MAX_WIDTH = 800;
    private static final double MAX_HEIGHT = 600;

    private static final double ystretch = 1;
    private static final double offset = 10;

    @SuppressWarnings("unused")
    private boolean showRectangles = true;

    private List<Word> words;
    private LayoutAlgo algo;
    private double scale;
    private double shiftX;
    private double shiftY;

    private double actualWidth;
    private double actualHeight;

    public SVGHelper(List<Word> words, LayoutAlgo algo)
    {
        this.algo = algo;
        this.words = words;
    }

    public void paintComponent(Document document, Element svgRoot)
    {
        if (words.isEmpty())
        {
            svgRoot.setAttributeNS(null, "width", "100");
            svgRoot.setAttributeNS(null, "height", "100");
            return;
        }

        computeShiftAndStretchFactors();

        drawRectangles(document, svgRoot);

        // Set the width and height attributes on the root 'svg' element.
        //svgRoot.setAttributeNS(null, "width", "100");
        //svgRoot.setAttributeNS(null, "height", "100");
    }

    private void drawRectangles(Document document, Element svgRoot)
    {
        double maxX = Double.MIN_VALUE;
        double minX = Double.MAX_VALUE;
        double maxY = Double.MIN_VALUE;
        double minY = Double.MAX_VALUE;

        for (Word w : words)
        {
            SWCRectangle rect = algo.getWordRectangle(w);
            rect = transformRect(rect);

            maxX = Math.max(maxX, rect.getMaxX());
            maxY = Math.max(maxY, rect.getMaxY());
            minX = Math.min(minX, rect.getMinX());
            minY = Math.min(minY, rect.getMinY());

            //if (showRectangles)
            {
                //svgRoot.setColor(new Color(238, 233, 233));
                //svgRoot.fill(rect);
                //svgRoot.setColor(Color.black);
                //svgRoot.draw(rect);

                // Create the rectangle.
                Element rectangle = document.createElementNS(SVGDOMImplementation.SVG_NAMESPACE_URI, "rect");
                rectangle.setAttributeNS(null, "x", "" + rect.getX());
                rectangle.setAttributeNS(null, "y", "" + rect.getY());
                rectangle.setAttributeNS(null, "width", "" + rect.getWidth());
                rectangle.setAttributeNS(null, "height", "" + rect.getHeight());
                rectangle.setAttributeNS(null, "style", "stroke:black; fill:rgb(238,233,233);");

                // Attach the rectangle to the root 'svg' element.
                svgRoot.appendChild(rectangle);


                //System.out.println(text.getComputedTextLength());
            }

            drawTextInBox(document, svgRoot, w.word, getColor(w), algo.getWordRectangle(w), rect);
        }

        actualWidth = maxX - minX;
        actualHeight = maxY - minY;
    }

    private void drawTextInBox(Document document, Element svgRoot, String text, Color color, SWCRectangle box, SWCRectangle rect)
    {
        SVGTextElement textElement = (SVGTextElement)document.createElementNS(SVGDOMImplementation.SVG_NAMESPACE_URI, "text");
        textElement.setAttributeNS(null, "x", "" + rect.getX());
        textElement.setAttributeNS(null, "y", "" + rect.getY());
        textElement.setTextContent(text);

        // Attach the rectangle to the root 'svg' element.
        svgRoot.appendChild(textElement);
    }

    private Map<Word, Color> colors = new HashMap<Word, Color>();
    private Random rnd = new Random(123);

    private Color getColor(Word w)
    {
        if (!colors.containsKey(w))
        {
            int r = 0;
            int g = rnd.nextInt(178);
            int b = 56 + rnd.nextInt(200);
            colors.put(w, new Color(r, g, b));
        }

        return colors.get(w);
    }

    private void computeShiftAndStretchFactors()
    {
        // Get max x and y
        double maxX = Double.MIN_VALUE;
        double minX = Double.MAX_VALUE;
        double maxY = Double.MIN_VALUE;
        double minY = Double.MAX_VALUE;

        for (Word w : words)
        {
            SWCRectangle rect = algo.getWordRectangle(w);

            maxX = Math.max(maxX, rect.getMaxX());
            maxY = Math.max(maxY, rect.getMaxY());
            minX = Math.min(minX, rect.getMinX());
            minY = Math.min(minY, rect.getMinY());
        }

        double panelWidth = MAX_WIDTH - 2 * offset;
        double panelHeight = MAX_HEIGHT - 2 * offset;
        scale = panelWidth / (maxX - minX);
        scale = Math.min(scale, panelHeight / (maxY - minY));

        shiftX = -1 * minX + offset / scale;
        shiftY = -1 * minY + offset / scale;
    }

    private SWCRectangle transformRect(SWCRectangle rect)
    {
        return new SWCRectangle(transformX(rect.getX()), transformY(rect.getY()), scale * rect.getWidth(), scale * rect.getHeight());
    }

    private double transformX(double x)
    {
        return scale * (shiftX + x);
    }

    private double transformY(double y)
    {
        return ystretch * scale * (shiftY + y);
    }

    public void setShowRectangles(boolean showRectangles)
    {
        this.showRectangles = showRectangles;
    }

    public double getActualWidth()
    {
        return actualWidth;
    }

    public double getActualHeight()
    {
        return actualHeight;
    }

    private class Color
    {
        public Color(int r2, int g2, int b2)
        {
            r = r2;
            g = g2;
            b = b2;
        }

        @SuppressWarnings("unused")
        int r, g, b;
    }

}
