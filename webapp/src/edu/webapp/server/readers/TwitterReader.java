package edu.webapp.server.readers;

import java.util.List;
import java.util.logging.Logger;

import twitter4j.*;
import twitter4j.conf.ConfigurationBuilder;

public class TwitterReader implements IDocumentReader
{

	private static final Logger log = Logger.getLogger(TwitterReader.class.getName());
	private String text;

	private Twitter twitter;

	private String QueryLang = "en";
	private String resultType = Query.MIXED;
	private int QueryCount = 100;
	private String unwantedPattern = "\\s*http[s]?://\\S+\\s*";

	@Override
	public boolean isConnected(String input)
	{
		if (input.startsWith("twitter: "))
		{
			return searchTweets(input);
		}
		return false;
	}

	@Override
	public String getText(String input)
	{
		return text;
	}

	private boolean searchTweets(String input)
	{

		auth();
		String[] tags = input.split("\\s+");
		String q = new String();
		for (int i = 1; i < tags.length; i++)
		{
			q += tags[i] + " ";
		}
		try
		{
			Query query = new Query(q);
			query.setCount(QueryCount);
			query.setLang(QueryLang);
			query.setResultType(resultType);
			QueryResult result;
			text = new String();
			do
			{
				result = twitter.search(query);
				List<Status> tweets = result.getTweets();
				for (Status tweet : tweets)
				{
					text += removeLinks(tweet.getText()) + ".";
				}
			} while ((query = result.nextQuery()) != null);
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
		return tweet.replaceAll(unwantedPattern, "");
	}

	private void auth()
	{
		ConfigurationBuilder cb = new ConfigurationBuilder();
		cb.setDebugEnabled(false);
		cb.setOAuthConsumerKey("");
		cb.setOAuthConsumerSecret("");
		cb.setOAuthAccessToken("");
		cb.setOAuthAccessTokenSecret("");
		TwitterFactory tf = new TwitterFactory(cb.build());
		twitter = tf.getInstance();
	}
}
