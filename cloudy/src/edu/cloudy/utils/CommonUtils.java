package edu.cloudy.utils;

/**
 * @author spupyrev
 * Jan 8, 2014
 */
public class CommonUtils
{
    /**
     * Using the hack as my webapp loader can't handle relative paths :(
     */
    public static String getAbsoluteFileName(String name)
    {
        return Thread.currentThread().getContextClassLoader().getResource(name).getFile();
    }

}
