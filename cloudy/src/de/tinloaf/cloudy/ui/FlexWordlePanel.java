package de.tinloaf.cloudy.ui;

import de.tinloaf.cloudy.similarity.TFRankingAlgo;
import de.tinloaf.cloudy.text.WCVDocument;
import de.tinloaf.cloudy.text.Word;
import de.tinloaf.cloudy.utils.FontUtils;
import de.tinloaf.cloudy.utils.ImageOutline;
import de.tinloaf.cloudy.utils.WikipediaXMLReader;
import de.tinloaf.cloudy.utils.colors.IColorScheme;
import de.tinloaf.cloudy.utils.colors.RandomColorScheme;

import javax.swing.JPanel;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * @author spupyrev
 * Nov 5, 2013
 */
public class FlexWordlePanel extends JPanel implements ActionListener {
	private static final long serialVersionUID = -1567284522572785736L;

	private static final int angleType = 0;

	private Random rnd = new Random(123);

	private IColorScheme wordColors;

	private boolean initialized = false;
	private List<Area> occupiedAreas;

	private BufferedImage canvas;

	public FlexWordlePanel() {
		this.setBackground(Color.WHITE);
	}

	@Override
	public void paint(Graphics g) {
		super.paint(g);

		if (!initialized) {
			initialized = true;

			canvas = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);
			Graphics2D g2 = canvas.createGraphics();
			draw(g2, getWidth(), getHeight());
		}

