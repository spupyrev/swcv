package de.tinloaf.cloudy.algos.packing;

import de.tinloaf.cloudy.text.Word;
import de.tinloaf.cloudy.utils.SWCRectangle;

import java.util.Set;

public interface WordPlacer {
	public SWCRectangle getRectangleForWord(Word w);

	public boolean contains(Word w);
	
	//public SWCRectangle getBoundingBox();
	public Set<Word> getWords();
}
