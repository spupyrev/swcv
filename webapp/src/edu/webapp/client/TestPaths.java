package edu.webapp.client;

/**
 * @author spupyrev
 * Jan 8, 2014
 */
public class TestPaths
{
    public static void main(String[] args)
    {
        String s = getAbsoluteFileName("") + "../..";
        System.out.println(s);
    }
    public static String getAbsoluteFileName(String name) {
        return Thread.currentThread().getContextClassLoader().getResource(name).getFile();
    }

}
