package edu.test.misc;

import edu.cloudy.geom.SWCRectangle;
import edu.cloudy.layout.LayoutResult;
import edu.cloudy.layout.WordGraph;
import edu.cloudy.layout.WordleAlgo;
import edu.cloudy.nlp.ParseOptions;
import edu.cloudy.nlp.SWCDynamicDocument;
import edu.cloudy.nlp.Word;
import edu.cloudy.nlp.ItemPair;
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
        String text1 = readText("data/focs");
        String text2 = readText("data/test");

        SWCDynamicDocument doc = new SWCDynamicDocument(text1, text2);
        doc.parse(new ParseOptions());
        doc.weightFilter(10, new TFRankingAlgo());

        Map<ItemPair<Word>, Double> similarity = new HashMap<ItemPair<Word>, Double>();
        MatchingTest.randomSimilarities(doc.getWords(), similarity);
        LayoutResult algo = runLayout(doc.getWords(), similarity);

        checkIntersections(doc, algo);
    }

    private static String readText(String filename)
    {
        StringBuilder text = new StringBuilder();
        try
        {
            Scanner scan = new Scanner(new File(filename));
            while (scan.hasNextLine())
                text.append(scan.nextLine() + "\n");
            scan.close();
        }
        catch (FileNotFoundException e)
        {
            throw new RuntimeException(e);
        }
        return text.toString();
    }

    private static void checkIntersections(SWCDynamicDocument doc, LayoutResult algo)
    {
        for (Word w : doc.getDocument1().getWords())
        {
            SWCRectangle rect = algo.getWordPosition(w);
            System.out.println(rect);
            for (Word w2 : doc.getDocument2().getWords())
            {
                SWCRectangle rect2 = algo.getWordPosition(w2);
                System.out.println(rect2);
                if (!w.word.equals(w2.word) && rect.intersects(rect2))
                {
                    throw new RuntimeException(w.word + " intersects " + w2.word);
                }
            }
        }
    }

    private static LayoutResult runLayout(List<Word> words, Map<ItemPair<Word>, Double> similarity)
    {
        return new WordleAlgo().layout(new WordGraph(words, similarity));
    }

}
