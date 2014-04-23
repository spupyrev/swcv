package edu.cloudy.nlp;

import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.cloudy.nlp.stemming.AbstractStemmer;
import edu.cloudy.nlp.stemming.PorterStemmer;
import opennlp.tools.tokenize.Tokenizer;
import edu.arizona.sista.twitter4food.SentimentClassifier;

/**
 * WCVDocument4Sentiment
 * the WCVDocument for sentiment analysis.
 * to avoid writing redundant functions.
 * this class is extended from original WCVDocument.
 *
 * @author jixianli
 *
 */
public class WCVDocument4Sentiment extends WCVDocument
{

	List<String> strChunks;
	int[] sentiValues;
	String[] sentences;

	public WCVDocument4Sentiment(List<String> strChunks)
	{
		String text = "";
		this.strChunks = strChunks;
		sentiValues = new int[strChunks.size()];
		// following statements just for WCVDocument
		for (String str : strChunks)
		{
			text += str;
		}
		setText(text);
	}

	@Override
	public void parse()
	{
		super.parse();
		AssignSentiValueToChunks();
		CalculateSentiValueToWords();
	}

	@Override
	public String[] buildSentences()
	{
		String[] strs = new String[strChunks.size()];
		for (int i = 0; i < strs.length; ++i)
		{
			strs[i] = strChunks.get(i);
		}
		sentences = strs;
		return strs;
	}

	private void AssignSentiValueToChunks()
	{
		SentimentClassifier sc = SentimentClassifier.resourceClassifier();
		System.out.println("sentiValues:");
		for (int i = 0; i < sentences.length; ++i)
		{
			sentiValues[i] = sc.predict(sentences[i]);
			System.out.print(sentiValues[i]);
		}
		System.out.println();
	}

	private void CalculateSentiValueToWords()
	{
		List<Word> words = getWords();
		for (int i = 0; i < words.size(); ++i)
		{
			int posCount = 0, negCount = 0, neuCount = 0;
			Word currentWord = words.get(i);
			Set<Point> coordinates = words.get(i).getCoordinates();
			for (Point p : coordinates)
			{
				switch (sentiValues[p.y])
				{
				case 0:
					neuCount++;
					break;
				case 1:
					posCount++;
					break;
				case -1:
					negCount++;
					break;
				}
			}
			double totalCount = posCount + negCount + neuCount;
			double posRatio = (double) posCount / totalCount;
			double negRatio = (double) negCount / totalCount;
			double neuRatio = (double) neuCount / totalCount;
			double sentiValue = getMostSignificant(posRatio, negRatio, neuRatio);
			currentWord.setSentiValue(sentiValue);
		}
	}

	private double getMostSignificant(double posRatio, double negRatio, double neuRatio)
	{
		if (posRatio > negRatio)
		{ 	// pos > neg
			if (posRatio > neuRatio)
			{	// pos > neu && pos > neg
				return posRatio;
			}
			else
			{	// neu > pos > neg
				return 0.;
			}
		}
		else
		{	// neg > pos
			if (negRatio > neuRatio)
			{	// neg > neu && neg > pos
				return -negRatio;
			}
			else
			{	// neu > neg > pos
				return 0.;
			}
		}
	}
}
