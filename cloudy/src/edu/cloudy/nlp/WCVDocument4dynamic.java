package edu.cloudy.nlp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import edu.cloudy.nlp.ranking.RankingAlgo;

public class WCVDocument4dynamic extends WCVDocument
{

	private WCVDocument doc1;
	private WCVDocument doc2;

	public WCVDocument4dynamic(String text1, String text2)
	{
		super(text1 + ".\n" + text2);
		doc1 = new WCVDocument(text1);
		doc2 = new WCVDocument(text2);
	}

	@Override
	public void parse()
	{
		super.parse();
		doc1.parse();
		doc2.parse();

		for (Word w : this.getWords())
		{
			if (doc2.hasWord(w.word))
			{
				if (doc1.hasWord(w.word))
				{
					doc1.getWord(w.word).documentIndex = Word.DocIndex.Both;
					doc2.getWord(w.word).documentIndex = Word.DocIndex.Both;
					w.documentIndex = Word.DocIndex.Both;
				}
				else
				{
					doc2.getWord(w.word).documentIndex = Word.DocIndex.Second;
					w.documentIndex = Word.DocIndex.Second;
				}
			}
		}
	}

	@Override
	public void weightFilter(int maxCount, RankingAlgo rankingAlgo)
	{
		rankingAlgo.buildWeights(doc1);
		rankingAlgo.buildWeights(doc2);

		HashMap<String, Word> addedWords = new HashMap<String, Word>();

		sortListByWeight(doc1.getWords());
		sortListByWeight(doc2.getWords());

		doc1.setWords(doc1.getWords().subList(0, maxCount));
		doc2.setWords(doc2.getWords().subList(0, maxCount));

		int one = 0, two = 0;
		List<Word> cwords = new ArrayList<Word>();
		for (; one != maxCount && two != maxCount;)
		{
			Word word1 = doc1.getWords().get(one);
			Word word2 = doc2.getWords().get(two);

			if (word1.weight >= word2.weight)
			{
				if (!addedWords.containsKey(word1.word))
				{
					addedWords.put(word1.word, word1);
					cwords.add(word1);
				}
				one++;
			}
			else
			{
				if (!addedWords.containsKey(word2.word))
				{
					addedWords.put(word2.word, word2);
					cwords.add(word2);
				}
				two++;
			}
		}
		if (one != maxCount)
		{
			for (; one < maxCount; one++)
			{
				Word w = doc1.getWords().get(one);
				cwords.add(w);
			}
		}
		else if (two != maxCount)
		{
			for (; two < maxCount; two++)
			{
				Word w = doc2.getWords().get(two);
				//getWord(w.stem).weight = w.weight;
				cwords.add(w);
			}
		}
		sortListByWeight(cwords);
		this.setWords(cwords);
		rescaleWeights(doc1.getWords(), 5);
		rescaleWeights(doc2.getWords(), 5);
		rescaleWeights(this.getWords(), 5);
	}

	private void sortListByWeight(List<Word> list)
	{
		Collections.sort(list);
		Collections.reverse(list);
	}

	public WCVDocument getDoc1()
	{
		return doc1;
	}

	public WCVDocument getDoc2()
	{
		return doc2;
	}
}
