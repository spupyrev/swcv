package edu.webapp.shared;

/**
 * @author spupyrev
 * Jan 18, 2014
 */
public class DBCloudNotFoundException extends Exception
{
    private static final long serialVersionUID = -2500270236557207735L;

    public DBCloudNotFoundException()
    {
    }

    public DBCloudNotFoundException(String msg)
    {
        super(msg);
    }

}
