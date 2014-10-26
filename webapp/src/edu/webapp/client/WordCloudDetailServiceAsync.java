package edu.webapp.client;

import com.google.gwt.user.client.rpc.AsyncCallback;

import edu.webapp.shared.WCSettings;
import edu.webapp.shared.WordCloud;

/**
 * The async counterpart of <code>GreetingService</code>.
 */
public interface WordCloudDetailServiceAsync
{
    void getWordCloud(int id, AsyncCallback<WordCloud> callback);

    void updateWordCloud(int id, String input, WCSettings setting, AsyncCallback<WordCloud> callback) throws IllegalArgumentException;
}
