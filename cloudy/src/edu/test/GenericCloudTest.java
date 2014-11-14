package edu.test;

import edu.cloudy.layout.*;
import edu.cloudy.layout.packing.ForceDirectedPackingAlgo;
import edu.cloudy.nlp.ParseOptions;
import edu.cloudy.nlp.SWCDocument;
import edu.cloudy.nlp.Word;
import edu.cloudy.nlp.WordPair;
import edu.cloudy.nlp.ranking.LexRankingAlgo;
import edu.cloudy.nlp.ranking.RankingAlgo;
import edu.cloudy.nlp.ranking.TFIDFRankingAlgo;
import edu.cloudy.nlp.ranking.TFRankingAlgo;
import edu.cloudy.nlp.similarity.CosineCoOccurenceAlgo;
import edu.cloudy.nlp.similarity.JaccardCoOccurenceAlgo;
import edu.cloudy.nlp.similarity.LexicalSimilarityAlgo;
import edu.cloudy.nlp.similarity.SimilarityAlgo;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 * @author spupyrev
 * Nov 11, 2014
 */
public class GenericCloudTest
{
    @Rule
    public Timeout globalTimeout = new Timeout(60000);

    @Test
    public void testMDS() throws FileNotFoundException
    {
        basicTest("data/test_med.txt", 50, new ForceDirectedPackingAlgo(), new CosineCoOccurenceAlgo(), new TFRankingAlgo());
    }

    @Test
    public void testSeam() throws FileNotFoundException
    {
        basicTest("data/test_med.txt", 30, new SeamCarvingAlgo(), new JaccardCoOccurenceAlgo(), new TFIDFRankingAlgo());
    }

    @Test
    public void testWordle() throws FileNotFoundException
    {
        basicTest("data/test_long.txt", 100, new WordleAlgo(), new LexicalSimilarityAlgo(), new LexRankingAlgo());
    }

    @Test
    public void testTagAlphabetical() throws FileNotFoundException
    {
        basicTest("data/test_long.txt", 150, new TagCloudAlphabeticalAlgo(), new CosineCoOccurenceAlgo(), new LexRankingAlgo());
    }

    @Test
    public void testTagRank() throws FileNotFoundException
    {
        basicTest("data/test_long.txt", 200, new TagCloudRankAlgo(), new JaccardCoOccurenceAlgo(), new TFRankingAlgo());
    }

    @Test
    public void testContext() throws FileNotFoundException
    {
        basicTest("data/test_med.txt", 62, new ContextPreservingAlgo(), new LexicalSimilarityAlgo(), new TFIDFRankingAlgo());
        basicTest("data/test_short.txt", 62, new ContextPreservingAlgo(), new LexicalSimilarityAlgo(), new TFIDFRankingAlgo());
    }

    @Test
    public void testInflate() throws FileNotFoundException
    {
        basicTest("data/test_long.txt", 75, new InflateAndPushAlgo(), new CosineCoOccurenceAlgo(), new LexRankingAlgo());
        basicTest("data/test_short.txt", 75, new InflateAndPushAlgo(), new CosineCoOccurenceAlgo(), new LexRankingAlgo());
    }

    @Test
    public void testStarForest() throws FileNotFoundException
    {
        basicTest("data/test_med.txt", 80, new StarForestAlgo(), new JaccardCoOccurenceAlgo(), new TFRankingAlgo());
    }

    @Test
    public void testCycleCover() throws FileNotFoundException
    {
        basicTest("data/test_long.txt", 80, new CycleCoverAlgo(), new LexicalSimilarityAlgo(), new TFIDFRankingAlgo());
    }

    public void basicTest(String filename, int maxWords, LayoutAlgo layoutAlgo, SimilarityAlgo similarityAlgo, RankingAlgo rankingAlgo) throws FileNotFoundException
    {
        SWCDocument document = readDocument(filename, maxWords, rankingAlgo);
        WordGraph wordGraph = buildWordGraph(document, similarityAlgo);
        LayoutResult layout = runLayout(wordGraph, layoutAlgo);

        LayoutTest.checkLayout(wordGraph, layout);
    }

    private LayoutResult runLayout(WordGraph wordGraph, LayoutAlgo layoutAlgo)
    {
        return layoutAlgo.layout(wordGraph);
    }

    private WordGraph buildWordGraph(SWCDocument document, SimilarityAlgo similarityAlgo)
    {
        List<Word> words = document.getWords();
        Map<WordPair, Double> similarity = similarityAlgo.computeSimilarity(document);

        return new WordGraph(words, similarity);
    }

    private SWCDocument readDocument(String filename, int maxWordCount, RankingAlgo rankingAlgo) throws FileNotFoundException
    {
        Scanner scanner = new Scanner(new File(filename), "UTF-8");
        StringBuilder sb = new StringBuilder();
        while (scanner.hasNextLine())
        {
            sb.append(scanner.nextLine() + "\n");
        }
        scanner.close();

        SWCDocument doc = new SWCDocument(sb.toString());
        doc.parse(new ParseOptions());
        doc.weightFilter(maxWordCount, rankingAlgo);
        return doc;
    }

}
