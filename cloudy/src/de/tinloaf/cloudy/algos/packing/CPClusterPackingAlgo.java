package de.tinloaf.cloudy.algos.packing;

import de.tinloaf.cloudy.text.Word;
import de.tinloaf.cloudy.utils.BoundingBoxGenerator;
import de.tinloaf.cloudy.utils.GeometryUtils;
import de.tinloaf.cloudy.utils.SWCPoint;
import de.tinloaf.cloudy.utils.SWCRectangle;
import de.tinloaf.cloudy.utils.WordPair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * May 12, 2013
 * 
 * TODO:
 * 1. merge with mds
 * 2. change forces
 */
public class CPClusterPackingAlgo {
	private static double KA = 500;
	private static double KR = 5000;
	private static double DELTA = 0.00001;
	private static double TOTAL_ITERATIONS = 10000;
	private static double FIELD_SIZE = 1;

	private List<TranslatedWordPlacer> lPlacers;
	private Map<WordPair, Double> similarity;
	
	private TranslatedWordPlacer[] placers;
	private Map<TranslatedWordPlacer, Integer> placerIndices;
	private double[][] placerDistances;
	
	private BoundingBoxGenerator bbGenerator;
	private double weightToAreaFactor;

	public List<? extends WordPlacer> getPlacers() {
		return this.lPlacers;
	}
	
	public void setData(List<WordPlacer> words, Map<WordPair, Double> similarity) {
		this.lPlacers = new LinkedList<TranslatedWordPlacer>();
		this.placers = new TranslatedWordPlacer[words.size()];
		int k = 0;
		this.placerIndices = new HashMap<TranslatedWordPlacer, Integer>();
		
		for (WordPlacer p: words) {
			TranslatedWordPlacer twp = new TranslatedWordPlacer(0, 0, p);
			this.lPlacers.add(twp);
			this.placerIndices.put(twp, k);
			this.placers[k++] = twp;
		}

		
		this.similarity = similarity;
		
		// compute desired distances of the placers
		double placerSimilarity[][] = new double[words.size()][words.size()];
		double maxSimilarity = 0.0;
		for (int i = 0; i < words.size(); i++) {
			for (int j = i+1; j < words.size(); j++) {
				placerSimilarity[i][j] = 0.0;
				
				for (Word w: this.placers[i].getWords()) {
					for (Word v: this.placers[j].getWords()) {
						WordPair wp = new WordPair(w, v);
						placerSimilarity[i][j] += this.similarity.get(wp);
					}
				}
				placerSimilarity[j][i] = placerSimilarity[i][j];
				maxSimilarity = Math.max(maxSimilarity, placerSimilarity[i][j]);
			}
		}
		
		// Normalize
		for (int i = 0; i < words.size(); i++) {
			for (int j = i+1; j < words.size(); j++) {
				placerSimilarity[j][i] /= maxSimilarity;				
				placerSimilarity[i][j] /= maxSimilarity;
			}
		}
		
		// Distances
		this.placerDistances = new double[words.size()][words.size()];
		for (int i = 0; i < words.size(); i++) {
			this.placerDistances[i][i] = 0.0;
			for (int j = i+1; j < words.size(); j++) {
				this.placerDistances[i][j] = this.placerDistances[j][i] = (1 - placerSimilarity[j][i]) * FIELD_SIZE;
			}
		}
	}

	public void run() {
		initialPlacement();

		SWCRectangle[] bboxes = new SWCRectangle[this.placers.length];
		SWCPoint[] points2D = new SWCPoint[this.placers.length];
		Map<TranslatedWordPlacer, SWCRectangle> placerToRect = new HashMap<TranslatedWordPlacer, SWCRectangle>();
		
		for (int i = 0; i < this.placers.length; i++) {
			// compute centers for placers' BoundingBoxes
			
			//BBox
			SWCRectangle bbox = new SWCRectangle();
			TranslatedWordPlacer twp = this.placers[i];
			for (Word w: twp.getWords()) {
				bbox.add(twp.getRectangleForWord(w));
			}
			
			bboxes[i] = bbox;
			placerToRect.put(twp, bbox);
		}
		
		List<List<Integer>> edges = new ArrayList<List<Integer>>();
		GeometryUtils.computeDelaunayTriangulation(this.lPlacers, placerToRect, points2D, edges);

		SWCPoint[] points = new SWCPoint[this.placers.length];
		for (int i = 0; i < this.placers.length; i++)
			points[i] = new SWCPoint(points2D[i].x(), points2D[i].y());

		// use force-directed to get rid of overlaps
		// f(a,b) = kr * min{dx, dy}
		removeOverlaps(edges, points);
	}