		Graphics2D gg = (Graphics2D) g;
		gg.drawImage(canvas, null, null);
	}

	public void draw(Graphics2D g2, double screenWidth, double screenHeight) {
		wordColors = new RandomColorScheme();
		occupiedAreas = new ArrayList<Area>();

		long startTime = System.currentTimeMillis();
		paintWords(g2, screenWidth, screenHeight);
		System.out.printf("flex layout done in %.3f sec\n", (System.currentTimeMillis() - startTime) / 1000.0);
	}

	private void paintWords(Graphics2D g2, double screenWidth, double screenHeight) {
		//Area boundingShape = ImageOutline.getShape("resources/shapes/batman.gif", screenWidth, screenHeight);
		//Area boundingShape = ImageOutline.getShape("resources/shapes/sherlock.png", screenWidth, screenHeight);
		Area boundingShape = ImageOutline.getShape("resources/shapes/cloud.png", screenWidth, screenHeight);
		Area screenArea = new Area(new Rectangle.Double(0, 0, screenWidth, screenHeight));
		screenArea.subtract(boundingShape);
		occupiedAreas.add(screenArea);

		//g2.setColor(Color.BLACK);
		//g2.draw(boundingShape);

		List<String> words = getWords();

		Map<String, Double> scales = new HashMap();
		for (int i = 0; i < words.size(); i++) {
			scales.put(words.get(i), computeOriginalScale(words.get(i), screenWidth, screenHeight, g2));
		}

		int cntPlaced = 0;
		int totalWords = 500;
		for (int i = 0; i < totalWords; i++) {
			String word = words.get(rnd.nextInt(words.size()));

			for (int r = 0; r < 50; r++) {
				double scale = scales.get(word);

				if (placeWord(word, scale, screenWidth, screenHeight, g2)) {
					cntPlaced++;
					if (cntPlaced % 50 == 0)
						System.out.println("placed " + cntPlaced + " words");
					break;
				} else {
					scales.put(word, scale * 0.9);
				}
			}
		}

		System.out.println("placed " + cntPlaced + " out of " + totalWords);
	}

	private boolean placeWord(String word, double scale, double screenWidth, double screenHeight, Graphics2D g2) {
		for (int r = 0; r < 1; r++) {
			double angle = generateAngle();
			GlyphVector gv = getGlyph(word, scale, angle, g2);
			for (int attempt = 0; attempt < 50; attempt++) {
				if (canPlaceWord(word, gv, screenWidth, screenHeight, g2)) {
					return true;
				}
			}
		}

		return false;
	}

	private double computeOriginalScale(String word, double screenWidth, double screenHeight, Graphics2D g2) {
		Font font = FontUtils.getFont();
		FontRenderContext frc = g2.getFontRenderContext();

		//get a new position for the text
		GlyphVector gv = font.layoutGlyphVector(frc, word.toCharArray(), 0, word.length(), Font.LAYOUT_LEFT_TO_RIGHT);
		Rectangle2D bb = gv.getPixelBounds(frc, 0, 0);

		double scaleX = screenWidth / bb.getWidth();
		double scaleY = screenHeight / bb.getHeight();

		double scale = Math.min(scaleX, scaleY);
		return scale * 0.75;
	}

	private GlyphVector getGlyph(String word, double scale, double angle, Graphics2D g2) {
		Font font = FontUtils.getFont();
		FontRenderContext frc = g2.getFontRenderContext();

		AffineTransform at = new AffineTransform();
		at.scale(scale, scale);
		at.rotate(angle);
		Font deriveFont = font.deriveFont(at);

		return deriveFont.layoutGlyphVector(frc, word.toCharArray(), 0, word.length(), Font.LAYOUT_LEFT_TO_RIGHT);
	}

	private boolean canPlaceWord(String word, GlyphVector gv, double screenWidth, double screenHeight, Graphics2D g2) {
		FontRenderContext frc = g2.getFontRenderContext();
		Rectangle2D bb = gv.getPixelBounds(frc, 0, 0);

		double x = rnd.nextDouble() * screenWidth;
		double y = rnd.nextDouble() * screenHeight;

		boolean fit = (0 <= x && x <= screenWidth - bb.getWidth() && bb.getHeight() <= y && y <= screenHeight - bb.getHeight());
		if (!fit)
			return false;

		Shape outline1 = gv.getOutline((float) x, (float) y);
		Area newArea = new Area(outline1);

		for (Area ua : occupiedAreas) {
			if (!ua.getBounds().intersects(newArea.getBounds()))
				continue;

			Area ar2 = new Area(ua);
			ar2.intersect(newArea);
			if (!ar2.isEmpty())
				return false;
		}

		g2.setColor(getWordColor(word, gv, x, y));
		g2.drawGlyphVector(gv, (float) x, (float) y);
		occupiedAreas.add(newArea);

		return true;
	}

	private Color getWordColor(String word, GlyphVector gv, double x, double y) {
		return wordColors.getColor(null);
		//if (word.equals("love"))
		//	return Color.RED;
		//return Color.BLACK;
	}

	private List<String> getWords() {
		WikipediaXMLReader xmlReader = new WikipediaXMLReader("data/wordcloud");
		xmlReader.read();
		Iterator<String> texts = xmlReader.getTexts();

		WCVDocument doc = new WCVDocument(texts.next());
		doc.parse();
		doc.weightFilter(25, new TFRankingAlgo());

		List<String> words = new ArrayList<String>();
		for (Word word : doc.getWords()) {
			words.add(word.word);
		}

		//words.add("Sherlock");
		//words.add("Holmes");
		return words;
	}

	private double generateAngle() {
		switch (angleType) {
		//horizontal
		case 0:
			return 0;
			//anyway
		case 1:
			return (rnd.nextDouble() * Math.PI - Math.PI / 2);
			//mostly horizontal
		case 2:
			if (Math.random() < 0.75)
				return 0;
			else
				return -Math.PI / 2;
			//half-half
		case 3:
			if (Math.random() < 0.5)
				return 0;
			else
				return -Math.PI / 2;
			//mostly vertical
		case 4:
			if (Math.random() < 0.25)
				return 0;
			else
				return -Math.PI / 2;
			//vertical
		case 5:
			return -Math.PI / 2;
		}
		return 0;
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		this.repaint();
		this.revalidate();
	}

}
