package edu.webapp.server;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

import edu.webapp.client.WordCloudLatestService;
import edu.webapp.server.db.DBUtils;
import edu.webapp.shared.WordCloud;

import java.util.List;

/**
 * @author spupyrev
 * Jan 14, 2014
 */
@SuppressWarnings("serial")
public class WordCloudLatestServiceImpl extends RemoteServiceServlet implements WordCloudLatestService
{

    public List<WordCloud> getLatestWordClouds(int limit)
    {
        return DBUtils.getLatestClouds(limit);
    }

}
