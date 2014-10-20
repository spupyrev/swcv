package edu.test;

import edu.cloudy.nlp.stemming.BaseStemmer;
import edu.cloudy.nlp.stemming.LovinsStemmer;
import edu.cloudy.nlp.stemming.PlingStemmer;
import edu.cloudy.nlp.stemming.PorterStemmer;
import edu.cloudy.nlp.stemming.PreKrovetzStemmer;

@SuppressWarnings("all")
public class StemmerTest
{
    public static void main(String[] args)
    {
        BaseStemmer a = new LovinsStemmer();
        BaseStemmer b = new PorterStemmer();
        BaseStemmer c = new PreKrovetzStemmer();
        BaseStemmer d = new PlingStemmer();
        String[] prestemmed = {
                "dance",
                "dances",
                "dancing",
                "dancer",
                "danced",
                "movie",
                "movies",
                "building",
                "biulds",
                "biult",
                "matrix",
                "matrices",
                "visualization",
                "visualizes",
                "visualized" };
        
        for (String bb : prestemmed)
        {
            System.out.println("Lovins:" + bb + "==>" + a.stem(bb) + "==>" + a.stem(c.stem(bb)));
            System.out.println("Porter:" + bb + "==>" + b.stem(bb) + "==>" + b.stem(c.stem(bb)));
            //System.out.println("Pling:"+ bb+"==>"+d.stem(bb)+"==>"+d.stem(c.stem(bb)));

        }
    }
}
