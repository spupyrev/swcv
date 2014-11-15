package edu.test;

import edu.cloudy.main.Main;

import org.junit.Test;

/**
 * @author spupyrev
 * Nov 14, 2014
 */
public class CMDToolTest
{
    @Test
    public void testCommandLine()
    {
        String[] argc = new String[] {
                "data/test_med.txt",
                "-s50",
                "-w1000",
                "-C2",
                "-Lmds",
                "-pn",
                "-lde",
                "-Tpdf" };

        Main.main(argc);
    }
}
