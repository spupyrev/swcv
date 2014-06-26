package edu.cloudy.nlp;

import edu.cloudy.nlp.Word.DocIndex;
import edu.cloudy.nlp.ranking.RankingAlgo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class WCVDynamicDocument extends WCVDocument
{
	private WCVDocument doc1;
	private WCVDocument doc2;

	public WCVDynamicDocument(String text1, String text2)
	{
		super(text1 + ContextDelimiter.DYNAMIC_DELIMITER_TEXT + text2);
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

		List<Word> cwords = new ArrayList<Word>();

		cwords.addAll(doc1.getWords());
		cwords.addAll(doc2.getWords());

		sortListByWeight(cwords);
		this.setWords(cwords);

		assignDocIndex();

		rescaleWeights(this.getWords(), 5);
	}

	private void assignDocIndex()
	{
		HashMap<String, Word> map1 = new HashMap<String, Word>();

		for (Word w : doc1.getWords())
			map1.put(w.stem, w);

		for (Word w : doc2.getWords())
			if (map1.containsKey(w.stem))
			{
				map1.get(w.stem).documentIndex = DocIndex.Both;
				w.documentIndex = DocIndex.Both;
			}
			else
			{
				w.documentIndex = DocIndex.Second;
			}
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
