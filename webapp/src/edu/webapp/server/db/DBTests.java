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
public class DBTests
{
	public static void main(String[] args)
	{
		try
		{
			//DBUtils.createDB();
			//testAddCloud();
			//testListClouds();
			//testUpdateClouds();
			//testListClouds();
		    
            testCount();
            testCountLastMonth();
            testCountLastWeek();
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}

    @SuppressWarnings("unused")
	private static void testUpdateClouds()
	{
		WordCloud cloud = new WordCloud();
		cloud.setId(1);
		cloud.setInputText("test updated");
		cloud.setSourceText("test source updated");
		cloud.setHeight(400);
		cloud.setWidth(300);
		cloud.setCreationDateAsDate(Calendar.getInstance().getTime());
		cloud.setSvg("meow meow meow");
		cloud.setCreatorIP("127.1.3.4");
		cloud.setSettings(new WCSetting());
		DBUtils.updateCloud(cloud);
	}

    @SuppressWarnings("unused")
	private static void testAddCloud() throws DBCloudNotFoundException
	{
		WordCloud wc = new WordCloud();
		//int id = new Random().nextInt(1234567);
		int id = DBUtils.getCloudCount();
		wc.setId(id);
		wc.setInputText("test");
		wc.setSourceText("test source");
		wc.setHeight(800);
		wc.setWidth(600);
		wc.setCreationDateAsDate(Calendar.getInstance().getTime());
		wc.setSvg("<svg /> ><g style='fill:white; stroke:white;' /></g");
		wc.setCreatorIP("127.1.3.4");
		wc.setSettings(new WCSetting());

		DBUtils.addCloud(wc);

		WordCloud wcNew = DBUtils.getCloud(id);
		assert (wc.getId() == wcNew.getId());
		assert (wc.getCreationDate() == wcNew.getCreationDate());
	}

    @SuppressWarnings("unused")
	private static void testListClouds()
	{
		List<WordCloud> clouds = DBUtils.getLatestClouds(25);
		for (WordCloud c : clouds)
		{
			System.out.println("id:     " + c.getId());
			System.out.println("input:  " + c.getInputText());
			System.out.println("source: " + c.getSourceText());
			System.out.println("height: " + c.getHeight());
			System.out.println("width:  " + c.getWidth());
			System.out.println("svg:    " + c.getSvg());
		}
	}

    private static void testCount()
    {
        System.out.println("size: " + DBUtils.getCloudCount());
    }

    private static void testCountLastMonth()
    {
        System.out.println("size last month: " + DBUtils.getCloudCountLastMonth());
    }

    private static void testCountLastWeek()
    {
        System.out.println("size last week: " + DBUtils.getCloudCountLastWeek());
    }
}
