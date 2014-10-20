package edu.cloudy.nlp.stemming;

import java.io.Serializable;

/**
 * Abstract class for stemmers
 */
public abstract class BaseStemmer implements Serializable
{
    private static final long serialVersionUID = 8196452823768416664L;

    public abstract String stem(String str);
}
