package edu.cloudy.utils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.cloudy.nlp.Word;

public class Cluster {
	public Map<Word, SWCRectangle> wordPositions = new HashMap<Word, SWCRectangle>();
	public SWCPoint center;

	public SWCRectangle getBoundingBox() {
		SWCRectangle bb = new SWCRectangle();
		for (Word w : wordPositions.keySet()) {
			SWCRectangle r = actualWordPosition(w);
			bb.add(r);
		}

		return bb;
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();
		for (Word w : wordPositions.keySet())
			sb.append(" " + w.word);
		return sb.toString();
	}

	public boolean overlap(List<Cluster> list) {
		for (Cluster c : list) {
			if (c.equals(this))
				continue;

			if (overlap(c))
				return true;
		}

		return false;
	}

	public boolean overlap(Cluster other) {
		for (Word w1 : wordPositions.keySet())
			for (Word w2 : other.wordPositions.keySet()) {
				SWCRectangle rect1 = actualWordPosition(w1);
				SWCRectangle rect2 = other.actualWordPosition(w2);

				if (overlap(rect1, rect2))
					return true;
			}

		return false;
	}

	public SWCRectangle actualWordPosition(Word word) {
		SWCRectangle r1 = wordPositions.get(word);
		return new SWCRectangle(r1.getX() + center.x(), r1.getY() + center.y(), r1.getWidth(), r1.getHeight());
	}

	public boolean overlap(SWCRectangle rect1, SWCRectangle rect2) {
		if (rect1.intersects(rect2)) {
			double hix = Math.min(rect1.getMaxX(), rect2.getMaxX());
			double lox = Math.max(rect1.getMinX(), rect2.getMinX());
			double hiy = Math.min(rect1.getMaxY(), rect2.getMaxY());
			double loy = Math.max(rect1.getMinY(), rect2.getMinY());
			double dx = hix - lox; // hi > lo
			double dy = hiy - loy;
			if (Math.min(dx, dy) > 1)
				return true;
		}

		return false;
	}
}