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
        //2508428 - Tucson
        //Los Angeles 2442047
        //New York 2459115
        //San Francisco 2487956
        //Seattle 2490383

        int[] codes = {
                1,
                23424977,
                23424936,
                2508428,
                2442047,
                2459115,
                2487956,
                2490383 };

        int id = rnd.nextInt(codes.length);
        return codes[id];
    }
}
