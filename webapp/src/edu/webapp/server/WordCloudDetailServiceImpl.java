package edu.webapp.server;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

import edu.webapp.client.WordCloudDetailService;
import edu.webapp.server.db.DBUtils;
import edu.webapp.shared.DBCloudNotFoundException;
import edu.webapp.shared.WordCloud;

/**
 * @author spupyrev
 * Jan 14, 2014
 */
@SuppressWarnings("serial")
public class WordCloudDetailServiceImpl extends RemoteServiceServlet implements WordCloudDetailService
{

    public WordCloud getWordCloud(int id) throws DBCloudNotFoundException
    {
        return DBUtils.getCloud(id);
    }

}
