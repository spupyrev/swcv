package edu.test;

import javax.swing.JFrame;
import javax.swing.JPanel;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;

/**
 * @author spupyrev 
 * Apr 30, 2013 
 * create and visualize a wordcloud for a document
 */
public class FontTests {

	public static void main(String argc[]) {
		new FontTests().run();
	}

	private void run() {
		new TestFrame("yyyyAAaai");
	}

	public static class TestFrame extends JFrame {
		private static final long serialVersionUID = 6602115306287717309L;

		public TestFrame(String text) {
			add(new TestPanel(text));

			setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			setExtendedState(JFrame.MAXIMIZED_BOTH);
			setVisible(true);
		}
	}

	public static class TestPanel extends JPanel {
		private static final long serialVersionUID = -3332798140563946847L;

		private String text;

		public TestPanel(String text) {
			this.text = text;
			this.setBackground(Color.WHITE);
		}

		@Override
		public void paintComponent(Graphics g) {
			super.paintComponent(g);
			Graphics2D g2 = (Graphics2D) g;
			g.setColor(Color.black);

			float x = 100;
			float y = 100;
			Font f = new Font("Arial", Font.PLAIN, 80);
			g.setFont(f);
			
			
			FontRenderContext frc = g2.getFontRenderContext();

			GlyphVector gv = f.layoutGlyphVector(frc, text.toCharArray(), 0, text.length(), Font.LAYOUT_LEFT_TO_RIGHT);
			Rectangle bb = gv.getPixelBounds(frc, x, y);

			g2.drawGlyphVector(gv, x, y);
			g2.draw(bb);
		}
	}

}
