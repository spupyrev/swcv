package edu.webapp.server.readers;

import edu.webapp.server.utils.RandomGoogleTrendExtractor;

/**
 * @author spupyrev
 * Oct 25, 2014
 */
public class HTMLTest
{
    public static void main(String[] args)
    {
        /*String input = "google: ebola";

        GoogleReader reader = new GoogleReader();
        System.out.println(reader.isConnected(input));
        System.out.println(reader.getText(input));*/

        System.out.println(RandomGoogleTrendExtractor.getRandomTrend());
    }
}
