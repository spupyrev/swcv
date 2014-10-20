package edu.cloudy.main.cmd;

/**
 * @author spupyrev
 * Oct 20, 2014
 */
interface BaseArgumentParser
{
    boolean accept(String option);
    void apply(CommandLineArguments cmd, String option);
}