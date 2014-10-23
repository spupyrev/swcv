package edu.cloudy.nlp;

import edu.cloudy.nlp.stemming.PorterStemmer;
import edu.cloudy.nlp.stemming.snowball.SnowballStemmer;
import edu.cloudy.nlp.stemming.snowball.ext.daStemmer;

import org.lemurproject.kstem.KrovetzStemmer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author spupyrev
 * Oct 4, 2014
 */
public class TextUtils
{
    /**
     * breaking sentences having line break inside
     * (this is a hack to separate sentences) 
     */
    public static List<String> splitSentences(List<String> sentences)
    {
        List<String> result = new ArrayList();
        for (String sent : sentences)
            result.addAll(splitSentence(sent));

        return result;
    }

    private static List<String> splitSentence(String sentence)
    {
        List<String> result = new ArrayList();
        StringBuilder sb = new StringBuilder();
        for (char c : sentence.toCharArray())
        {
            if (c == '\n')
            {
                if (sb.length() > 0)
                {
                    result.add(sb.toString());
                    sb = new StringBuilder();
                }
            }
            else
            {
                sb.append(c);
            }
        }

        if (sb.length() > 0)
            result.add(sb.toString());

        return result;
    }

    private static Map<String, SnowballStemmer> stemmers = new HashMap();

    public static String stem(String word, ParseOptions parseOptions)
    {
        String languageId = parseOptions.getLanguage().getId();

        tryToCreateStemmer(languageId);
        SnowballStemmer stemmer = stemmers.get(languageId);

        if (stemmer == null)
        {
            //LovinsStemmer stemmer = new LovinsStemmer();
            PorterStemmer enStemmer = new PorterStemmer();
            KrovetzStemmer enStemmer2 = new KrovetzStemmer();

            String prestemmed = enStemmer2.stem(word);
            return enStemmer.stem(prestemmed);
        }
        else
        {
            stemmer.setCurrent(word);
            stemmer.stem();
            String result = stemmer.getCurrent();
            return result;
        }
    }

    private static void tryToCreateStemmer(String languageId)
    {
        if (!stemmers.containsKey(languageId))
        {
            try
            {
                Class stemClass = Class.forName(daStemmer.class.getPackage().getName() + "." + languageId + "Stemmer");
                stemmers.put(languageId, (SnowballStemmer)stemClass.newInstance());
            }
            catch (ClassNotFoundException e)
            {
                stemmers.put(languageId, null);
            }
            catch (InstantiationException e)
            {
                throw new RuntimeException(e);
            }
            catch (IllegalAccessException e)
            {
                throw new RuntimeException(e);
            }
        }
    }
}
