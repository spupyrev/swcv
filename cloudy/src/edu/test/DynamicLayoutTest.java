package edu.test;

import edu.cloudy.geom.SWCRectangle;
import edu.cloudy.layout.CycleCoverAlgo;
import edu.cloudy.layout.LayoutResult;
import edu.cloudy.nlp.ParseOptions;
import edu.cloudy.nlp.SWCDynamicDocument;
import edu.cloudy.nlp.Word;
import edu.cloudy.nlp.WordPair;
import edu.cloudy.nlp.ranking.TFRankingAlgo;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class DynamicLayoutTest
{
    public static void main(String[] args)
    {
        String text1 = "";
        String text2 = "";
        try
        {
            Scanner scan = new Scanner(new File("data/wiki_cs.txt"));
            Scanner scan2 = new Scanner(new File("data/wiki_tc.txt"));
            while (scan.hasNextLine())
                text1 += scan.nextLine();
            while (scan2.hasNextLine())
                text2 += scan2.nextLine();

            scan.close();
            scan2.close();
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
            System.exit(1);
        }

        SWCDynamicDocument doc = new SWCDynamicDocument(text1, text2);
        doc.parse(new ParseOptions());
        doc.weightFilter(100, new TFRankingAlgo());

        Map<WordPair, Double> similarity = new HashMap<WordPair, Double>();
        MatchingTest.randomSimilarities(doc.getWords(), similarity);
        LayoutResult algo = runLayout(doc.getWords(), similarity);

        checkIntersections(doc, algo);
    }

    private static void checkIntersections(SWCDynamicDocument doc, LayoutResult algo)
    {
        for (Word w : doc.getDoc1().getWords())
        {
            SWCRectangle rect = algo.getWordPosition(w);
            for (Word w2 : doc.getDoc2().getWords())
            {
                SWCRectangle rect2 = algo.getWordPosition(w2);
                if (!w.word.equals(w2.word) && rect.intersects(rect2))
                {
                    System.out.println(w.word + " in document " + w.documentIndex.toString() + " intersects with " + w2.word + " in document "
                            + w2.documentIndex.toString());
                    System.out.print(rect);
                    System.out.println(rect2);
                }
            }
        }
    }

    private static LayoutResult runLayout(List<Word> words, Map<WordPair, Double> similarity)
    {
        return new CycleCoverAlgo().layout(words, similarity);
    }

}
