package edu.webapp.client;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

import edu.webapp.shared.WCSettings;
import edu.webapp.shared.WordCloud;

/**
 * The client side stub for the RPC service.
 */
@RemoteServiceRelativePath("input")
public interface WordCloudService extends RemoteService
{
    WordCloud buildWordCloud(String input, WCSettings setting);
    
    String getRandomWikiUrl();

    String getRandomTwitterUrl();

    String getRandomYoutubeUrl();
}
