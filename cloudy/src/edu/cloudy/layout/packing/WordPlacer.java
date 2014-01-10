package edu.cloudy.layout.packing;

import edu.cloudy.nlp.Word;
import edu.cloudy.utils.SWCRectangle;

import java.util.Set;

public interface WordPlacer {
	public SWCRectangle getRectangleForWord(Word w);

	public boolean contains(Word w);
	
	//public SWCRectangle getBoundingBox();
	public Set<Word> getWords();
}
