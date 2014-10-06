package edu.test;

import edu.cloudy.nlp.WCVDocument;
import edu.cloudy.nlp.Word;
import edu.cloudy.nlp.ranking.TFRankingAlgo;
import edu.cloudy.utils.Logger;
import edu.cloudy.utils.WikipediaXMLReader;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@SuppressWarnings("all")
public class RankingTest
{

	public static void main(String[] args)
	{
		Logger.doLogging = false;

		WCVDocument doc = readDocument("data/weekday_nohash.txt");

		//List<WCVDocument> docs = readDocumentCollection("data/test_wiki1");
		//WCVDocument doc = docs.get(0);
		//System.out.println("#docs: " + docs.size());
		//
		//		doc.weightFilter(10, new LexRankingAlgo());
		//		
		//		for (Word w : doc.getWords())
		//			System.out.println(w.word + "   " + w.stem + "   " + w.weight);

		System.out.println("\nTF RANKING:\n");

		doc.weightFilter(100, new TFRankingAlgo());
		for (Word w : doc.getWords())
			System.out.println(w.word + "    " + w.stem + "    " + w.weight);
		//WCVDocument doc2 = readDocument("data/test_wiki3");/*
		//doc2.weightFilter(10, new TFRankingAlgo());

		//		System.out.println("\nTF RANKING:\n");
		//		
		//		for (Word w : doc2.getWords())
		//			System.out.println(w.word + "   " + w.stem + "   " + w.weight);*/
	}

	private static WCVDocument readDocument(String filename)
	{
		WikipediaXMLReader xmlReader = new WikipediaXMLReader(filename);
		xmlReader.read();
		Iterator<String> texts = xmlReader.getTexts();

		int index = 0;
		for (int i = 0; i < index; i++)
			texts.next();

		WCVDocument wordifier = new WCVDocument(texts.next());
		// 2. build similarities, words etc
		wordifier.parse();
		return wordifier;
	}

	private static List<WCVDocument> readDocumentCollection(String filename)
	{
		WikipediaXMLReader xmlReader = new WikipediaXMLReader(filename);
		xmlReader.read();
		Iterator<String> texts = xmlReader.getTexts();

		List<WCVDocument> docs = new ArrayList<WCVDocument>();
		while (texts.hasNext())
		{
			WCVDocument doc = new WCVDocument(texts.next());
			doc.parse();
			docs.add(doc);
		}

		return docs;
	}

}
