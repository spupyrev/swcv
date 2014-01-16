package edu.webapp.server.db;

import edu.webapp.shared.WordCloud;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

/**
 * @author spupyrev
 * Jan 14, 2014
 */
public class DBUtils
{
    public static int getCloudCount()
    {
        final List<Integer> tmp = new ArrayList<Integer>();
        executeDBAction(new IDBAction()
        {
            public void execute(Connection c, Statement stmt) throws Exception
            {
                ResultSet rs = stmt.executeQuery("SELECT COUNT(*) As total FROM CLOUD");
                tmp.add(rs.getInt("total"));
            }
        });

        return tmp.get(0);
    }

    public static void addCloud(final WordCloud cloud)
    {
        executeDBAction(new IDBAction()
        {
            public void execute(Connection c, Statement stmt) throws Exception
            {
                String[] fields = new String[] {
                        "ID",
                        "INPUTTEXT",
                        "CREATIONDATE",
                        "WIDTH",
                        "HEIGHT",
                        "SVG",
                        "CREATORIP" };
                Object[] values = new Object[] {
                        cloud.getId(),
                        cloud.getInputText(),
                        cloud.getCreationDate(),
                        cloud.getWidth(),
                        cloud.getHeight(),
                        cloud.getSvg(),
                        cloud.getCreatorIP() };

                StringBuffer sql = new StringBuffer();
                sql.append("INSERT INTO CLOUD (");
                for (int i = 0; i < fields.length; i++)
                {
                    if (i != 0)
                        sql.append(",");
                    sql.append(fields[i]);
                }
                sql.append(") ");

                sql.append(" VALUES (");
                for (int i = 0; i < values.length; i++)
                {
                    if (i != 0)
                        sql.append(",");
                    sql.append("?");
                }
                sql.append(");");

                System.out.println("SQL: " + sql.toString());
                PreparedStatement ps = c.prepareStatement(sql.toString());
                for (int i = 0; i < values.length; i++)
                {
                    addValue(ps, i + 1, values[i]);
                }

                ps.executeUpdate();
            }

            private void addValue(PreparedStatement ps, int index, Object o) throws SQLException
            {
                if (o instanceof String)
                    ps.setString(index, o.toString());
                else if (o instanceof Integer)
                    ps.setInt(index, (Integer)o);
                else if (o == null)
                    ps.setNull(index, Types.VARCHAR);
                else
                    throw new RuntimeException("unknown db type for object: " + o);
            }
        });
    }

    public static WordCloud getCloud(final int id)
    {
        final WordCloud cloud = new WordCloud();

        executeDBAction(new IDBAction()
        {
            public void execute(Connection c, Statement stmt) throws Exception
            {
                ResultSet rs = stmt.executeQuery("SELECT * FROM CLOUD WHERE ID = " + id + ";");

                while (rs.next())
                {
                    convertRSToCloud(cloud, rs);
                }
                rs.close();
            }

        });

        return cloud;
    }

    private static void convertRSToCloud(final WordCloud cloud, ResultSet rs) throws SQLException
    {
        cloud.setId(rs.getInt("ID"));
        cloud.setInputText(rs.getString("INPUTTEXT"));
        cloud.setHeight(rs.getInt("HEIGHT"));
        cloud.setWidth(rs.getInt("WIDTH"));
        cloud.setCreationDate(rs.getString("CREATIONDATE"));
        cloud.setSvg(rs.getString("SVG"));
        cloud.setCreatorIP(rs.getString("CREATORIP"));
    }

    public static List<WordCloud> getLatestClouds(final int limit)
    {
        final List<WordCloud> clouds = new ArrayList<WordCloud>();

        executeDBAction(new IDBAction()
        {
            public void execute(Connection c, Statement stmt) throws Exception
            {
                stmt.setMaxRows(limit - 1);
                ResultSet rs = stmt.executeQuery("SELECT * FROM CLOUD ORDER BY ID DESC;");

                while (rs.next())
                {
                    WordCloud cloud = new WordCloud();
                    convertRSToCloud(cloud, rs);

                    clouds.add(cloud);
                }
                rs.close();
            }
        });

        return clouds;
    }

    public static void createDB()
    {
        executeDBAction(new IDBAction()
        {
            public void execute(Connection c, Statement stmt) throws SQLException
            {
                stmt = c.createStatement();
                String[] fields = new String[] {
                        "ID INT PRIMARY KEY     NOT NULL",
                        "INPUTTEXT TEXT    NOT NULL",
                        "CREATIONDATE CHAR(50) NOT NULL",
                        "WIDTH INT NOT NULL",
                        "HEIGHT INT NOT NULL",
                        "SVG TEXT NOT NULL",
                        "CREATORIP CHAR(50)" };

                StringBuffer sql = new StringBuffer();
                sql.append("CREATE TABLE CLOUD (");
                for (int i = 0; i < fields.length; i++)
                {
                    if (i != 0)
                        sql.append(", ");
                    sql.append(fields[i]);
                }
                sql.append(")");

                stmt.executeUpdate(sql.toString());
            }
        });
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
                c = DriverManager.getConnection("jdbc:sqlite:db/clouds.db");
            }
            catch (SQLException e)
            {
                c = DriverManager.getConnection("jdbc:sqlite:war/db/clouds.db");
            }
            //System.out.println("Opened database successfully");

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
        //System.out.println("Action executed successfully");
    }

    interface IDBAction
    {
        void execute(Connection c, Statement stmt) throws Exception;
    };

}
