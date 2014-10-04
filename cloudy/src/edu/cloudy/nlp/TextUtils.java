package edu.cloudy.nlp;

/**
 * @author spupyrev
 * Oct 4, 2014
 */
public class TextUtils
{
    /**
     * adding commas after each line of the input text
     * (this is a hack to separate sentences) 
     */
    public static String splitSentences(String text)
    {
        StringBuilder sb = new StringBuilder();
        for (char c : text.toCharArray())
        {
            if (c == '\n')
                sb.append(".");
            sb.append(c);
        }
        return sb.toString();
    }
}
