package edu.webapp.client;

import com.google.gwt.user.client.rpc.AsyncCallback;

import edu.webapp.shared.WordCloud;

import java.util.List;

/**
 * The async counterpart of <code>GreetingService</code>.
 */
public interface WordCloudLatestServiceAsync
{
    void getLatestWordClouds(int limit, AsyncCallback<List<WordCloud>> callback) throws IllegalArgumentException;
}
