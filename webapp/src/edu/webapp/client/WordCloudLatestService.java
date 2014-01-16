package edu.webapp.client;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

import edu.webapp.shared.WordCloud;

import java.util.List;

/**
 * The client side stub for the RPC service.
 */
@RemoteServiceRelativePath("latest")
public interface WordCloudLatestService extends RemoteService
{
    List<WordCloud> getLatestWordClouds(int limit);
}
