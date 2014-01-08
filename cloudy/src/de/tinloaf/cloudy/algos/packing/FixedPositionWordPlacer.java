package de.tinloaf.cloudy.algos.packing;

import de.tinloaf.cloudy.text.Word;
import de.tinloaf.cloudy.utils.SWCRectangle;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class FixedPositionWordPlacer implements WordPlacer {

	//smth wrong with generics here...
	private Map wordToRect;

	public FixedPositionWordPlacer(Map<Word, ? extends SWCRectangle> wordToRect) {
		this.wordToRect = wordToRect;
	}

	public FixedPositionWordPlacer(Word word, SWCRectangle rect) {
		this.wordToRect = new HashMap<Word, SWCRectangle>();
		wordToRect.put(word, rect);
	}

	@Override
	public SWCRectangle getRectangleForWord(Word w) {
		return (SWCRectangle) this.wordToRect.get(w);
	}

	public SWCRectangle getBoundingBox() {
		double maxX, minX, maxY, minY;
		SWCRectangle firstRect = (SWCRectangle) wordToRect.values().iterator().next();
		maxX = firstRect.getMaxX();
		minX = firstRect.getMinX();
		maxY = firstRect.getMaxY();
		minY = firstRect.getMinY();

		for (Object o : wordToRect.values()) {
			SWCRectangle rect = (SWCRectangle) o;
			maxX = Math.max(maxX, rect.getMaxX());
			maxY = Math.max(maxY, rect.getMaxY());
			minX = Math.min(minX, rect.getMinX());
			minY = Math.min(minY, rect.getMinY());
		}

		return new SWCRectangle(0, 0, (maxX - minX), (maxY - minY));
	}

	@Override
	public boolean contains(Word w) {
		return this.wordToRect.containsKey(w);
	}

	@Override
	public Set<Word> getWords() {
		return this.wordToRect.keySet();
	}

}
