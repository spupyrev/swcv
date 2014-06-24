package edu.cloudy.ui;

import edu.cloudy.colors.IColorScheme;
import edu.cloudy.layout.LayoutAlgo;
import edu.cloudy.layout.MDSWithFDPackingAlgo;
import edu.cloudy.layout.PackingCostCalculator;
import edu.cloudy.metrics.AdjacenciesMetric;
import edu.cloudy.metrics.ProximityMetric;
import edu.cloudy.nlp.Word;
import edu.cloudy.nlp.WordPair;
import edu.cloudy.utils.FontUtils;
import edu.cloudy.utils.GeometryUtils;
import edu.cloudy.utils.SWCPoint;
import edu.cloudy.utils.SWCRectangle;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

/**
 * @author spupyrev
 * Jun 23, 2014
 * 
 * renders the words into Graphics2D
 */
public class WordCloudRenderer
{
    private static final double ystretch = 1;
    private static final double offset = 10;

    private double scaleFactor;
    private double shiftX;
    private double shiftY;

    private boolean showRectangles = true;
    private boolean showConvexHull = false;
    private boolean showAdjacencies = false;
    private boolean showProximity = false;
    private boolean showWords = true;

    private volatile List<Word> words;
    private LayoutAlgo algo;

    private IColorScheme colorScheme;

    private double actualWidth;
    private double actualHeight;

    private double screenWidth;
    private double screenHeight;

    public WordCloudRenderer(List<Word> words, LayoutAlgo algo, IColorScheme colorScheme, double screenWidth, double screenHeight)
    {
        this.algo = algo;
        this.words = words;
        this.colorScheme = colorScheme;
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
    }

    public void render(Graphics2D g2)
    {
        if (words.isEmpty())
            return;

        computeShiftAndStretchFactors();

        drawRectangles(g2);

        if (showConvexHull)
        {
            drawConvexHull(g2);
        }

        if (showAdjacencies)
        {
            drawAdjacencies(g2);
        }

        if (showProximity)
        {
            drawProximity(g2);
        }

        drawBoundingBox(g2);
    }

    private void drawRectangles(Graphics2D g2)
    {
        List<SWCRectangle> allRects = new ArrayList();
        for (Word w : words)
        {
            //SWCRectangle rect5 = algo.getWordRectangle(w);
            //SWCRectangle swcRect = bbg.getBoundingBox(w, w.weight);
            //swcRect.moveTo(rect5.getX(), rect5.getY());

            SWCRectangle positionOnScreen = transformRect(algo.getWordPosition(w));

            allRects.add(positionOnScreen);

            if (showRectangles)
            {
                Rectangle2D rect2D = createRectangle2D(positionOnScreen);
                g2.setColor(new Color(238, 233, 233));
                g2.fill(rect2D);
                g2.setColor(Color.black);
                g2.draw(rect2D);
            }

            if (showWords)
            {
                drawTextInBox(g2, w.word, colorScheme.getColor(w), positionOnScreen);
            }
        }

        actualWidth = maxX(allRects) - minX(allRects);
        actualHeight = maxY(allRects) - minY(allRects);
    }

    private void computeShiftAndStretchFactors()
    {
        List<SWCRectangle> allRects = new ArrayList();

        for (Word w : words)
        {
            //SWCRectangle rect5 = algo.getWordRectangle(w);
            //SWCRectangle rect = bbg.getBoundingBox(w, w.weight);
            //rect.moveTo(rect5.getX(), rect5.getY());

            SWCRectangle rect = algo.getWordPosition(w);
            allRects.add(rect);
        }

        double panelWidth = screenWidth - 2 * offset;
        double panelHeight = screenHeight - 2 * offset;

        scaleFactor = panelWidth / (maxX(allRects) - minX(allRects));
        scaleFactor = Math.min(scaleFactor, panelHeight / (maxY(allRects) - minY(allRects)));

        shiftX = -1 * minX(allRects) + offset / scaleFactor;
        shiftY = -1 * minY(allRects) + offset / scaleFactor;
    }

    private void drawTextInBox(Graphics2D g2, String text, Color color, SWCRectangle positionOnScreen)
    {
        Font font = FontUtils.getFont();
        FontRenderContext frc = g2.getFontRenderContext();

        //bounding box of the word
        GlyphVector gv2 = font.layoutGlyphVector(frc, text.toCharArray(), 0, text.length(), Font.LAYOUT_LEFT_TO_RIGHT);
        Rectangle2D bb = gv2.getPixelBounds(frc, 0, 0);

        //find correct font size
        double scaleX = positionOnScreen.getWidth() / bb.getWidth();
        double scaleY = positionOnScreen.getHeight() / bb.getHeight();

        //get a new position for the text
        double x = positionOnScreen.getX() - bb.getX() * scaleX;
        double y = positionOnScreen.getY() - bb.getY() * scaleY;

        //preparing font
        AffineTransform at = new AffineTransform(scaleX, 0, 0, scaleY, 0, 0);
        Font deriveFont = font.deriveFont(at);
        g2.setFont(deriveFont);
        g2.setColor(color);

        //draw the label
        GlyphVector gv = deriveFont.layoutGlyphVector(frc, text.toCharArray(), 0, text.length(), Font.LAYOUT_LEFT_TO_RIGHT);
        g2.drawGlyphVector(gv, (float)x, (float)y);
    }

