package de.tinloaf.cloudy.text;

import de.tinloaf.cloudy.similarity.RankingAlgo;
import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;
import opennlp.tools.tokenize.Tokenizer;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;

import java.awt.Point;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.lemurproject.kstem.KrovetzStemmer;

public class WCVDocument {
	private String text;
	private List<Word> words;

	public WCVDocument(String text) {
		this.text = text;
	}

	public List<Word> getWords() {
		return words;
	}

	public String getText() {
		return text;
	}

	/**
	 * Parse document:
	 * 1. wordify (extract sentences and words)
	 * 2. stem
	 * 3. remove stopwords
	 */
	public void parse() {
		Tokenizer tokenizer = buildTokenizer();
		String[] sentences = buildSentences();
		Set<String> stopwords = buildStopwords();
		//LovinsStemmer stemmer = new LovinsStemmer();
		Stemmer stemmer = new PorterStemmer();
		//stem => word
		Map<String, Word> wordMap = new HashMap<String, Word>();
		//stem => list of original words
		Map<String, List<String>> stemMap = new HashMap<String, List<String>>();

		for (int i = 0; i < sentences.length; i++) {
			String[] temp = tokenizer.tokenize(sentences[i]);
			for (int j = 0; j < temp.length; j++) {
				String currentWord = temp[j].toLowerCase();
				//only consider words starting with letters
				if (!isWord(currentWord))
					continue;

				String currentStem = getStemmedWord(currentWord, stemmer);

				//skip stopwords
				if (stopwords.contains(currentWord) || stopwords.contains(currentStem))
					continue;

				//words of length >= 3
				if (currentWord.length() < 3)
					continue;
				
				//skip all unicode
				boolean unicode = false;
				for (int k = 0; k < currentWord.length(); k++) {
					char a = currentWord.charAt(k);
					if(a<0||a>127){
						unicode = true;
						break;
					}
				}
				if(unicode) continue;

				if (!wordMap.containsKey(currentStem)) {
					wordMap.put(currentStem, new Word("", 0.0));
					stemMap.put(currentStem, new ArrayList<String>());
				}

				wordMap.get(currentStem).stem = currentStem;
				wordMap.get(currentStem).addSentence(i);
				wordMap.get(currentStem).addCoordinate(new Point(j,i));
				stemMap.get(currentStem).add(temp[j]);
			}
		}

		//restore the most popular word variant
		words = new ArrayList<Word>();
		for (String stem : wordMap.keySet()) {
			Map<String, Integer> variants = new HashMap<String, Integer>();
			for (String w : stemMap.get(stem)) {
				if (!variants.containsKey(w))
					variants.put(w, 0);

				variants.put(w, variants.get(w) + 1);
			}

			String bestVariant = null;
			for (String variant : variants.keySet())
				if (bestVariant == null || variants.get(variant) > variants.get(bestVariant))
					bestVariant = variant;

			wordMap.get(stem).word = bestVariant;
			words.add(wordMap.get(stem));
		}

	}

	private boolean isWord(String currentWord) {
		for (int i = 0; i < currentWord.length(); i++)
			if (!Character.isLetter(currentWord.charAt(i)) && currentWord.charAt(i) != '-')
				return false;
		return true;
	}

	private String[] buildSentences() {
		InputStream modelIn1;
		try {
			modelIn1 = new FileInputStream(getAbsoluteFileName("opennlp/en-sent.bin"));
		} catch (FileNotFoundException e1) {
			throw new RuntimeException(e1);
		}

		SentenceModel model1 = null;
		try {
			model1 = new SentenceModel(modelIn1);
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			if (modelIn1 != null) {
				try {
					modelIn1.close();
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
		}
		SentenceDetectorME sentenceDetector = new SentenceDetectorME(model1);

		// Split into sentences
		String sentences[] = sentenceDetector.sentDetect(text);
		return sentences;
	}

	private Tokenizer buildTokenizer() {
		InputStream modelIn2;
		try {
			modelIn2 = new FileInputStream(getAbsoluteFileName("opennlp/en-token.bin"));
		} catch (FileNotFoundException e1) {
			throw new RuntimeException(e1);
		}
		TokenizerModel model2 = null;
		try {
			model2 = new TokenizerModel(modelIn2);
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			if (modelIn2 != null) {
				try {
					modelIn2.close();
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
		}
		Tokenizer tokenizer = new TokenizerME(model2);
		return tokenizer;
	}

	private String getStemmedWord(String word, Stemmer stemmer) {
		KrovetzStemmer krovetstem =new KrovetzStemmer();
		String prestemmed = krovetstem.stem(word);
		return stemmer.stem(prestemmed);
	}

	private Set<String> buildStopwords() {
		Set<String> stopWords = new HashSet<String>();

		try {
			BufferedReader br = new BufferedReader(new FileReader(getAbsoluteFileName("opennlp/stopwords-en.txt")));
			String line;
			while ((line = br.readLine()) != null) {
				stopWords.add(line.toLowerCase().trim());
			}
			br.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		return stopWords;
	}

	/**
	 * Keep the most important words
	 */
	public void weightFilter(int maxWords, RankingAlgo rankingAlgo) {
		rankingAlgo.buildWeights(this);
		Collections.sort(words);
		Collections.reverse(words);

		if (maxWords > words.size())
			return;

		words = words.subList(0, maxWords);
	}

	/**
	 * Using the hack as my webapp loader can't handle relative paths :(
	 */
	private String getAbsoluteFileName(String name) {
		return Thread.currentThread().getContextClassLoader().getResource(name).getFile();
	}

}
