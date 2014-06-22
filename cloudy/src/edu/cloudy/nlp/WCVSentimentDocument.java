package edu.cloudy.nlp;

import edu.arizona.sista.twitter4food.SentimentClassifier;

import java.awt.Point;
import java.util.List;
import java.util.Set;

/**
 * WCVDocument4Sentiment
 * the WCVDocument for sentiment analysis.
 * to avoid writing redundant functions.
 * this class is extended from original WCVDocument.
 *
 * @author jixianli
 *
 */
public class WCVSentimentDocument extends WCVDocument
{
    private List<String> strChunks;
    private int[] sentiValues;
    private String[] sentences;

    public WCVSentimentDocument(List<String> strChunks)
    {
        this.strChunks = strChunks;
        sentiValues = new int[strChunks.size()];
        StringBuffer sb = new StringBuffer();
        
        for (String chunk : strChunks)
        {
            sb.append(chunk + ContextDelimiter.SENTIMENT_DELIMITER_TEXT);
        }
        
        setText(sb.toString());
    }

    @Override
    public void parse()
    {
        super.parse();
        assignSentiValueToChunks();
        calculateSentiValueToWords();
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

    private void assignSentiValueToChunks()
    {
        SentimentClassifier sc = SentimentClassifier.resourceClassifier();
        for (int i = 0; i < sentences.length; ++i)
        {
            sentiValues[i] = sc.predict(sentences[i]);
        }
    }

    private void calculateSentiValueToWords()
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
            double posRatio = posCount / totalCount;
            double negRatio = negCount / totalCount;
            double neuRatio = neuCount / totalCount;
            double sentiValue = getMostSignificant(posRatio, negRatio, neuRatio);
            currentWord.setSentimentValue(sentiValue);
            currentWord.setSentimentCount(posCount, negCount, neuCount, totalCount);
        }
    }

    private double getMostSignificant(double posRatio, double negRatio, double neuRatio)
    {
        if (posRatio > negRatio)
        { // pos > neg
            if (posRatio > neuRatio)
            { // pos > neu && pos > neg
                return posRatio;
            }
            else
            { // neu > pos > neg
                return 0.;
            }
        }
        else
        { // neg >= pos
            if (posRatio == negRatio)
            {
                return 0.;
            }
            else if (negRatio > neuRatio)
            { // neg > neu && neg > pos
                return -negRatio;
            }
            else
            { // neu > neg > pos
                return 0.;
            }
        }
    }
}
