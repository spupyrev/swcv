package edu.cloudy.main.cmd;

/**
 * @author spupyrev
 * Oct 20, 2014
 */
class StringArgumentParser implements BaseArgumentParser
{
    private String prefix;
    private ArgumentParser<String> parser;
    
    public StringArgumentParser(String prefix, ArgumentParser<String> parser)
    {
        this.prefix = prefix;
        this.parser = parser;
    }
    
    public boolean accept(String option)
    {
        return option.startsWith(prefix);
    }
    
    public void apply(CommandLineArguments cmd, String option)
    {
        String value = option.substring(prefix.length());
        parser.apply(cmd, value);
    }
}