package edu.cloudy.ui;

import edu.cloudy.geom.SWCRectangle;
import edu.cloudy.utils.FontUtils;

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
    private boolean showWords = true;

    private volatile List<UIWord> words;

    private double actualWidth;
    private double actualHeight;

    private double screenWidth;
    private double screenHeight;

    public WordCloudRenderer(List<UIWord> words, double screenWidth, double screenHeight)
    {
        this.words = words;
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
    }

    public void render(Graphics2D g2)
    {
        if (words.isEmpty())
            return;

        computeShiftAndStretchFactors();

        drawRectangles(g2);
    }

    private void drawRectangles(Graphics2D g2)
    {
        List<SWCRectangle> allRects = new ArrayList();
        for (UIWord w : words)
        {
            SWCRectangle positionOnScreen = transformRect(w.getRectangle());
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
                drawTextInBox(g2, w.getText(), w.getColor(), positionOnScreen);
            }
        }

        actualWidth = maxX(allRects) - minX(allRects);
        actualHeight = maxY(allRects) - minY(allRects);
    }

    private void computeShiftAndStretchFactors()
    {
        List<SWCRectangle> allRects = new ArrayList();

        for (UIWord w : words)
        {
            SWCRectangle rect = w.getRectangle();
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
        //GlyphVector gv = deriveFont.layoutGlyphVector(frc, text.toCharArray(), 0, text.length(), Font.LAYOUT_LEFT_TO_RIGHT);
        //g2.drawGlyphVector(gv, (float)x, (float)y);
        g2.drawString(text, (float)x, (float)y);
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

    public void setShowWords(boolean b)
    {
        this.showWords = b;
    }

    public boolean isShowRectangles()
    {
        return showRectangles;
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
