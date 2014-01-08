package de.tinloaf.cloudy.text;

import java.util.Map;

import de.tinloaf.cloudy.similarity.Similar;
import de.tinloaf.cloudy.utils.WordPair;

public class SimilarWord extends Word implements Similar<SimilarWord> {

	private Word original;
	private Map<WordPair, Double> similarityMap;
	
	public SimilarWord(Word word, Map<WordPair, Double> similarity) {
		super(word.word, word.weight);
		
		this.similarityMap = similarity;
		this.original = word;
	}

	@Override
	public double similarity(SimilarWord other) {
		WordPair up = new WordPair(original, other.original);
		assert(similarityMap.containsKey(up));

		return similarityMap.get(up);
	}

}
