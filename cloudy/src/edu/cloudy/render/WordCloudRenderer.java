package edu.cloudy.render;

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
    private static final double offset = 10;

    private double scaleFactor;
    private double shiftX;
    private double shiftY;

    private boolean showRectangles = false;
    private boolean showText = true;

    private List<UIWord> words;

    private double actualWidth;
    private double actualHeight;

    private double width;
    private double height;

    public WordCloudRenderer(List<UIWord> words, double screenWidth, double screenHeight)
    {
        this.words = words;
        this.width = screenWidth;
        this.height = screenHeight;
    }

    public void render(Graphics2D g2)
    {
        if (words.isEmpty())
            return;

        computeShiftAndStretchFactors();

        renderWords(g2);
    }

    private void renderWords(Graphics2D g2)
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

            if (showText)
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

        double panelWidth = width - 2 * offset;
        double panelHeight = height - 2 * offset;

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
        return rects.stream().mapToDouble(rect -> rect.getMinX()).min().getAsDouble();
    }

    private double maxX(List<SWCRectangle> rects)
    {
        return rects.stream().mapToDouble(rect -> rect.getMaxX()).max().getAsDouble();
    }

    private double minY(List<SWCRectangle> rects)
    {
        return rects.stream().mapToDouble(rect -> rect.getMinY()).min().getAsDouble();
    }

    private double maxY(List<SWCRectangle> rects)
    {
        return rects.stream().mapToDouble(rect -> rect.getMaxY()).max().getAsDouble();
    }

    private double transformX(double x)
    {
        return scaleFactor * (shiftX + x);
    }

    private double transformY(double y)
    {
        return scaleFactor * (shiftY + y);
    }

    public void setShowRectangles(boolean showRectangles)
    {
        this.showRectangles = showRectangles;
    }

    public void setShowText(boolean b)
    {
        this.showText = b;
    }

    public boolean isShowRectangles()
    {
        return showRectangles;
    }

    public boolean isShowText()
    {
        return showText;
    }

    public double getActualWidth()
    {
        return actualWidth;
    }

    public double getActualHeight()
    {
        return actualHeight;
    }

    public double getWidth()
    {
        return width;
    }

    public double getHeight()
    {
        return height;
    }

}