    private void drawConvexHull(Graphics2D g2)
    {
        List<SWCPoint> points = new ArrayList<SWCPoint>();
        for (Word w : words)
        {
            SWCRectangle rect = algo.getWordPosition(w);
            rect = transformRect(rect);
            points.add(new SWCPoint(rect.getMinX(), rect.getMinY()));
            points.add(new SWCPoint(rect.getMaxX(), rect.getMinY()));
            points.add(new SWCPoint(rect.getMinX(), rect.getMaxY()));
            points.add(new SWCPoint(rect.getMaxX(), rect.getMaxY()));
        }

        List<SWCPoint> ch = points;
        ch = GeometryUtils.computeConvexHull(ch);
        for (int i = 0; i < ch.size(); i++)
        {
            SWCPoint p1 = ch.get(i);
            SWCPoint p2 = ch.get((i + 1) % ch.size());

            g2.setColor(Color.red);
            g2.drawLine((int)p1.x(), (int)p1.y(), (int)p2.x(), (int)p2.y());
        }
    }

    private void drawAdjacencies(Graphics2D g2)
    {
        List<WordPair> adjacent = new AdjacenciesMetric().getCloseWords(words, algo);

        for (WordPair wp : adjacent)
        {
            SWCRectangle word1 = transformRect(algo.getWordPosition(wp.getFirst()));
            SWCRectangle word2 = transformRect(algo.getWordPosition(wp.getSecond()));

            g2.drawLine((int)word1.getCenterX(), (int)word1.getCenterY(), (int)word2.getCenterX(), (int)word2.getCenterY());
        }

    }

    private void drawProximity(Graphics2D g2)
    {
        List<WordPair> adjacent = new ProximityMetric().getCloseWords(words, algo);

        for (WordPair wp : adjacent)
        {
            SWCRectangle word1 = transformRect(algo.getWordPosition(wp.getFirst()));
            SWCRectangle word2 = transformRect(algo.getWordPosition(wp.getSecond()));

            g2.drawLine((int)word1.getCenterX(), (int)word1.getCenterY(), (int)word2.getCenterX(), (int)word2.getCenterY());
        }

    }

    @SuppressWarnings("unused")
    private void drawDelaunay(Graphics2D g2, List<SWCRectangle> delaunay)
    {
        for (int i = 0; i < delaunay.size(); i += 2)
        {
            SWCRectangle rect = delaunay.get(i);
            SWCRectangle rect2 = delaunay.get(i + 1);
            rect = transformRect(rect);
            rect2 = transformRect(rect2);

            g2.setColor(Color.green);
            g2.drawLine((int)rect.getCenterX(), (int)rect.getCenterY(), (int)rect2.getCenterX(), (int)rect2.getCenterY());
        }
    }

    private void drawBoundingBox(Graphics2D g2)
    {
        if (algo instanceof MDSWithFDPackingAlgo)
        {
            SWCRectangle rect = transformRect(PackingCostCalculator.bbox);
            Rectangle2D rect2D = createRectangle2D(rect);
            g2.setColor(new Color(238, 233, 233));
            g2.setColor(Color.black);
            g2.draw(rect2D);
        }
    }

    private SWCRectangle transformRect(SWCRectangle rect)
    {
        SWCRectangle ret = new SWCRectangle();
        ret.setRect(transformX(rect.getX()), transformY(rect.getY()), scaleFactor * rect.getWidth(), scaleFactor * rect.getHeight());
        return ret;
    }

    private Rectangle2D createRectangle2D(SWCRectangle swcRect)
    {
        return new Rectangle.Double(swcRect.getX(), swcRect.getY(), swcRect.getWidth(), swcRect.getHeight());
    }

    private double minX(List<SWCRectangle> rects)
    {
        double minX = rects.get(0).getMinX();
        for (SWCRectangle rect : rects)
            minX = Math.min(minX, rect.getMinX());

        return minX;
    }

    private double maxX(List<SWCRectangle> rects)
    {
        double maxX = rects.get(0).getMaxX();
        for (SWCRectangle rect : rects)
            maxX = Math.max(maxX, rect.getMaxX());

        return maxX;
    }

    private double minY(List<SWCRectangle> rects)
    {
        double minY = rects.get(0).getMinY();
        for (SWCRectangle rect : rects)
            minY = Math.min(minY, rect.getMinY());

        return minY;
    }

    private double maxY(List<SWCRectangle> rects)
    {
        double maxY = rects.get(0).getMaxY();
        for (SWCRectangle rect : rects)
            maxY = Math.max(maxY, rect.getMaxY());

        return maxY;
    }

    private double transformX(double x)
    {
        return scaleFactor * (shiftX + x);
    }

    private double transformY(double y)
    {
        return ystretch * scaleFactor * (shiftY + y);
    }

    public void setShowRectangles(boolean showRectangles)
    {
        this.showRectangles = showRectangles;
    }

    public void setShowAdjacencies(boolean set)
    {
        this.showAdjacencies = set;
    }

    public void setShowProximity(boolean set)
    {
        this.showProximity = set;
    }

    public void setShowConvexHull(boolean showConvexHull)
    {
        this.showConvexHull = showConvexHull;
    }

    public void setShowWords(boolean b)
    {
        this.showWords = b;
    }

    public boolean isShowRectangles()
    {
        return showRectangles;
    }

    public boolean isShowConvexHull()
    {
        return showConvexHull;
    }

    public boolean isShowAdjacencies()
    {
        return showAdjacencies;
    }

    public boolean isShowProximity()
    {
        return showProximity;
    }

    public boolean isShowWords()
    {
        return showWords;
    }

    public double getActualWidth()
    {
        return actualWidth;
    }

    public double getActualHeight()
    {
        return actualHeight;
    }

}
