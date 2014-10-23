package edu.webapp.server.db;

import edu.webapp.shared.DBCloudNotFoundException;
import edu.webapp.shared.WCSetting;
import edu.webapp.shared.WordCloud;

import java.util.Calendar;
import java.util.List;

/**
 * @author spupyrev
 * Jan 14, 2014
 */
public class DBSpeedTests
{
    public static void main(String[] args)
    {
        try
        {
            //WordCloud wc = DBUtils.getCloud(123);
            
            long start = System.currentTimeMillis();
            testListClouds();
            //for (int i = 0; i < 1000; i++)
            //    testAddCloud(wc);
            
            //testCount();

            long end = System.currentTimeMillis();
            System.out.println("execution time: " + (end - start) / 1000.0);
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unused")
    private static void testAddCloud(WordCloud wc) throws DBCloudNotFoundException
    {
        WordCloud res = new WordCloud();
        int id = DBUtils.getCloudCount();
        res.setId(id);
        res.setInputText(wc.getInputText());
        res.setHeight(wc.getHeight());
        res.setWidth(wc.getWidth());
        res.setCreationDateAsDate(wc.getCreationDateAsDate());
        res.setSvg(wc.getSvg());
        res.setCreatorIP(wc.getCreatorIP());
        res.setSetting(wc.getSetting());

        DBUtils.addCloud(res);

        WordCloud wcNew = DBUtils.getCloud(id);
        assert (res.getId() == wcNew.getId());
        assert (res.getCreationDate() == wcNew.getCreationDate());
    }
    
    @SuppressWarnings("unused")
    private static WordCloud createRandomWordCloud()
    {
        WordCloud wc = new WordCloud();
        int id = DBUtils.getCloudCount();
        wc.setId(id);
        wc.setInputText("test");
        wc.setHeight(800);
        wc.setWidth(600);
        wc.setCreationDateAsDate(Calendar.getInstance().getTime());
        wc.setSvg("<svg /> ><g style='fill:white; stroke:white;' /></g");
        wc.setCreatorIP("127.1.3.4");
        wc.setSetting(new WCSetting());
        return wc;
    }
    
    private static void testListClouds()
    {
        List<WordCloud> clouds = DBUtils.getLatestClouds(25);
        System.out.println("size: " + clouds.size());
    }

}
