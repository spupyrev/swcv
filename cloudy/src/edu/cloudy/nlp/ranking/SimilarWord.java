package edu.cloudy.nlp.ranking;

import java.util.Map;

import edu.cloudy.nlp.Word;
import edu.cloudy.nlp.ItemPair;

public class SimilarWord extends Word implements Similar<SimilarWord> {

	private Word original;
	private Map<ItemPair<Word>, Double> similarityMap;
	
	public SimilarWord(Word word, Map<ItemPair<Word>, Double> similarity) {
		super(word.word, word.weight);
		
		this.similarityMap = similarity;
		this.original = word;
	}

	@Override
	public double similarity(SimilarWord other) {
		ItemPair<Word> up = new ItemPair<Word>(original, other.original);
		assert(similarityMap.containsKey(up));

		return similarityMap.get(up);
	}

}
