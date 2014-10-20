package edu.cloudy.main.cmd;

/**
 * @author spupyrev
 * Oct 20, 2014
 */
class IntegerArgumentParser implements BaseArgumentParser
{
    private String prefix;
    private ArgumentParser<Integer> parser;
    private int lowerBound = -1;
    private int upperBound = -1;

    public IntegerArgumentParser(String prefix, ArgumentParser<Integer> parser)
    {
        this(prefix, -1, -1, parser);
    }

    public IntegerArgumentParser(String prefix, int lowerBound, int upperBound, ArgumentParser<Integer> parser)
    {
        this.prefix = prefix;
        this.parser = parser;
        this.lowerBound = lowerBound;
        this.upperBound = upperBound;
    }

    public boolean accept(String option)
    {
        return option.startsWith(prefix);
    }

    public void apply(CommandLineArguments cmd, String option)
    {
        String value = option.substring(prefix.length());
        int iValue = Integer.valueOf(value);
        if (upperBound != -1)
            iValue = Math.min(iValue, upperBound);
        if (lowerBound != -1)
            iValue = Math.max(iValue, lowerBound);
        parser.apply(cmd, iValue);
    }
}