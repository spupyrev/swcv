package edu.cloudy.main.cmd;

/**
 * @author spupyrev
 * Oct 20, 2014
 * @param <T>
 */
@FunctionalInterface
interface ArgumentParser<T>
{
    void apply(CommandLineArguments cmd, T value);
}