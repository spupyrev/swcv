package edu.webapp.server;


import edu.cloudy.utils.CommonUtils;
import edu.webapp.server.db.DBUtils;
import edu.webapp.shared.WCSetting;
import edu.webapp.shared.WordCloud;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * @author spupyrev
 * Jan 8, 2014
 */
public class WCExporter
{
    /**
     * Save the constructed cloud to database
     */
    public static void saveCloud(WordCloud cloud)
    {
        cloud.setId(DBUtils.getCloudCount());
        DBUtils.addCloud(cloud);
    }

    /**
     * Save the constructed cloud to svg file
     */
    public static void saveCloudAsSVG(String filename, WordCloud cloud, WCSetting setting)
    {
        // Set path to .../wordcloud/svgs/
        String path = CommonUtils.getAbsoluteFileName("") + "../../db/svgs/";

        // write svg file
        FileWriter fw;
        try
        {
            fw = new FileWriter(path + filename);
            fw.write(cloud.getSvg());
            fw.close();
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    /**
     * Save the constructed cloud to html file
     */
    public static void saveCloudAsHTML(String filename, WordCloud cloud, WCSetting setting)
    {
        // write a html file for each graph generated
        String path = CommonUtils.getAbsoluteFileName("") + "../../db/htmls/";

        FileWriter fWriter = null;
        BufferedWriter writer = null;
        try
        {
            fWriter = new FileWriter(path + filename);
            writer = new BufferedWriter(fWriter);
            path = getParent(path);
            path = getParent(path);
            path = path + File.separator + "svgs" + File.separator;

            writer.write("<div style=\"width: 1025px; margin: 10px auto\">");
            writer.write("<object data=\"../svgs/" + filename + ".svg\" type=\"image/svg+xml\" height=" + cloud.getHeight() + "px; width="
                    + cloud.getWidth() + "px></object>");
            writer.write("<div style=\"width=100px\">" + setting.toString() + "</div>");
            writer.write("<div style=\"float: right\">Share The Cloud!<a href=\"https://www.facebook.com/sharer/sharer.php?u=http%3A%2F%2Fwordcloud.cs.arizona.edu%2Fhtmlforclouds%2F"
                    + filename
                    + "\" target=\"_blank\"><img src=\"../imgs/facebook.ico\" height=\"24px\" width=\"24px\" Title=\"Share on Facebook\" /></a>");
            writer.write("<a href=\"https://twitter.com/share?url=http%3A%2F%2Fwordcloud.cs.arizona.edu%2Fhtmlforclouds\"" + filename
                    + "\" target=\"_blank\"> <img src=\"../imgs/twitter.ico\" height=\"24px\" width=\"24px\" Title=\"Tweet on Twitter\" /></a></div>");
            writer.write("<a href=\"http://wordcloud.cs.arizona.edu\">Generate a New Cloud Now!</a>");
            writer.write("</div>");

            writer.newLine();
            writer.close();
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    /**
     * if User get the SVG.
     * Save the cloud setting to log
     */
    /*    public static String saveGetSvgLog(WordCloud cloud)
        {
            String path = getAbsoluteFileName("");
            path = getParent(path);
            path = getParent(path);
            path = getParent(path);
            path = path + File.separator + "saved" + File.separator;

            FileWriter fw;
            try
            {
                fw = new FileWriter(path + "saved.log", true);
                fw.write("User saved " + cloud.getFileName() + "\n");
                fw.write(cloud.getSettings());
                fw.write("\n\n");
                fw.close();
            }
            catch (IOException e)
            {
                throw new RuntimeException(e);
            }

            return cloud.getFileName();
        }*/

    private static String getParent(String path)
    {
        return path.substring(0, path.lastIndexOf('/'));
    }
}
