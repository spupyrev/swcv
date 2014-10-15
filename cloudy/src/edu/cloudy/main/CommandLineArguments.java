package edu.cloudy.main;

/**
 * @author spupyrev
 * Oct 15, 2014
 */
public class CommandLineArguments
{
    private String[] args;

    public CommandLineArguments(String[] args)
    {
        this.args = args;
    }

    public boolean parse()
    {
        return false;
    }

    public void usage()
    {
        System.out.println("cloudy.jar <options> textfile");
        System.out.println("\twhere textfile must contain at least 10 distinct words. Acceptable options are:");
        System.out.println("\t-a k - average number of artificial points added along the bounding box of the labels. If < 0, a suitable value is selected automatically. (by default, -1)");
        System.out.println("\t-b v - polygon line width, with v < 0 for no line. (0)");
        System.out.println("\t-c k - polygon color scheme (1)");
        System.out.println("\t   0 : no polygons");
        System.out.println("\t   1 : pastel");
    }
}
