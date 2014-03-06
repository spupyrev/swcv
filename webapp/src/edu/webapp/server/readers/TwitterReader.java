package edu.webapp.server.readers;
import java.util.List;

import twitter4j.*;
import twitter4j.conf.ConfigurationBuilder;
public class TwitterReader implements IDocumentReader{
	
	private String text;
	
	private Twitter twitter;
	
	@Override
	public boolean isConnected(String input) {
		text = new String();
		return searchTweets(input);
	}

	@Override
	public String getText(String input) {
		return text;
	}

	private boolean searchTweets(String input){
		auth();
		if (input.startsWith("twitter:")){
			String[] tags = input.split("\\s+");
			String q = new String();
			for (int i = 1; i<tags.length; i++){
				q += tags[i]+" ";
			}
			try {
	            Query query = new Query(q);
	            query.setCount(100);
	            query.setLang("en");
	            query.setResultType(Query.MIXED);
	            QueryResult result;
	            do {
	                result = twitter.search(query);
	                List<Status> tweets = result.getTweets();
	                for (Status tweet : tweets) {
	                    text += removeLinks(tweet.getText());
	                }
	            } while ((query = result.nextQuery()) != null);
	            return true;
	        } catch (TwitterException te) {
	            te.printStackTrace();
	            System.out.println("Failed to search tweets: " + te.getMessage());
	            return false;
	        }
		}
		return false;
	}

	private String removeLinks(String tweet) {
		return tweet.replaceAll("\\s+http://\\S+\\s*", "");
	}

	private void auth() {
		ConfigurationBuilder cb = new ConfigurationBuilder();
		cb.setDebugEnabled(false)
		  .setOAuthConsumerKey("")
		  .setOAuthConsumerSecret("")
		  .setOAuthAccessToken("")
		  .setOAuthAccessTokenSecret("");
		TwitterFactory tf = new TwitterFactory(cb.build());
		twitter = tf.getInstance();
	}
}
