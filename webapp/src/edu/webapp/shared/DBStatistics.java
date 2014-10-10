package edu.webapp.shared;

import java.io.Serializable;

/**
 * @author spupyrev
 * Oct 10, 2014
 */
public class DBStatistics implements Serializable
{
    private static final long serialVersionUID = -689532373337529231L;

    private int total;
    private int lastWeek;
    private int lastMonth;

    public DBStatistics()
    {
    }

    public int getTotal()
    {
        return total;
    }

    public void setTotal(int total)
    {
        this.total = total;
    }

    public int getLastWeek()
    {
        return lastWeek;
    }

    public void setLastWeek(int lastWeek)
    {
        this.lastWeek = lastWeek;
    }

    public int getLastMonth()
    {
        return lastMonth;
    }

    public void setLastMonth(int lastMonth)
    {
        this.lastMonth = lastMonth;
    }

}
