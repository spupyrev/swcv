package edu.webapp.server.readers;

import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;

import java.util.List;
import java.util.logging.Logger;

public class TwitterReader implements IDocumentReader
{
    private static final Logger log = Logger.getLogger(TwitterReader.class.getName());

    private static final int QUERY_COUNT = 100;
    private static final String UNWANTED_PATTERN = "\\s*http[s]?://\\S+\\s*";

    private String tweets;

    @Override
    public boolean isConnected(String input)
    {
        if (!input.startsWith("twitter:"))
            return false;

        String searchString = input.substring(8).trim();
        if (searchString.length() == 0)
            return false;

        return searchTweets(searchString);
    }

    @Override
    public String getText(String input)
    {
        return tweets;
    }

    private boolean searchTweets(String searchString)
    {
        //log.info("twitter search string: '" + searchString + "'");

        try
        {
            Query query = new Query(searchString);
            query.setCount(QUERY_COUNT);
            query.setLang("en");
            query.setResultType(Query.MIXED);
            QueryResult result;
            StringBuffer sb = new StringBuffer();

            do
            {
                Twitter twitter = getTwitterInstance();
                result = twitter.search(query);
                List<Status> tweets = result.getTweets();
                for (Status tweet : tweets)
                {
                    sb.append(removeLinks(tweet.getText()));
                    sb.append(".\n");
                }
            }
            while ((query = result.nextQuery()) != null);

            tweets = sb.toString();
            //log.info("Twitter result: " + tweets);
            return true;
        }
        catch (TwitterException te)
        {
            log.info(te.getMessage());
            return false;
        }
    }

    private String removeLinks(String tweet)
    {
        return tweet.replaceAll(UNWANTED_PATTERN, "");
    }

    public static Twitter getTwitterInstance()
    {
        ConfigurationBuilder cb = new ConfigurationBuilder();
        cb.setDebugEnabled(false);
        cb.setOAuthConsumerKey("OxGHWmfAzhCtRjq4l161Gg");
        cb.setOAuthConsumerSecret("uMDzxLd4L8dJ08MTNs6RP3SCEgqjgSoQUqttCAO31I");
        cb.setOAuthAccessToken("2352327474-soCM0anV3VW1unwUxlem0ypQ6Ysdu0RprLlLaPz");
        cb.setOAuthAccessTokenSecret("ZIGCCIsyWq6RLg8qBZzRrEmoqmANc6UaGeYJ45QYBIRc4");
        TwitterFactory tf = new TwitterFactory(cb.build());
        return tf.getInstance();
    }
}
