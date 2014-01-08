package de.tinloaf.cloudy.algos.overlaps;

import de.tinloaf.cloudy.text.Word;
import de.tinloaf.cloudy.utils.SWCRectangle;

import java.util.List;
import java.util.Map;

/**
 * @author spupyrev
 * May 12, 2013
 */
public interface OverlapRemoval<T extends SWCRectangle> {
	public void run(List<Word> words, Map<Word, T> wordPositions);
}
