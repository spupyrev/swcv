package edu.cloudy.nlp;

import edu.cloudy.nlp.Word.DocIndex;
import edu.cloudy.nlp.ranking.RankingAlgo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class SWCDynamicDocument extends SWCDocument
{
	private SWCDocument doc1;
	private SWCDocument doc2;

	public SWCDynamicDocument(String text1, String text2)
	{
		super(text1 + ContextDelimiter.DYNAMIC_DELIMITER_TEXT + text2);
		
		doc1 = new SWCDocument(text1);
		doc2 = new SWCDocument(text2);
	}

	@Override
	public void parse(ParseOptions parseOptions)
	{
		super.parse(parseOptions);
		
		doc1.parse(parseOptions);
		doc2.parse(parseOptions);
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
			map1.put(w.word, w);

		for (Word w : doc2.getWords())
			if (map1.containsKey(w.word))
			{
				map1.get(w.word).documentIndex = DocIndex.Both;
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

	public SWCDocument getDoc1()
	{
		return doc1;
	}

	public SWCDocument getDoc2()
	{
		return doc2;
	}
}
