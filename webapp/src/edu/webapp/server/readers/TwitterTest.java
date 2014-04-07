package edu.webapp.server.readers;

import twitter4j.Trend;
import twitter4j.Trends;
import twitter4j.Twitter;
import twitter4j.TwitterException;

/**
 * @author spupyrev
 * Apr 6, 2014
 */
public class TwitterTest
{
    public static void main(String[] args)
    {
        try
        {
            Twitter twitter = TwitterReader.getTwitterInstance();

            Trends trends = twitter.getPlaceTrends(1);
            //ResponseList<Location> trends = twitter.getAvailableTrends();

            for (Trend loc : trends.getTrends())
            {
                System.out.println(loc.getName());
            }
        }
        catch (TwitterException e)
        {
            throw new RuntimeException(e);
        }
    }
}
