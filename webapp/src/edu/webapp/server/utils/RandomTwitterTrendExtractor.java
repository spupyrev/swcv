package edu.webapp.server.utils;

import edu.webapp.server.readers.TwitterReader;
import twitter4j.Trend;
import twitter4j.Trends;
import twitter4j.Twitter;
import twitter4j.TwitterException;

import java.util.Random;

/**
 * @author spupyrev
 * Jan 11, 2014
 */
public class RandomTwitterTrendExtractor
{
    private static final Random rnd = new Random();

    public static String getRandomTrend()
    {
        try
        {
            Twitter twitter = TwitterReader.getTwitterInstance();

            Trends trends = twitter.getPlaceTrends(getRandomPlace());
            Trend trend = extractRandomTrend(trends.getTrends());

            return "twitter: " + trend.getName();
        }
        catch (TwitterException e)
        {
            throw new RuntimeException(e);
        }
    }

    private static Trend extractRandomTrend(Trend[] trends)
    {
        int id = rnd.nextInt(trends.length);
        return trends[id];
    }

    private static int getRandomPlace()
    {
        //1 - worldwide
        //23424977 - United States
        //23424936 - Russia

        int id = rnd.nextInt(3);
        if (id == 0)
            return 1;
        else if (id == 1)
            return 23424977;
        return 23424936;
    }
}
