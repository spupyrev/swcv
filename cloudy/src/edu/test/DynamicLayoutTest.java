package edu.test;

import edu.cloudy.geom.BoundingBoxGenerator;
import edu.cloudy.geom.SWCRectangle;
import edu.cloudy.layout.CycleCoverAlgo;
import edu.cloudy.layout.LayoutAlgo;
import edu.cloudy.nlp.WCVDynamicDocument;
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

        WCVDynamicDocument doc = new WCVDynamicDocument(text1, text2);
        doc.parse();
        doc.weightFilter(100, new TFRankingAlgo());

        Map<WordPair, Double> similarity = new HashMap<WordPair, Double>();
        MatchingTest.randomSimilarities(doc.getWords(), similarity);
        BoundingBoxGenerator bbg = new BoundingBoxGenerator(1.0);
        LayoutAlgo algo = runLayout(doc.getWords(), similarity, bbg);

        checkIntersections(doc, algo);
    }

    private static void checkIntersections(WCVDynamicDocument doc, LayoutAlgo algo)
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

    private static LayoutAlgo runLayout(List<Word> words, Map<WordPair, Double> similarity, BoundingBoxGenerator bbg)
    {
        LayoutAlgo algo = new CycleCoverAlgo(words, similarity);
        algo.run();
        return algo;
    }

}
