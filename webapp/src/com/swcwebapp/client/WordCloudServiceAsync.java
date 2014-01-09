package com.swcwebapp.client;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.swcwebapp.shared.WCSetting;
import com.swcwebapp.shared.WordCloud;

/**
 * The async counterpart of <code>GreetingService</code>.
 */
public interface WordCloudServiceAsync
{
    void buildWordCloud(String input, WCSetting setting, AsyncCallback<WordCloud> callback) throws IllegalArgumentException;
}
