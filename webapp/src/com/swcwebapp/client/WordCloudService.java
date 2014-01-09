package com.swcwebapp.client;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
import com.swcwebapp.shared.WCSetting;
import com.swcwebapp.shared.WordCloud;

/**
 * The client side stub for the RPC service.
 */
@RemoteServiceRelativePath("cloud")
public interface WordCloudService extends RemoteService
{
    WordCloud buildWordCloud(String input, WCSetting setting);
}
