package de.tinloaf.cloudy.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Observable;

import de.tinloaf.cloudy.algos.LayoutAlgo;
import de.tinloaf.cloudy.algos.StarForestAlgoNew;
import de.tinloaf.cloudy.algos.packing.ClusterSpiralPlacer;
import de.tinloaf.cloudy.text.Word;

/**
 * 
 * This class takes stars (Clusters) and expands them within the cloud to create
 * a more uniform and visually appealing graph.
 * 
 * @author Dylan Clavell
 *
 */
public class StarExpander extends Observable {

	private final long pauseTime = 0;
	private boolean animated;
	private List<Cluster> clusters;
	private Map<Word, SWCRectangle> wordPositions;
	private List<Word> words;
	private double cloudXMin;
	private double cloudXMax;
	private double cloudYMin;
	private double cloudYMax;

	public StarExpander(List<Cluster> clusters, Map<Word, SWCRectangle> wordPositions, List<Word> words, boolean animated) {
		this.animated = true;
		this.clusters = clusters;
		this.wordPositions = wordPositions;
		this.words = words;
		setBoundingBox();
	}

	/**
	 * Expands every word in some way away from the center of its star without
	 * encountering collisions (if possible).
	 */
	public void expandStars() {
		for (Cluster c : clusters) {
			for (Word w : c.wordPositions.keySet()) {
				centerInEmptySpace(clusters, c, w, wordPositions);
				if (animated) {
					try {
						this.setChanged();
						this.notifyObservers(this.words);
						Thread.sleep(pauseTime);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}

	private void setBoundingBox() {
		cloudXMin = Double.POSITIVE_INFINITY;
		cloudXMax = Double.NEGATIVE_INFINITY;
		cloudYMin = Double.POSITIVE_INFINITY;
		cloudYMax = Double.NEGATIVE_INFINITY;

		for(Entry<Word, SWCRectangle> wrd : wordPositions.entrySet()){
			if (wrd.getValue().getX() < cloudXMin) {
				cloudXMin = wrd.getValue().getX();
			}
			if (wrd.getValue().getY() < cloudYMin) {
				cloudYMin = wrd.getValue().getY();
			}
			if (wrd.getValue().getX() + wrd.getValue().getWidth() > cloudXMax) {
				cloudXMax = wrd.getValue().getX();
			}
			if (wrd.getValue().getY() + wrd.getValue().getHeight() > cloudYMax) {
				cloudYMax = wrd.getValue().getY();
			}
		}
	}

	/*
	 * Saved for posterity.
	 * 
	 * Moves the word some arbitrary amount outward from the cluster's center,
	 * then lets the ForceDirectedOverlapRemover finish the job.
	 * 
	 */
	private void naiveOverlapRemoverApproach(Cluster c, Word w, Map<Word, SWCRectangle> wordPositions) {
		SWCRectangle position = wordPositions.get(w);
		// Naive "let the force directed overlap removal handle it" approach:
		double xVel = -.3 * (c.center.x() - c.actualWordPosition(w).getCenterX());
		double yVel = -.5 * (c.center.y() - c.actualWordPosition(w).getCenterY());
		position.setCenter(position.getCenterX() + xVel, position.getCenterY() + yVel);
	}

	/*
	 * Saved for posterity.
	 * 
	 * Moves the word some arbitrary amount outward from the cluster's center,
	 * preventing collisions then lets the ForceDirectedOverlapRemover finish the job.
	 * 
	 */
	private void advancedOverlapRemoverApproach(Cluster c, Word w, Map<Word, SWCRectangle> wordPositions) {
		SWCRectangle position = wordPositions.get(w);
		// Do it until they overlap approach:
		double xMax = position.getCenterX(), yMax = position.getCenterY();
		double xPos = position.getCenterX(), yPos = position.getCenterY();

		for (double d = 0.01; d < 0.2; d = d + 0.01) {
			double potY = yPos - d * (c.center.y() - yPos);
			position.setCenter(xPos, potY);
			boolean conflicts = false;
			for (Word otherWord : wordPositions.keySet()) {
				if (otherWord != w && wordPositions.get(otherWord).intersects(position)) {
					conflicts = true;
				}
			}
			if (!conflicts) {
				yMax = potY;
			}
			position.setCenter(xPos, yPos);
		}
		position.setCenter(xPos, yMax);
		for (double d = 0.01; d < 0.4; d = d + 0.01) {
			double potX = xPos - d * (c.center.x() - xPos);
			position.setCenter(potX, yMax);
			boolean conflicts = false;
			for (Word otherWord : wordPositions.keySet()) {
				if (otherWord != w && wordPositions.get(otherWord).intersects(position)) {
					conflicts = true;
				}
			}
			if (!conflicts) {
				xMax = potX;
			}
			position.setCenter(xPos, yMax);
		}
		xPos = xMax;
		position.setCenter(xPos, yPos);
		for (double d = 0.01; d < 0.2; d = d + 0.01) {
			double potY = yPos - d * (c.center.y() - yPos);
			position.setCenter(xPos, potY);
			boolean conflicts = false;
			for (Word otherWord : wordPositions.keySet()) {
				if (otherWord != w && wordPositions.get(otherWord).intersects(position)) {
					conflicts = true;
				}
			}
			if (!conflicts) {
				yMax = potY;
			}
			position.setCenter(xPos, yPos);
		}
		yPos = yMax;
		position.setCenter(xPos, yPos);
	}

	/*
	 * The most successful/up-to-date algorithm.
	 * 
	 * Using the bounding box of the cloud, moves each word outward from the center
	 * of its star to fill in the gaps between it and other words (staying inside the
	 * cloud's bounding box, and avoiding collisions).
	 * 
	 */
	private void centerInEmptySpace(List<Cluster> clusters, Cluster c, Word w, Map<Word, SWCRectangle> wordPositions) {

		SWCRectangle position = wordPositions.get(w);

		// Center in empty space approach:
		if(position == null) return;
		double xPos = position.getCenterX(), yPos = position.getCenterY();

		// --- Now, move the clusters

		double d = expandY(w, xPos, yPos, c.center.y(), position);
		// If there were any valid moves, choose the midpoint
		if (d > 0) {

			double lastY = yPos - (d - 0.01) * (c.center.y() - yPos);
			double halfLength = (lastY - yPos) / 2;
			yPos = halfLength + yPos;
			position.setCenter(xPos, yPos);

			d = expandX(w, xPos, yPos, c.center.x(), position);

			// If there were any valid moves, choose the midpoint
			if (d > 0) {

				double lastX = xPos - (d - 0.01) * (c.center.x() - xPos);
				halfLength = (lastX - xPos) / 2;
				xPos = halfLength + xPos;
			}

			position.setCenter(xPos, yPos);

			// If there were no valid moves, try again, expanding X first, this time.
		} else {

			position.setCenter(xPos, yPos);
			d = expandX(w, xPos, yPos, c.center.x(), position);

			// If there were any valid moves, choose the midpoint
			if (d > 0) {
				double lastX = xPos - (d - 0.01) * (c.center.x() - xPos);
				double halfLength = (lastX - xPos) / 2;
				xPos = halfLength + xPos;
			}

			position.setCenter(xPos, yPos);
			d = expandY(w, xPos, yPos, c.center.y(), position);

			// If there were any valid moves, choose the midpoint
			if (d > 0) {
				double lastY = yPos - (d - 0.01) * (c.center.y() - yPos);
				double halfLength = (lastY - yPos) / 2;
				yPos = halfLength + yPos;
			}

			position.setCenter(xPos, yPos);
		}
	}

	/**
	 * 
	 * @param w
	 * @param xPos
	 * @param yPos
	 * @param cXCenter
	 * @param position
	 * @return The double value the iterations ended at; if negative, it means the word went
	 *		 out of bounds before it conflicted.
	 */
	private double expandX(Word w, double xPos, double yPos, double cXCenter, SWCRectangle position) {
		double d;
		double last = Double.POSITIVE_INFINITY;
		for (d = 0.01;; d = d + 0.01) {
			double potX = xPos - d * (cXCenter - xPos);
			if (potX < cloudXMin || potX > cloudXMax)
				return -d;
			if (d >= 5) {
				last = d;
			}
			position.setCenter(potX, yPos);
			for (Word otherWord : wordPositions.keySet()) {
				if (otherWord != w && wordPositions.get(otherWord).intersects(position)) {
					return Math.min(d, last);
				}
			}
			if (animated) {
				try {
					this.setChanged();
					this.notifyObservers(this.words);
					Thread.sleep(pauseTime);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			position.setCenter(xPos, yPos);
		}
	}

	/**
	 *
	 * @param w
	 * @param xPos
	 * @param yPos
	 * @param cYCenter
	 * @param position
	 * @return The double value the iterations ended at; if negative, it means the word went
	 *		 out of bounds before it conflicted.
	 */
	private double expandY(Word w, double xPos, double yPos, double cYCenter, SWCRectangle position) {
		double d;
		double last = Double.POSITIVE_INFINITY;
		for (d = 0.01;; d = d + 0.01) {
			double potY = yPos - d * (cYCenter - yPos);
			if (potY < cloudYMin || potY > cloudYMax) {
				return -d;
			}
			if (d >= 3) {
				last = d;
			}
			position.setCenter(xPos, potY);
			for (Word otherWord : wordPositions.keySet()) {
				if (otherWord != w && wordPositions.get(otherWord).intersects(position)) {
					return Math.min(d, last);
				}
			}
			if (animated) {
				try {
					this.setChanged();
					this.notifyObservers(this.words);
					Thread.sleep(pauseTime);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			position.setCenter(xPos, yPos);
		}
	}
}
