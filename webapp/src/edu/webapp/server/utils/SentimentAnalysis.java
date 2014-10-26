package edu.webapp.server.utils;

import edu.arizona.sista.twitter4food.SentimentClassifier;
import edu.cloudy.nlp.ContextDelimiter;
import edu.cloudy.nlp.SWCDocument;
import edu.cloudy.nlp.Word;
import edu.webapp.server.readers.IDocumentReader;
import edu.webapp.server.readers.ISentimentReader;
import edu.webapp.shared.WCColorScheme;
import edu.webapp.shared.WCSettings;
import edu.webapp.shared.registry.WCColorSchemeRegistry;

import java.util.List;

/**
 * @author spupyrev
 * Oct 21, 2014
 */
public class SentimentAnalysis
{
    public SentimentAnalysis()
    {
    }

    public void computeValues(SWCDocument document)
    {
        //String[] chunks = document.getText().split(ContextDelimiter.SENTIMENT_DELIMITER_REGEX);
        List<String> chunks = document.getSentences();

        int[] sentValues = new int[chunks.size()];
        SentimentClassifier sc = SentimentClassifier.resourceClassifier();
        for (int i = 0; i < chunks.size(); ++i)
        {
            String chunk = chunks.get(i).replaceAll(ContextDelimiter.SENTIMENT_DELIMITER_REGEX, "");
            sentValues[i] = sc.predict(chunk);
        }

        for (Word word : document.getWords())
        {
            int posCount = 0, negCount = 0, neuCount = 0;
            for (int si : word.getSentences())
            {
                switch (sentValues[si])
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

            double value = computeSentimentValue(posCount, negCount, neuCount);
            word.setSentimentValue(value);

            //System.out.println(word + ": " + negCount + "," + neuCount + "," + posCount + ":  " + value);
        }
    }

    private double computeSentimentValue(double posCount, double negCount, double neuCount)
    {
        double mx = Math.max(posCount, negCount);
        double mn = Math.min(posCount, negCount);

        double sum = neuCount + 2.0 * (mx - mn);
        if (sum == 0)
            return 0.0;
        return 2.0 * (posCount - negCount) / sum;
    }

    public boolean accept(IDocumentReader reader, SWCDocument document, WCSettings setting)
    {
        WCColorScheme colorScheme = setting.getColorScheme();
        if (!WCColorSchemeRegistry.COLOR_SCHEME_TYPE_SENTIMENT.equals(colorScheme.getType()))
            return false;

        return (reader instanceof ISentimentReader || document.getText().contains(ContextDelimiter.SENTIMENT_DELIMITER_TEXT));
    }
}
