package edu.cloudy.utils;

import javax.imageio.ImageIO;

import java.awt.Color;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * @author spupyrev
 * Nov 7, 2013
 */
public class ImageOutline {
	public static Area getOutline(BufferedImage image, Color color, boolean include, int tolerance) {
		Area area = new Area();
		for (int x = 0; x < image.getWidth(); x++) {
			for (int y = 0; y < image.getHeight(); y++) {
				Color pixel = new Color(image.getRGB(x, y));
				if (include) {
					if (isIncluded(color, pixel, tolerance)) {
						Rectangle r = new Rectangle(x, y, 1, 1);
						area.add(new Area(r));
					}
				} else {
					if (!isIncluded(color, pixel, tolerance)) {
						Rectangle r = new Rectangle(x, y, 1, 1);
						area.add(new Area(r));
					}
				}
			}
		}
		return area;
	}

	public static boolean isIncluded(Color target, Color pixel, int tolerance) {
		int rT = target.getRed();
		int gT = target.getGreen();
		int bT = target.getBlue();
		int rP = pixel.getRed();
		int gP = pixel.getGreen();
		int bP = pixel.getBlue();
		return ((rP - tolerance <= rT) && (rT <= rP + tolerance) && (gP - tolerance <= gT) && (gT <= gP + tolerance) && (bP - tolerance <= bT) && (bT <= bP
				+ tolerance));
	}

	public static Area getShape(String shapeFile, double screenWidth, double screenHeight) {
		try {
			System.out.print("reading shape...");
			BufferedImage outline = ImageIO.read(new File(shapeFile));
			Area boundingShape = ImageOutline.getOutline(outline, Color.BLACK, true, 10);
			System.out.println("done");

			Rectangle2D bb = boundingShape.getBounds2D();

			double scaleX = screenWidth / bb.getWidth();
			double scaleY = screenHeight / bb.getHeight();

			double scale = Math.min(scaleX, scaleY) * 0.95;

			AffineTransform at = new AffineTransform(scale, 0, 0, scale, 0, 0);
			boundingShape.transform(at);
			return boundingShape;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

}
