package edu.test.misc;

import edu.cloudy.utils.FontUtils;

import javax.swing.JFrame;
import javax.swing.JPanel;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;

/**
 * @author spupyrev 
 * Apr 30, 2013 
 */
public class FontTests
{

    public static void main(String argc[])
    {
        new FontTests().run();
    }

    private void run()
    {
        new TestFrame("Computationi");
    }

    public static class TestFrame extends JFrame
    {
        private static final long serialVersionUID = 6602115306287717309L;

        public TestFrame(String text)
        {
            add(new TestPanel(text));

            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            setExtendedState(JFrame.MAXIMIZED_BOTH);
            setVisible(true);
        }
    }

    public static class TestPanel extends JPanel
    {
        private static final long serialVersionUID = -3332798140563946847L;

        private String text;

        public TestPanel(String text)
        {
            this.text = text;
            this.setBackground(Color.WHITE);
        }

        @Override
        public void paintComponent(Graphics g)
        {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D)g;
            g.setColor(Color.black);

            float x = 100;
            float y = 100;
            Font f = FontUtils.getFont();
            g.setFont(f);

            FontRenderContext frc = g2.getFontRenderContext();

            GlyphVector gv = f.layoutGlyphVector(frc, text.toCharArray(), 0, text.length(), Font.LAYOUT_LEFT_TO_RIGHT);
            Rectangle2D bb = gv.getPixelBounds(frc, x, y);
            
            
            double scale = 2.91;
            scale = 1.3461;
            scale = 2;
            bb.setRect(bb.getX() - 250.5, bb.getY() + 150.566, bb.getWidth()*scale, bb.getHeight()*scale);

            drawTextInBox(g2, text, bb);
        }
        
        private void drawTextInBox(Graphics2D g2, String text, Rectangle2D positionOnScreen)
        {
            Font font = FontUtils.getFont();
            FontRenderContext frc = g2.getFontRenderContext();

            //bounding box of the word
            GlyphVector gv2 = font.layoutGlyphVector(frc, text.toCharArray(), 0, text.length(), Font.LAYOUT_LEFT_TO_RIGHT);
            Rectangle bb = gv2.getPixelBounds(frc, 0.0f, 0.0f);

            //find correct font size
            float scaleX = (float)(positionOnScreen.getWidth() / bb.getWidth());
            float scaleY = (float)(positionOnScreen.getHeight() / bb.getHeight());

            //get a new position for the text
            float x = (float)(positionOnScreen.getX() - bb.getX() * scaleX);
            float y = (float)(positionOnScreen.getY() - bb.getY() * scaleY);

            //preparing font
            AffineTransform at = new AffineTransform(scaleX, 0, 0, scaleY, 0, 0);
            Font deriveFont = font.deriveFont(at);
            g2.setFont(deriveFont);
            g2.setColor(Color.black);

            //draw the label
            GlyphVector gv = deriveFont.layoutGlyphVector(frc, text.toCharArray(), 0, text.length(), Font.LAYOUT_LEFT_TO_RIGHT);
            g2.drawGlyphVector(gv, x, y);
            g2.draw(positionOnScreen);
        }
        
    }

}
