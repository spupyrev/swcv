package de.tinloaf.cloudy.algos.packing;

import de.tinloaf.cloudy.text.Word;
import de.tinloaf.cloudy.utils.SWCRectangle;

import java.util.Set;

public class TranslatedWordPlacer implements WordPlacer {

	private double translateX;
	private double translateY;
	private WordPlacer original;
	
	public TranslatedWordPlacer(double x, double y, WordPlacer original) {
		this.translateX = x;
		this.translateY = y;
		this.original = original;
	}
	
	public void setTranslation(double x, double y) {
		this.translateX = x;
		this.translateY = y;
	}
	
	public double getTranslationX() {
		return this.translateX;
	}
	
	public double getTranslationY() {
		return this.translateY;
	}
	
	@Override
	public SWCRectangle getRectangleForWord(Word w) {
		SWCRectangle centeredRet = new SWCRectangle(original.getRectangleForWord(w));
		centeredRet.move(translateX, translateY);
		
		return centeredRet;
	}
	
	public SWCRectangle getBoundingBox() {
		SWCRectangle bbox = new SWCRectangle();
		
		for (Word w: this.getWords()) {
			bbox.add(getRectangleForWord(w));
		}
		
		return bbox;
	}
	
	@Override
	public boolean contains(Word w) {
		return this.original.contains(w);
	}

	@Override
	public Set<Word> getWords() {
		return this.original.getWords();
	}

}
