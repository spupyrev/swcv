package edu.cloudy.utils;

import java.util.function.Supplier;

/**
 * @author spupyrev
 * Oct 11, 2014
 */
public class TimeMeasurer
{
    public static <T> T execute(Supplier<T> function)
    {
        return execute("operation", function);
    }
    
    public static <T> T execute(String description, Supplier<T> function)
    {
        long startTime = System.currentTimeMillis();
        T result = function.get();
        Logger.printf(description + " done in %.3f sec\n", (System.currentTimeMillis() - startTime) / 1000.0);
        return result;
    }
}
