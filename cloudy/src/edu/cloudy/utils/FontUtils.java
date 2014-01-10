package edu.cloudy.utils;

import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

/**
 * @author spupyrev
 * May 3, 2013
 */
public class FontUtils {
	private static FontProvider provider = null;

	private static FontProvider getProvider() {
		if (provider == null) {
			provider = new AWTFontProvider();
		}
		return provider;
	}

	public static void initialize(FontProvider p) {
		assert (provider == null);
		provider = p;
	}

	public static SWCRectangle getBoundingBox(String text) {
		return getProvider().getBoundingBox(text);
	}

	public static Font getFont() {
		return ((AWTFontProvider)getProvider()).getFont();
	}

	public interface FontProvider {
		//Font getFont();

		SWCRectangle getBoundingBox(String text);
	}

	public static class AWTFontProvider implements FontProvider {
		private BufferedImage dummy;

		public AWTFontProvider() {
			dummy = new BufferedImage(1000, 1000, BufferedImage.TYPE_3BYTE_BGR);
		}

		//@Override
		public Font getFont() {
			Font f = dummy.getGraphics().getFont();
			//need a large font to have enough precision
			return f.deriveFont((float) 100.0);
		}

		@Override
		public SWCRectangle getBoundingBox(String text) {
			Font font = getFont();
			FontRenderContext frc = ((Graphics2D) dummy.getGraphics()).getFontRenderContext();

			GlyphVector gv = font.layoutGlyphVector(frc, text.toCharArray(), 0, text.length(), Font.LAYOUT_LEFT_TO_RIGHT);
			Rectangle2D bb = gv.getPixelBounds(frc, 0, 0);

			return new SWCRectangle(bb.getX(), bb.getY(), bb.getWidth(), bb.getHeight());
		}
	}
}
