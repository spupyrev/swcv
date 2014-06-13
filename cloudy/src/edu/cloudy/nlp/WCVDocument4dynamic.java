package edu.cloudy.nlp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import edu.cloudy.nlp.Word.DocIndex;
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
	}

	@Override
	public void weightFilter(int maxCount, RankingAlgo rankingAlgo)
	{
		rankingAlgo.buildWeights(doc1);
		rankingAlgo.buildWeights(doc2);

		sortListByWeight(doc1.getWords());
		sortListByWeight(doc2.getWords());
		
		int count1 = Math.min(maxCount, doc1.getWords().size());
		int count2 = Math.min(maxCount, doc2.getWords().size());

		doc1.setWords(doc1.getWords().subList(0, count1));
		doc2.setWords(doc2.getWords().subList(0, count2));

		int one = 0, two = 0;
		List<Word> cwords = new ArrayList<Word>();
		for (; one != count1 && two != count2;)
		{
			Word word1 = doc1.getWords().get(one);
			Word word2 = doc2.getWords().get(two);

			if (word1.weight >= word2.weight)
			{
				cwords.add(word1);
				one++;
			}
			else
			{
				cwords.add(word2);
				two++;
			}
		}
		if (one != count1)
		{
			for (; one < count1; one++)
			{
				Word w = doc1.getWords().get(one);
				cwords.add(w);
			}
		}
		else if (two != count2)
		{
			for (; two < count2; two++)
			{
				Word w = doc2.getWords().get(two);
				cwords.add(w);
			}
		}
		sortListByWeight(cwords);
		this.setWords(cwords);
		HashMap<String,Word> map1 = new HashMap<String,Word>();
		
		for (Word w: doc1.getWords())
			map1.put(w.word, w);
		
		for (Word w: doc2.getWords())
			if (map1.containsKey(w.word)){
				map1.get(w.word).documentIndex = DocIndex.Both;
				w.documentIndex = DocIndex.Both;
			}else{
				w.documentIndex = DocIndex.Second;
			}
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
