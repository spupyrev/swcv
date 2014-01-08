package de.tinloaf.cloudy.algos.packing;

import de.tinloaf.cloudy.text.Word;
import de.tinloaf.cloudy.utils.SWCRectangle;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CombinationPlacer implements WordPlacer {

	private List<? extends WordPlacer> placers;
	private Map<Word, WordPlacer> placerMap;
	
	public CombinationPlacer(List<? extends WordPlacer> placers) {
		this.placers = placers;
		this.placerMap = new HashMap<Word, WordPlacer>();
		
		for (WordPlacer wp: placers) {
			for (Word w: wp.getWords()) {
				this.placerMap.put(w, wp);
			}
		}
	}
	
	@Override
	public SWCRectangle getRectangleForWord(Word w) {
		if (!this.contains(w)) {
			return null;
		}
		
		return this.placerMap.get(w).getRectangleForWord(w);
	}

	@Override
	public boolean contains(Word w) {
		return this.placerMap.containsKey(w);
	}

	@Override
	public Set<Word> getWords() {
		Set<Word> ret = new HashSet<Word>();
		
		for (WordPlacer wp: this.placers) {
			ret.addAll(wp.getWords());
		}
		
		return ret;
	}

}
