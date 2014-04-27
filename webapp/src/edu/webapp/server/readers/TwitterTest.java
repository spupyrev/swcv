package edu.webapp.server.readers;

import edu.webapp.server.readers.TwitterReader.SearchQuery;
import twitter4j.Location;
import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;

import java.util.ArrayList;
import java.util.List;

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
            //printTrendLocations(twitter);
            //List<String> tweets = searchTweets("uofa", 100);
            //System.err.println(tweets.size());
            //printTweets(tweets);
            testParseQuery();
        }
        catch (TwitterException e)
        {
            throw new RuntimeException(e);
        }
    }

    private static void testParseQuery() throws TwitterException
    {
        SearchQuery sq = new SearchQuery("abc include:retweets size:250 lang:ru type:mixed some other");
        sq.parse();
        System.out.println(sq.getSize());
        System.out.println(sq.getResultType());
        System.out.println(sq.isIncludeRetweets());
        System.out.println(sq.getLang());
        System.out.println(sq.getSearchPhrase());
    }

    private static void printTweets(List<String> tweets)
    {
        StringBuffer sb = new StringBuffer();
        for (String s : tweets)
            sb.append(s);

        System.out.println(sb.toString());
    }

    private static void printTrendLocations() throws TwitterException
    {
        Twitter twitter = TwitterReader.getTwitterInstance();
        //Trends trends = twitter.getPlaceTrends(1);
        ResponseList<Location> trends = twitter.getAvailableTrends();

        for (Location loc : trends)
        {
            System.out.println(loc.getName() + " " + loc.getWoeid());
        }
    }

    private static List<String> searchTweets(String searchString, int count) throws TwitterException
    {
        List<String> resTweets = new ArrayList();

        Twitter twitter = TwitterReader.getTwitterInstance();
        Query query = new Query(searchString);
        query.setCount(100);
        query.setResultType(Query.RECENT);
        int downloadCount = 0;

        long lowestTweetId = Long.MAX_VALUE;
        QueryResult result;
        while (downloadCount < count)
        {
            query.setMaxId(lowestTweetId - 1);
            result = twitter.search(query);
            List<Status> tweets = result.getTweets();
            for (Status tweet : tweets)
            {
                if (tweet.isRetweet())
                    continue;

                String tmp = tweet.getText();
                lowestTweetId = Math.min(lowestTweetId, tweet.getId());
                resTweets.add(tmp + ".\n");
                downloadCount++;
            }

            if (result.hasNext())
                query = result.nextQuery();
            else
                break;
        }

        return resTweets;
    }
}
