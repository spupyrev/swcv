package edu.cloudy.main;

/**
 * @author spupyrev
 * Oct 15, 2014
 */
public class Main
{
    public static void main(String[] args)
    {
        CommandLineArguments cmd = new CommandLineArguments(args);
        
        if (!cmd.parse())
        {
            cmd.usage();
            System.exit(1);
        }
        
        constructWordCloud(cmd);
    }

    private static void constructWordCloud(CommandLineArguments cmd)
    {
        // TODO Auto-generated method stub
        
    }
}