	private boolean checkIntersect(TranslatedWordPlacer wp1, TranslatedWordPlacer wp2) {
		for (Word w: wp1.getWords()) {
			for (Word v: wp2.getWords()) {
				if (wp1.getRectangleForWord(w).intersects(wp2.getRectangleForWord(v))) {
					return true;
				}
			}
		}
		
		return false;
	}
	
	//TODO: merge with ForceDirectedOverlapRemoval ?
	private void removeOverlaps(List<List<Integer>> edges, SWCPoint[] points) {
		double[] displacementx = new double[this.placers.length];
		double[] displacementy = new double[this.placers.length];
		int numOverlaps = 0;
		int numIterations = 0;

		do {
			// compute the displacement for all words in this time step
			for (int i = 0; i < this.placers.length; i++) {
				for (int j = i + 1; j < this.placers.length; j++) {
					// compute the displacement due to the overlap repulsive force
					TranslatedWordPlacer wp1 = this.placers[i];					
					TranslatedWordPlacer wp2 = this.placers[j];
					SWCRectangle bbox1 = wp1.getBoundingBox();
					SWCRectangle bbox2 = wp2.getBoundingBox();

					if (this.checkIntersect(wp1, wp2)) {
						double maxForce = 0.0;
						double deltax = 0.0;
						double deltay = 0.0;
						
						for (Word w: wp1.getWords()) {
							for (Word v: wp2.getWords()) {
								SWCRectangle w1Rect = wp1.getRectangleForWord(w);
								SWCRectangle w2Rect = wp2.getRectangleForWord(v);
								
								if (! w1Rect.intersects(w2Rect))
									continue;
								
								double hix = Math.min(w1Rect.getMaxX(), w2Rect.getMaxX());
								double lox = Math.max(w1Rect.getMinX(), w2Rect.getMinX());
								double hiy = Math.min(w1Rect.getMaxY(), w2Rect.getMaxY());
								double loy = Math.max(w1Rect.getMinY(), w2Rect.getMinY());
								double dx = hix - lox; // hi > lo
								double dy = hiy - loy;
								double force = KR * Math.min(dx, dy);
								
								if (force > maxForce) {
									maxForce = force;
									deltax = w2Rect.getCenterX() - w2Rect.getCenterX();
									deltay = w2Rect.getCenterY() - w1Rect.getCenterY();
								}
							}
						}
						
						
						//if (force < .000001)
						//	continue;


						if (deltax == 0) {
							deltax = Math.random() / 1000 + 0.000001;
						}

						if (deltay == 0) {
							deltay = Math.random() / 1000 + 0.000001;
						}

						assert (deltax != 0);
						assert (deltay != 0);

						double hlength = Math.sqrt(deltax * deltax + deltay * deltay);

						if (hlength == 0) {
							hlength = .00001;
						}

						assert (hlength != 0);

						displacementx[j] += maxForce * DELTA * deltax / hlength;
						displacementy[j] += maxForce * DELTA * deltay / hlength;

						displacementx[i] += -1 * maxForce * DELTA * deltax / hlength;
						displacementy[i] += -1 * maxForce * DELTA * deltay / hlength;
					}
					// compute the displacement due to the attractive spring force
					else if (edges.get(i).contains(j)) {

						// compute delta l (the distance between rectangle
						// boundaries
						double delta_l = rectangleSeparation(wp1, wp2);

						// TODO constant?
						//double force = (words[i].weight / maxWeight) * (words[j].weight / maxWeight) * delta_l;
						double force = delta_l;
						
						if (force < .0000001)
							continue;

						double deltax = bbox1.getCenterX() - bbox2.getCenterX();
						double deltay = bbox1.getCenterY() - bbox2.getCenterY();
						double hlength = Math.hypot(deltax, deltay);
						assert (hlength != 0);

						displacementx[j] += -force * DELTA * deltax / hlength;
						displacementy[j] += -force * DELTA * deltay / hlength;
						displacementx[i] += force * DELTA * deltax / hlength;
						displacementy[i] += force * DELTA * deltay / hlength;
						// System.out.println(displacementx[other] + " " +
						// displacementy[other]);
					}
				}
			}
			/*
			// compute edge word attractions
			for (int i = 0; i < this.placers.length; i++) {
				// All adjacent vertices of i
				for (int j : edges.get(i)) {
					// Get common neighbors
					List<Integer> commonNeighbors = new ArrayList<Integer>(edges.get(j));
					commonNeighbors.retainAll(edges.get(i));

					// k will be all common neighbors
					for (int k : commonNeighbors) {
						// i-j-k form a triangle...
						// Everything that could be in here has to be either a neighbor of
						// i or of j...
						List<Integer> joinedNeighbors = new ArrayList<Integer>(edges.get(j));
						joinedNeighbors.addAll(edges.get(i));

						for (int a : joinedNeighbors) {
							if ((a == i) || (a == j) || (a == k)) {
								continue;
							}

							if (points[a].inside(points[i], points[j], points[k])) {
								if (edges.get(a).contains(i) && edges.get(a).contains(j)) {
									// Compute distance from point to line
									double dist = GeometryUtils.pointToLineDistance(points[i], points[j], points[a]);

									// compute (signless) slope of normal (beware of the sign!)
									double deltaX = points[i].x() - points[j].x();
									System.out.println("DeltaX: " + deltaX);
									double deltaY = points[i].y() - points[j].y();
									if (deltaX == 0) {
										deltaX = 0.0001;
									}
									double slope = deltaY / deltaX;
									if (slope == 0) {
										slope = 0.0001;
									}
									double normalSlope = Math.abs(1 / slope);

									// figure out whether displacement vector should point to positive y
									int signY = 0;
									if (points[a].y() < (points[i].y() + (points[a].x() - points[i].x()) * slope)) {
										// we're below the line, point upwards
										signY = 1;
									} else {
										signY = -1;
									}

									int signX = 0;
									if (points[a].x() < (points[i].x() + (points[a].y() - points[i].y()) * (1 / slope))) {
										signX = 1;
									} else {
										signX = -1;
									}

									// calculate x- and y-fractions of the distance along the normal slope
									double fracX = Math.cos(Math.atan(normalSlope));
									double fracY = Math.sin(Math.atan(normalSlope));

									System.out.println("Repulsive force: " + (signX * KA * DELTA * fracX * dist));
									
									displacementx[a] += signX * KA * DELTA * fracX * dist;
									displacementy[a] += signY * KA * DELTA * fracY * dist;
									assert (Math.abs(displacementy[i]) < 100);
									assert (!Double.isNaN(displacementx[a]));
									assert (!Double.isNaN(displacementy[a]));
								} else if (edges.get(a).contains(i) && edges.get(a).contains(k)) {
									// Compute distance from point to line
									double dist = GeometryUtils.pointToLineDistance(points[i], points[k], points[a]);

									// compute (signless) slope of normal (beware of the sign!)
									double deltaX = points[i].x() - points[k].x();
									double deltaY = points[i].y() - points[k].y();
									if (deltaX == 0) {
										deltaX = 0.0001;
									}
									double slope = deltaY / deltaX;
									if (slope == 0) {
										slope = 0.0000001;
									}
									double normalSlope = Math.abs(1 / slope);

									// figure out whether displacement vector should point to positive y
									int signY = 0;
									if (points[a].y() < (points[i].y() + (points[a].x() - points[i].x()) * slope)) {
										// we're below the line, point upwards
										signY = 1;
									} else {
										signY = -1;
									}

									int signX = 0;
									if (points[a].x() < (points[i].x() + (points[a].y() - points[i].y()) * (1 / slope))) {
										signX = 1;
									} else {
										signX = -1;
									}

									// calculate x- and y-fractions of the distance along the normal slope
									double fracX = Math.cos(Math.atan(normalSlope));
									double fracY = Math.sin(Math.atan(normalSlope));

									System.out.println("Attractive force: " + (signX * KA * DELTA * fracX * dist));
									
									displacementx[a] += signX * KA * DELTA * fracX * dist;
									displacementy[a] += signY * KA * DELTA * fracY * dist;
									assert (Math.abs(displacementy[i]) < 10);
									assert (!Double.isNaN(displacementx[a]));
									assert (!Double.isNaN(displacementy[a]));
								} else {
									// Compute distance from point to line
									double dist = GeometryUtils.pointToLineDistance(points[j], points[k], points[a]);

									// compute (signless) slope of normal (beware of the sign!)
									double deltaX = points[j].x() - points[k].x();
									double deltaY = points[j].y() - points[k].y();
									if (deltaX == 0) {
										deltaX = 0.0001;
									}
									double slope = deltaY / deltaX;
									if (slope == 0) {
										slope = 0.0000001;
									}
									double normalSlope = Math.abs(1 / slope);

									// figure out whether displacement vector should point to positive y
									int signY = 0;
									if (points[a].y() < (points[j].y() + (points[a].x() - points[j].x()) * slope)) {
										// we're below the line, point upwards
										signY = 1;
									} else {
										signY = -1;
									}

									int signX = 0;
									if (points[a].x() < (points[j].x() + (points[a].y() - points[j].y()) * (1 / slope))) {
										signX = 1;
									} else {
										signX = -1;
									}

									// calculate x- and y-fractions of the distance along the normal slope
									double fracX = Math.cos(Math.atan(normalSlope));
									double fracY = Math.sin(Math.atan(normalSlope));

									assert (!Double.isNaN(dist));
									assert (!Double.isNaN(fracX));
									assert (!Double.isNaN(fracY));
									assert (!Double.isNaN(DELTA));

									System.out.println("Flip force: " + (signX * KA * DELTA * fracX * dist));
									
									displacementx[a] += signX * KA * DELTA * fracX * dist;
									displacementy[a] += signY * KA * DELTA * fracY * dist;
									assert (Math.abs(displacementy[i]) < 10);
								}
								assert (!Double.isNaN(displacementx[a]));
								assert (!Double.isNaN(displacementy[a]));
							}
						}
					}
				}
			}*/

			// move all the words
			for (int i = 0; i < this.placers.length; i++) {
				TranslatedWordPlacer twp = this.placers[i];
				
				double newX = twp.getTranslationX() + displacementx[i];
				double newY = twp.getTranslationY() + displacementy[i];
				
				//assert (Math.abs(displacementy[i]) < 10);
				
				assert(newX < 10000);
				
				assert(newX != Double.NaN);				
				assert(newY != Double.NaN);
				
				twp.setTranslation(newX, newY);
				
				displacementx[i] = 0.;
				displacementy[i] = 0.;
			}

			numOverlaps = countOverlaps();
			if (numIterations++ > 3 * TOTAL_ITERATIONS)
				break;
			
			System.out.println(numOverlaps);

			if (numIterations % 1000 == 0)
				System.out.println("Finished Iteration " + numIterations);
		} while (numOverlaps > 0 || numIterations < TOTAL_ITERATIONS);
	}

