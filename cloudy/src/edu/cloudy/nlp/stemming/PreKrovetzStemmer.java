package edu.cloudy.nlp.stemming;

import org.lemurproject.kstem.KrovetzStemmer;

public class PreKrovetzStemmer extends BaseStemmer
{
    private static final long serialVersionUID = 1L;

    @Override
    public String stem(String str)
    {
        KrovetzStemmer a = new KrovetzStemmer();
        return a.stem(str);
    }

}
