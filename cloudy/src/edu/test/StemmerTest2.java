package edu.test;

import edu.cloudy.nlp.stemming.AbstractStemmer;
import edu.cloudy.nlp.stemming.PorterStemmer;

/**
 * @author spupyrev
 * Jan 10, 2014
 */
public class StemmerTest2
{
    /**
     * Iterated stemming of the given word.
     */
    public static void main(String[] args)
    {
        AbstractStemmer stemmer = new PorterStemmer();
        String a = "dance \n dancer\n dancing\n dances\n danced\n vertex\n vertices\n";
        System.out.print(stemString(stemmer, a));
    }

    /**
     * Stems everything in the given string.
     */
    public static String stemString(AbstractStemmer stemmer, String str)
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
                    result.append(stemmer.stem(str.substring(start, j)));
                    start = -1;
                }
                result.append(c);
            }
        }
        if (start != -1)
        {
            result.append(stemmer.stem(str.substring(start, str.length())));
        }
        return result.toString();
    }

}
