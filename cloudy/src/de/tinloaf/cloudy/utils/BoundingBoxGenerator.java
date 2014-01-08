package de.tinloaf.cloudy.utils;

import de.tinloaf.cloudy.text.Word;

public class BoundingBoxGenerator {
	private double weightToAreaFactor;

	public BoundingBoxGenerator(double weightToAreaFactor) {
		this.weightToAreaFactor = weightToAreaFactor;
	}

	public SWCRectangle getBoundingBox(Word w, double area) {
		SWCRectangle bb = FontUtils.getBoundingBox(w.word);

		double origWidth = bb.getWidth();
		double origHeight = bb.getHeight();

		double scaling = Math.sqrt(area * weightToAreaFactor / (origWidth * origHeight));
		return new SWCRectangle(0, 0, origWidth * scaling, origHeight * scaling);
	}

	public double getWeightToAreaFactor() {
		return weightToAreaFactor;
	}

}
