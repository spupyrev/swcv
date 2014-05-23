package edu.cloudy.nlp;

import java.util.ArrayList;
import java.util.List;

public class WCVDocument4dynamic
{

	private WCVDocument combined;
	private WCVDocument doc1;
	private WCVDocument doc2;

	public WCVDocument4dynamic(String text1, String text2)
	{
		combined = new WCVDocument(text1 + text2);
		doc1 = new WCVDocument(text1);
		doc2 = new WCVDocument(text2);

		doc1.parse();
		List<Word> words1 = doc1.getWords();
		doc2.parse();
		List<Word> words2 = doc2.getWords();
		combined.parse();
		List<Word> cwords = combined.getWords();

		for (Word w : cwords)
			for (Word w2 : words2)
				if (w.word.equals(w2.word))
				{
					w.documentIndex = 1; // in document 2
					for (Word w1 : words1)
						if (w.word.equals(w1.word))
							w.documentIndex = 2;// in both text
				}
	}

	public WCVDocument getDocument()
	{
		return combined;
	}

	public WCVDocument getDocument1(WCVDocument doc)
	{
		List<Word> words1 = new ArrayList<Word>();
		for (Word w : doc.getWords()){
			if (w.documentIndex == 0 || w.documentIndex == 2){
				words1.add(w);
			}
		}
		doc1.setWords(words1);
		return doc1;
	}

	public WCVDocument getDocument2(WCVDocument doc)
	{
		List<Word> words2 = new ArrayList<Word>();
		for (Word w : doc.getWords()){
			if (w.documentIndex == 1 || w.documentIndex == 2){
				words2.add(w);
			}
		}
		doc2.setWords(words2);
		return doc2;
	}
}
