package edu.webapp.server.db;

import edu.cloudy.utils.CommonUtils;
import edu.webapp.shared.*;
import edu.webapp.shared.WCSetting.ASPECT_RATIO;
import edu.webapp.shared.WCSetting.CLUSTER_ALGORITHM;
import edu.webapp.shared.WCSetting.COLOR_SCHEME;
import edu.webapp.shared.WCSetting.LAYOUT_ALGORITHM;
import edu.webapp.shared.WCSetting.RANKING_ALGORITHM;
import edu.webapp.shared.WCSetting.SIMILARITY_ALGORITHM;

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

	public static void updateCloud(final WordCloud cloud)
	{
		executeDBAction(new IDBAction()
		{
			public void execute(Connection c, Statement stmt) throws Exception
			{
				String[] fields = new String[] { "WIDTH", "HEIGHT", "SVG", "SVG2", "CREATOR_IP", "WORD_COUNT", "SIMILARITY_ALGO", "RANKING_ALGO", "LAYOUT_ALGO", "FONT", "COLOR_SCHEME", "COLOR_DISTR", "ASPECT_RATIO" };
				WCSetting settings = cloud.getSettings();
                Object[] values = new Object[] { cloud.getWidth(), cloud.getHeight(), cloud.getSvg(), cloud.getSvg2(), cloud.getCreatorIP(), settings.getWordCount(),
					settings.getSimilarityAlgorithm().toString(), settings.getRankingAlgorithm().toString(), settings.getLayoutAlgorithm().toString(),
					settings.getFont().getName(), settings.getColorScheme().toString(), settings.getClusterAlgorithm().toString(), settings.getAspectRatio().toString() };

				StringBuffer sql = new StringBuffer();
				sql.append("UPDATE CLOUD SET ");
				for (int i = 0; i < fields.length; ++i)
				{
					if (i != 0)
						sql.append(",");
					sql.append(fields[i]);
					sql.append("=");
					sql.append("?");
				}

				sql.append("WHERE ID = ?;");

				PreparedStatement ps = c.prepareStatement(sql.toString());
				for (int i = 0; i < values.length; i++)
				{
					addValue(ps, i + 1, values[i]);
				}
				addValue(ps, values.length + 1, cloud.getId());
				ps.execute();

			}

			private void addValue(PreparedStatement ps, int index, Object o) throws SQLException
			{
				if (o instanceof String)
					ps.setString(index, o.toString());
				else if (o instanceof Integer)
					ps.setInt(index, (Integer) o);
				else if (o == null)
					ps.setNull(index, Types.VARCHAR);
				else
					throw new RuntimeException("unknown db type for object: " + o);
			}
		});

	};

	public static void addCloud(final WordCloud cloud)
	{
		executeDBAction(new IDBAction()
		{
			public void execute(Connection c, Statement stmt) throws Exception
			{
				String[] fields = new String[] { "ID", "INPUT_TEXT", "SOURCE_TEXT", "CREATION_DATE", "WIDTH", "HEIGHT", "WIDTH2", "HEIGHT2", "SVG", "SVG2", "CREATOR_IP", "WORD_COUNT",
					"SIMILARITY_ALGO", "RANKING_ALGO", "LAYOUT_ALGO", "FONT", "COLOR_SCHEME", "COLOR_DISTR", "ASPECT_RATIO" };

				WCSetting settings = cloud.getSettings();
                Object[] values = new Object[] { cloud.getId(), cloud.getInputText(), cloud.getSourceText(), cloud.getCreationDate(), cloud.getWidth(), cloud.getHeight(), cloud.getWidth2(),
					cloud.getHeight2(), cloud.getSvg(), cloud.getSvg2(), cloud.getCreatorIP(), settings.getWordCount(), settings.getSimilarityAlgorithm().toString(),
					settings.getRankingAlgorithm().toString(), settings.getLayoutAlgorithm().toString(), settings.getFont().getName(),
					settings.getColorScheme().toString(), settings.getClusterAlgorithm().toString(), settings.getAspectRatio().toString() };

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

				//System.out.println("SQL: " + sql.toString());
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
					ps.setInt(index, (Integer) o);
				else if (o == null)
					ps.setNull(index, Types.VARCHAR);
				else
					throw new RuntimeException("unknown db type for object: " + o);
			}
		});
	}

	public static WordCloud getCloud(final int id) throws DBCloudNotFoundException
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

		if (cloud.getId() != id)
			throw new DBCloudNotFoundException("No word cloud exists with id=" + id);

		return cloud;
	}

    private static void convertRSToCloud(final WordCloud cloud, ResultSet rs) throws SQLException
    {
        cloud.setId(rs.getInt("ID"));
        cloud.setInputText(rs.getString("INPUT_TEXT"));
        cloud.setSourceText(rs.getString("SOURCE_TEXT"));
        cloud.setHeight(rs.getInt("HEIGHT"));
        cloud.setWidth(rs.getInt("WIDTH"));
        cloud.setHeight2(rs.getInt("HEIGHT2"));
        cloud.setWidth2(rs.getInt("WIDTH2"));
        cloud.setCreationDate(rs.getString("CREATION_DATE"));
        cloud.setSvg(rs.getString("SVG"));
        cloud.setSvg2(rs.getString("SVG2"));
        cloud.setCreatorIP(rs.getString("CREATOR_IP"));

        cloud.setSettings(new WCSetting());
        cloud.getSettings().setWordCount(rs.getInt("WORD_COUNT"));
        cloud.getSettings().setSimilarityAlgorithm(SIMILARITY_ALGORITHM.valueOf(rs.getString("SIMILARITY_ALGO")));
        cloud.getSettings().setRankingAlgorithm(RANKING_ALGORITHM.valueOf(rs.getString("RANKING_ALGO")));
        cloud.getSettings().setLayoutAlgorithm(LAYOUT_ALGORITHM.valueOf(rs.getString("LAYOUT_ALGO")));
        cloud.getSettings().setFont(WCFontCollection.getByName(rs.getString("FONT")));
        cloud.getSettings().setColorScheme(COLOR_SCHEME.valueOf(rs.getString("COLOR_SCHEME")));
        cloud.getSettings().setClusterAlgorithm(CLUSTER_ALGORITHM.valueOf(rs.getString("COLOR_DISTR")));
        cloud.getSettings().setAspectRatio(ASPECT_RATIO.valueOf(rs.getString("ASPECT_RATIO")));
    }

    private static void convertRSToCloudLight(final WordCloud cloud, ResultSet rs) throws SQLException
    {
        cloud.setId(rs.getInt("ID"));
        cloud.setInputText(rs.getString("INPUT_TEXT"));
        cloud.setCreationDate(rs.getString("CREATION_DATE"));
        cloud.setCreatorIP(rs.getString("CREATOR_IP"));
    }

	public static List<WordCloud> getLatestClouds(final int limit)
	{
		final List<WordCloud> clouds = new ArrayList<WordCloud>();

		executeDBAction(new IDBAction()
		{
			public void execute(Connection c, Statement stmt) throws Exception
			{
				stmt.setMaxRows(limit - 1);
				ResultSet rs = stmt.executeQuery("SELECT ID, CREATION_DATE, INPUT_TEXT, CREATOR_IP FROM CLOUD ORDER BY ID DESC;");

				while (rs.next())
				{
					WordCloud cloud = new WordCloud();
					convertRSToCloudLight(cloud, rs);

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
				String[] fields = new String[] { "ID INT PRIMARY KEY     NOT NULL", "INPUT_TEXT TEXT    NOT NULL", "SOURCE_TEXT TEXT NOT NULL", "CREATION_DATE CHAR(50) NOT NULL",
					"WIDTH INT NOT NULL", "HEIGHT INT NOT NULL", "WIDTH2 INT NOT NULL", "HEIGHT2 INT NOT NULL", "SVG TEXT NOT NULL", "SVG2 TEXT NOT NULL", "CREATOR_IP CHAR(50)",
					"WORD_COUNT INT NOT NULL", "SIMILARITY_ALGO CHAR(50)", "RANKING_ALGO CHAR(50)", "LAYOUT_ALGO CHAR(50)", "FONT CHAR(50)", "COLOR_SCHEME CHAR(50)", "COLOR_DISTR CHAR(50)", "ASPECT_RATIO CHAR(50)" };

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
				String path = CommonUtils.getAbsoluteFileName("") + "../../db/clouds.db";
				c = DriverManager.getConnection("jdbc:sqlite:" + path);
			}
			catch (SQLException e)
			{
				//c = DriverManager.getConnection("jdbc:sqlite:war/db/clouds.db");
				throw e;
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
	}

}
