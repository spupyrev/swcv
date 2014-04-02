package edu.webapp.client;

import com.google.gwt.user.client.rpc.AsyncCallback;

import edu.webapp.shared.WCSetting;
import edu.webapp.shared.WordCloud;

/**
 * The async counterpart of <code>GreetingService</code>.
 */
public interface WordCloudDetailServiceAsync
{
    void getWordCloud(int id, AsyncCallback<WordCloud> callback);
    
    void createWordCloud(String input, WCSetting setting, AsyncCallback<WordCloud> callback) throws IllegalArgumentException;
}
