package edu.webapp.server;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

import edu.webapp.client.WordCloudLatestService;
import edu.webapp.server.db.DBUtils;
import edu.webapp.shared.DBStatistics;
import edu.webapp.shared.WordCloud;

import java.util.List;

/**
 * @author spupyrev
 * Jan 14, 2014
 */
@SuppressWarnings("serial")
public class WordCloudLatestServiceImpl extends RemoteServiceServlet implements WordCloudLatestService
{
    //private static final Logger log = Logger.getLogger(WordCloudLatestServiceImpl.class.getName());

    @Override
    public List<WordCloud> getLatestWordClouds(int limit)
    {
        //long start = System.currentTimeMillis();
        List<WordCloud> latestClouds = DBUtils.getLatestClouds(limit);
        //long end = System.currentTimeMillis();
        //log.info("latest query time: " + (end - start) / 1000.0);
        return latestClouds;
    }

    @Override
    public DBStatistics getStatistics()
    {
        DBStatistics stat = new DBStatistics();

        stat.setTotal(DBUtils.getCloudCount());
        stat.setLastMonth(DBUtils.getCloudCountLastMonth());
        stat.setLastWeek(DBUtils.getCloudCountLastWeek());
        return stat;
    }

}
