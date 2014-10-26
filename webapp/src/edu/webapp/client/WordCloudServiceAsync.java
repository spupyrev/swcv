package edu.webapp.client;

import com.google.gwt.user.client.rpc.AsyncCallback;

import edu.webapp.shared.WCSettings;
import edu.webapp.shared.WordCloud;

/**
 * The async counterpart of <code>GreetingService</code>.
 */
public interface WordCloudServiceAsync
{
    void buildWordCloud(String input, WCSettings setting, AsyncCallback<WordCloud> callback) throws IllegalArgumentException;

    void getRandomWikiUrl(AsyncCallback<String> callback);
    
    void getRandomTwitterUrl(AsyncCallback<String> callback);
    
    void getRandomYoutubeUrl(AsyncCallback<String> callback);
    
    void getRandomGoogleUrl(AsyncCallback<String> callback);
}
