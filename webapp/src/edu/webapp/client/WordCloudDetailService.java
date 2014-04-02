package edu.webapp.client;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

import edu.webapp.shared.DBCloudNotFoundException;
import edu.webapp.shared.WCSetting;
import edu.webapp.shared.WordCloud;

/**
 * The client side stub for the RPC service.
 */
@RemoteServiceRelativePath("cloud")
public interface WordCloudDetailService extends RemoteService
{
    WordCloud getWordCloud(int id) throws DBCloudNotFoundException;
    WordCloud createWordCloud(String input, WCSetting setting) throws IllegalArgumentException;
}
