package edu.cloudy.ui;

import edu.cloudy.clustering.IClusterAlgo;
import edu.cloudy.colors.ClusterColorScheme;
import edu.cloudy.colors.IColorScheme;
import edu.cloudy.colors.RandomColorScheme;
import edu.cloudy.layout.LayoutAlgo;
import edu.cloudy.layout.StarForestAlgoOld;
import edu.cloudy.metrics.AdjacenciesMetric;
import edu.cloudy.metrics.ProximityMetric;
import edu.cloudy.nlp.Word;
import edu.cloudy.nlp.WordPair;
import edu.cloudy.utils.FontUtils;
import edu.cloudy.utils.GeometryUtils;
import edu.cloudy.utils.SWCPoint;
import edu.cloudy.utils.SWCRectangle;

import javax.swing.JPanel;
import javax.swing.Timer;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

public class WordCloudPanel extends JPanel implements ActionListener
{
    private static final long serialVersionUID = -3332798140563946847L;

    private static final double ystretch = 1;
    private static final double offset = 10;

    private boolean showRectangles = true;
    private boolean showConvexHull = false;
    private boolean showAdjacencies = false;
    private boolean showProximity = true;
    private boolean showWords = false;

    private volatile List<Word> words;
    private LayoutAlgo algo;

    private double scale;
    private double shiftX;
    private double shiftY;

    private IColorScheme wordColors;

    private double actualWidth;
    private double actualHeight;

    private Timer timer;

    public WordCloudPanel(List<Word> words, LayoutAlgo algo, IClusterAlgo clusterAlgo, IColorScheme colorScheme)
    {
        this.algo = algo;
        this.words = words;
        if (colorScheme != null)
            wordColors = colorScheme;
        else
            wordColors = (clusterAlgo != null ? new ClusterColorScheme(clusterAlgo, words) : new RandomColorScheme());

        this.setBackground(Color.WHITE);
        timer = new Timer(100, this);
    }

    @Override
    public void paintComponent(Graphics g)
    {
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D)g;

        if (!timer.isRunning())
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
                drawProximity(g2);

            //timer.start();
        }
        else
        {
            //ContextPreservingAlgo an = (ContextPreservingAlgo) algo;
            StarForestAlgoOld an = (StarForestAlgoOld)algo;
            an.doIteration(1);
            drawRectangles(g2);
            //drawDelaunay(g2, an.getDelaunay());
        }
    }

    private void drawRectangles(Graphics2D g2)
    {
        double maxX = Double.MIN_VALUE;
        double minX = Double.MAX_VALUE;
        double maxY = Double.MIN_VALUE;
        double minY = Double.MAX_VALUE;

        for (Word w : words)
        {
            SWCRectangle swcRect = algo.getWordRectangle(w);
            swcRect = transformRect(swcRect);

            maxX = Math.max(maxX, swcRect.getMaxX());
            maxY = Math.max(maxY, swcRect.getMaxY());
            minX = Math.min(minX, swcRect.getMinX());
            minY = Math.min(minY, swcRect.getMinY());

            Rectangle2D rect = createRectangle2D(swcRect);

            if (showRectangles)
            {
                g2.setColor(new Color(238, 233, 233));
                g2.fill(rect);
                g2.setColor(Color.black);
                g2.draw(rect);
            }

            if (showWords)
                drawTextInBox(g2, w.word, wordColors.getColor(w), createRectangle2D(algo.getWordRectangle(w)), rect);
        }

        actualWidth = maxX - minX;
        actualHeight = maxY - minY;
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

        double panelWidth = getSize().getWidth() - 2 * offset;
        double panelHeight = getSize().getHeight() - 2 * offset;
        scale = panelWidth / (maxX - minX);
        scale = Math.min(scale, panelHeight / (maxY - minY));

        shiftX = -1 * minX + offset / scale;
        shiftY = -1 * minY + offset / scale;
    }

    private void drawTextInBox(Graphics2D g2, String text, Color color, Rectangle2D box, Rectangle2D rect)
    {
        Font font = FontUtils.getFont();
        FontRenderContext frc = g2.getFontRenderContext();

        //get a new position for the text
        GlyphVector gv = font.layoutGlyphVector(frc, text.toCharArray(), 0, text.length(), Font.LAYOUT_LEFT_TO_RIGHT);
        Rectangle2D bb = gv.getPixelBounds(frc, 0, 0);
        double sx = box.getWidth() / bb.getWidth();
        double sy = box.getHeight() / bb.getHeight();
        double x = box.getCenterX() - bb.getCenterX() * sx;
        double y = box.getCenterY() - bb.getCenterY() * sy;
        x = transformX(x);
        y = transformY(y);

        //find correct font size
        double scaleX = rect.getWidth() / bb.getWidth();
        double scaleY = rect.getHeight() / bb.getHeight();

        //drawing words a bit less than needed to avoid overlaps
        scaleX /= 1.05;
        scaleY /= 1.05;

        //assert (Math.abs(scaleX - scaleY) < 1e-6);
        AffineTransform at = new AffineTransform(scaleX, 0, 0, scaleY, 0, 0);
        Font deriveFont = font.deriveFont(at);
        g2.setFont(deriveFont);
        g2.setColor(color);

        //draw the label
        gv = deriveFont.layoutGlyphVector(frc, text.toCharArray(), 0, text.length(), Font.LAYOUT_LEFT_TO_RIGHT);
        g2.drawGlyphVector(gv, (float)x, (float)y);
    }

    private void drawConvexHull(Graphics2D g2)
    {
        List<SWCPoint> points = new ArrayList<SWCPoint>();
        for (Word w : words)
        {
            SWCRectangle rect = algo.getWordRectangle(w);
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
            SWCRectangle word1 = transformRect(algo.getWordRectangle(wp.getFirst()));
            SWCRectangle word2 = transformRect(algo.getWordRectangle(wp.getSecond()));

            g2.drawLine((int)word1.getCenterX(), (int)word1.getCenterY(), (int)word2.getCenterX(), (int)word2.getCenterY());
        }

    }

    private void drawProximity(Graphics2D g2)
    {
        List<WordPair> adjacent = new ProximityMetric().getCloseWords(words, algo);

        for (WordPair wp : adjacent)
        {
            SWCRectangle word1 = transformRect(algo.getWordRectangle(wp.getFirst()));
            SWCRectangle word2 = transformRect(algo.getWordRectangle(wp.getSecond()));

            g2.drawLine((int)word1.getCenterX(), (int)word1.getCenterY(), (int)word2.getCenterX(), (int)word2.getCenterY());
        }

    }

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

    private SWCRectangle transformRect(SWCRectangle rect)
    {
        SWCRectangle ret = new SWCRectangle();
        ret.setRect(transformX(rect.getX()), transformY(rect.getY()), scale * rect.getWidth(), scale * rect.getHeight());
        return ret;
    }

    private Rectangle2D createRectangle2D(SWCRectangle swcRect)
    {
        return new Rectangle.Double(swcRect.getX(), swcRect.getY(), swcRect.getWidth(), swcRect.getHeight());
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

    @Override
    public void actionPerformed(ActionEvent e)
    {
        this.repaint();
        this.revalidate();
    }

    public double getActualWidth()
    {
        return actualWidth;
    }

    public double getActualHeight()
    {
        return actualHeight;
    }

    public void setShowWords(boolean b)
    {
        this.showWords = b;
    }
}
