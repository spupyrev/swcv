package edu.webapp.server.db;

import edu.cloudy.utils.CommonUtils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * @author spupyrev
 * Jan 14, 2014
 */
public class DBUtils2
{
    public static void main(String[] args)
    {
        try
        {
            System.out.println(getCloudCount());
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    public static int getCloudCount()
    {
        final List<Integer> tmp = new ArrayList<Integer>();
        executeDBAction(new IDBAction()
        {
            public void execute(Connection c, Statement stmt) throws Exception
            {
                //stmt.execute("ALTER TABLE maps_task ALTER COLUMN contiguous_algorithm varchar(64)");
                stmt.execute("ALTER TABLE maps_task ADD COLUMN vis_type varchar(64) NOT NULL DEFAULT gmap");
                //ResultSet rs = stmt.executeQuery("SELECT COUNT(*) As total FROM maps_task");
                tmp.add(0);
            }
        });

        return tmp.get(0);
    }

    private static void executeDBAction(IDBAction action)
    {
        Connection c = null;
        Statement stmt = null;
        try
        {
            Class.forName("org.sqlite.JDBC");
            try
            {
                String path = CommonUtils.getAbsoluteFileName("") + "../../db/gmap.db";
                c = DriverManager.getConnection("jdbc:sqlite:" + path);
            }
            catch (SQLException e)
            {
                throw e;
            }
            System.out.println("Opened database successfully");

            stmt = c.createStatement();

            action.execute(c, stmt);

            stmt.close();
            c.close();
        }
        catch (Exception e)
        {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            throw new RuntimeException(e);
        }
        System.out.println("Action executed successfully");
    }

    interface IDBAction
    {
        void execute(Connection c, Statement stmt) throws Exception;
    };

}
