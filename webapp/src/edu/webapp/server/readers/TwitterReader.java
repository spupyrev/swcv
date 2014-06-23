package edu.webapp.server.readers;

import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TwitterReader implements IDocumentReader, ISentimentReader
{
    private static final Logger log = Logger.getLogger(TwitterReader.class.getName());
    private static final String UNWANTED_PATTERN = "\\s*http[s]?://\\S+\\s*";
    private static final int DEFAULT_NUMBER_OF_TWEETS = 300;

    private String tweetsText;
    private List<String> tweetsList;

    @Override
    public boolean isConnected(String input)
    {
        if (!input.startsWith("twitter:"))
            return false;

        SearchQuery sq = new SearchQuery(input.substring(8).trim());
        sq.parse();
        if (!sq.isValidQuery())
            return false;

        try
        {
            tweetsList = searchTweets(sq);
            tweetsText = concatTweets(tweetsList);
            return true;
        }
        catch (TwitterException e)
        {
            log.info(e.getMessage());
            return false;
        }
    }

    @Override
    public String getText(String input)
    {
        return tweetsText;
    }

    /**
     * @return List of tweets. 
     * So sentiment tool can be applied to each tweets instead of each sentence.
     */
    public List<String> getStrChunks()
    {
        return tweetsList;
    }

    private List<String> searchTweets(SearchQuery sq) throws TwitterException
    {
        List<String> resTweets = new ArrayList();

        Twitter twitter = TwitterReader.getTwitterInstance();
        Query query = new Query(sq.getSearchPhrase());
        query.setCount(sq.getSize());
        query.setResultType(sq.getResultType());
        if (sq.getLang() != null)
            query.setLang(sq.getLang());

        int downloadCount = 0;
        long lowestTweetId = Long.MAX_VALUE;
        QueryResult result;
        while (downloadCount < sq.getSize())
        {
            query.setMaxId(lowestTweetId - 1);
            result = twitter.search(query);
            List<Status> tweets = result.getTweets();
            for (Status tweet : tweets)
            {
                if (tweet.isRetweet() && !sq.isIncludeRetweets())
                    continue;

                lowestTweetId = Math.min(lowestTweetId, tweet.getId());
                resTweets.add(removeLinks(tweet.getText()) + ".\n");
                downloadCount++;
            }

            if (result.hasNext())
                query = result.nextQuery();
            else
                break;
        }

        return resTweets;
    }

    private String concatTweets(List<String> tweets)
    {
        StringBuffer sb = new StringBuffer();
        for (String s : tweets)
            sb.append(s);

        return sb.toString();
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
        cb.setUseSSL(true);
        TwitterFactory tf = new TwitterFactory(cb.build());
        return tf.getInstance();
    }

    public static class SearchQuery
    {
        private String input;
        private String searchPhrase;

        private int size = DEFAULT_NUMBER_OF_TWEETS;
        private String resultType = Query.RECENT;
        private String lang;
        private boolean includeRetweets = false;

        public SearchQuery(String input)
        {
            this.input = input;
        }

        public void parse()
        {
            //ex: uofa size:500 type:[recent,popular,mixed] lang:en include:retweets
            String sz = extractOption("size:(\\d+)");
            if (sz != null)
            {
                size = Integer.valueOf(sz);
                size = Math.min(size, 5000);
            }

            String st = extractOption("type:(\\w+)");
            if ("recent".equalsIgnoreCase(st))
                resultType = Query.RECENT;
            else if ("popular".equalsIgnoreCase(st))
                resultType = Query.POPULAR;
            else if ("mixed".equalsIgnoreCase(st))
                resultType = Query.MIXED;

            String se = extractOption("lang:(\\w+)");
            lang = se;

            String sr = extractOption("include:(\\w+)");
            if ("retweets".equalsIgnoreCase(sr))
                includeRetweets = true;

            searchPhrase = input.trim();
        }

        private String extractOption(String pattern)
        {
            Pattern typePattern = Pattern.compile(pattern);
            Matcher typeMatcher = typePattern.matcher(input);
            if (typeMatcher.find())
            {
                String res = typeMatcher.group(1);
                input = typeMatcher.replaceAll("");
                return res;
            }

            return null;
        }

        public int getSize()
        {
            return size;
        }

        public String getSearchPhrase()
        {
            return searchPhrase;
        }

        public String getResultType()
        {
            return resultType;
        }

        public String getLang()
        {
            return lang;
        }

        public boolean isIncludeRetweets()
        {
            return includeRetweets;
        }

        public boolean isValidQuery()
        {
            return searchPhrase.length() > 0;
        }
    }
}
