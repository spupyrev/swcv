package edu.cloudy.main;

import java.util.Arrays;

/**
 * @author spupyrev
 * Oct 15, 2014
 */
public class CommandLineArguments
{
    private String[] args;

    private String inputFile;
    private String outputFile;
    private boolean autogenOutputFile = false;
    private String outputFormat = "svg";

    public CommandLineArguments(String[] args)
    {
        this.args = args;
    }

    public void usage()
    {
        System.out.println("Usage: java -jar cloudy.jar [options] [input file]");
        System.out.println("\twhere input file must contain a text with at least 10 distinct words. If no input file is supplied, the program reads from stdin.");
        System.out.println("\tAcceptable options are:");
        System.out.println("\t-ofile   - write output to 'file' (stdout)");
        System.out.println("\t-O       - automatically generate an output filename based on the input filename with a .'format' appended");
        System.out.println("\t-Tformat - set output to one of the supported formats (svg)");
        System.out.println("\t     bmp : Windows Bitmap Format");
        System.out.println("\t     eps : Encapsulated PostScript");
        System.out.println("\t     jpg : JPEG");
        System.out.println("\t     pdf : Portable Document Format");
        System.out.println("\t     png : Portable Network Graphics");
        System.out.println("\t      ps : PostScript");
        System.out.println("\t     svg : Scalable Vector Graphics");
        System.out.println("\t     tif : Tag Image File Format");
    }

    public boolean parse()
    {
        Arrays.asList(args).forEach(a -> parseOption(a));
        return true;
    }

    private void parseOption(String option)
    {
        if (option.startsWith("-o"))
        {
            outputFile = option.substring(2);
        }
        else if (option.startsWith("-O"))
        {
            autogenOutputFile = true;
        }
        else if (option.startsWith("-T"))
        {
            outputFormat = option.substring(2);
        }
        else
        {
            inputFile = option;
        }
    }

    public String getInputFile()
    {
        return inputFile;
    }

    public String getOutputFile()
    {
        return outputFile;
    }

    public boolean isAutogenOutputFile()
    {
        return autogenOutputFile;
    }

    public String getOutputFormat()
    {
        return outputFormat;
    }

}