	private int countOverlaps() {
		int overlaps = 0;
		for (int i = 0; i < this.placers.length; i++) {
			for (int j = i + 1; j < this.placers.length; j++) {
				TranslatedWordPlacer twp1 = this.placers[i];
				TranslatedWordPlacer twp2 = this.placers[j];
				
				if (this.checkIntersect(twp1, twp2))
					overlaps++;
			}
		}
		return overlaps;
	}

	/*
	private void initialPlacement() {
		//find initial placement by mds layout
		
		double[][] outputMDS = MDSJ.stressMinimization(this.placerDistances, 2);
		
		for (int i = 0; i < this.placers.length; i++) {
			TranslatedWordPlacer twp = this.placers[i];
			twp.setTranslation(outputMDS[0][i], outputMDS[1][i]);
		}
	}
	*/

	private void initialPlacement() {
		final int MAX_X = 500;
		final int MAX_Y = 500;
		
		for (int i = 0; i < this.placers.length; i++) {
			TranslatedWordPlacer twp = this.placers[i];
			
			double x = Math.random() * MAX_X;
			double y = Math.random() * MAX_Y;
			
			twp.setTranslation(x, y);
		}
		
	}
	private double rectangleSeparation(TranslatedWordPlacer twp1, TranslatedWordPlacer twp2) {
		// Computes separation distance between rectangle boundaries

		double minDist = Double.MAX_VALUE;

		for (Word w: twp1.getWords()) {
			for (Word v: twp2.getWords()) {
				SWCRectangle rect1 = twp1.getRectangleForWord(w);
				SWCRectangle rect2 = twp2.getRectangleForWord(v);
				
				// rectangle centers
				double x1 = rect1.getCenterX();
				double y1 = rect1.getCenterY();
				double x2 = rect2.getCenterX();
				double y2 = rect2.getCenterY();

				assert (!Double.isNaN(x1));
				assert (!Double.isNaN(x2));
				assert (!Double.isNaN(y1));
				assert (!Double.isNaN(y2));

				// rectangle widths and heights
				double w1 = rect1.getWidth();
				double h1 = rect1.getHeight();
				double w2 = rect2.getWidth();
				double h2 = rect2.getHeight();

				/*
				 * if (x2 == x1) { x2 *= 1.001; // perturb a little bit }
				 */

				double dx = x2 - x1;
				double dy = y2 - y1;
				double centerDist = Math.hypot(dx, dy);
				if (dx == 0) {
					dx = 0.00001;
				}

				assert (dx != 0);
				double centerSlope = (dy / dx);

				// rectangle slopes
				assert (w1 > 0);
				assert (w2 > 0);
				double s1 = h1 / w1;
				double s2 = h2 / w2;
				boolean sideIntercept1 = Math.abs(centerSlope) <= s1;
				boolean sideIntercept2 = Math.abs(centerSlope) <= s2;

				double d1; // part of line segment inside rectangle 1
				if (sideIntercept1) {
					d1 = (w1 / 2.0) * centerDist / Math.abs(dx);
				} else {
					d1 = (h1 / 2.0) * centerDist / Math.abs(dy);
				}

				double d2; // part of line segment inside rectangle 2
				if (sideIntercept2) {
					d2 = (w2 / 2.0) * centerDist / Math.abs(dx);
				} else {
					d2 = (h2 / 2.0) * centerDist / Math.abs(dy);
				}

				double endDist = centerDist - d1 - d2;
				minDist = Math.min(endDist, minDist);
			}
		}
		return minDist;
	}

}