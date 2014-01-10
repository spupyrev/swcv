package edu.cloudy.nlp.stemming;

import java.io.Serializable;

/**
 * Abstract class for stemmers.
 *
 * @author Eibe Frank (eibe@cs.waikato.ac.nz)
 * @version 1.0
 */
public abstract class AbstractStemmer implements Serializable
{
    private static final long serialVersionUID = 8196452823768416664L;

    public abstract String stem(String str);
}
