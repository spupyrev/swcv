package de.tinloaf.cloudy.text;

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

    /**
       * Iterated stemming of the given word.
       */
    public static void main(String[] args)
    {
        AbstractStemmer stemmer = new PorterStemmer();
        String a = "dance \n dancer\n dancing\n dances\n danced\n vertex\n vertices\n";
        System.out.print(stemmer.stemString(a));
    }

    public abstract String stem(String str);

    /**
     * Stems everything in the given string.
     */
    public String stemString(String str)
    {
        str = str.toLowerCase();
        StringBuffer result = new StringBuffer();
        int start = -1;
        for (int j = 0; j < str.length(); j++)
        {
            char c = str.charAt(j);
            if (Character.isLetterOrDigit(c))
            {
                if (start == -1)
                {
                    start = j;
                }
            }
            else if (c == '\'')
            {
                if (start == -1)
                {
                    result.append(c);
                }
            }
            else
            {
                if (start != -1)
                {
                    result.append(stem(str.substring(start, j)));
                    start = -1;
                }
                result.append(c);
            }
        }
        if (start != -1)
        {
            result.append(stem(str.substring(start, str.length())));
        }
        return result.toString();
    }
}
