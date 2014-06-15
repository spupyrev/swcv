package edu.cloudy.nlp;

public abstract class ContextDelimiter
{
	public final static String SENTIMENT_DELIMITER_TEXT = "@$@$";
	public final static String SENTIMENT_DELIMITER_REGEX = "\\@\\$\\@\\$";
	
	public final static String DYNAMIC_DELIMITER_TEXT = "$@$@$";
	public final static String DYNAMIC_DELIMITER_REGEX = "\\$\\@\\$\\@\\$";
}
