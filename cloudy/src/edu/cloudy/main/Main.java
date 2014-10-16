package edu.cloudy.main;

import edu.cloudy.colors.ColorScheme;
import edu.cloudy.colors.ColorSchemeCollection;
import edu.cloudy.layout.LayoutAlgo;
import edu.cloudy.layout.LayoutResult;
import edu.cloudy.layout.WordleAlgo;
import edu.cloudy.nlp.WCVDocument;
import edu.cloudy.nlp.Word;
import edu.cloudy.nlp.WordPair;
import edu.cloudy.nlp.ranking.TFRankingAlgo;
import edu.cloudy.nlp.similarity.CosineCoOccurenceAlgo;
import edu.cloudy.nlp.similarity.SimilarityAlgo;
import edu.cloudy.render.RenderUtils;
import edu.cloudy.render.UIWord;
import edu.cloudy.render.WordCloudRenderer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 * @author spupyrev
 * Oct 15, 2014
 */
public class Main
{
    public static void main(String[] args)
    {
        CommandLineArguments cmd = new CommandLineArguments(args);

        if (!cmd.parse())
        {
            cmd.usage();
            System.exit(1);
        }

        try
        {
            new Main().constructWordCloud(cmd);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            System.exit(1);
        }
    }

    private void constructWordCloud(CommandLineArguments cmd) throws Exception
    {
        // read the document
        WCVDocument document = readDocument(cmd);

        // rank the words
        List<Word> words = ranking(document, cmd);

        // calculate pairwise similarities
        Map<WordPair, Double> similarity = computeSimilarity(document, cmd);

        // layout the words in the plane
        LayoutResult layout = layout(words, similarity, cmd);

        // coloring
        ColorScheme colorScheme = coloring(words, similarity, cmd);

        // draw the result
        visualize(words, similarity, layout, colorScheme, cmd);
    }

    private WCVDocument readDocument(CommandLineArguments cmd) throws FileNotFoundException
    {
        Scanner scanner = cmd.getInputFile() != null ? new Scanner(new File(cmd.getInputFile())) : new Scanner(System.in);
        StringBuilder sb = new StringBuilder();
        while (scanner.hasNextLine())
        {
            sb.append(scanner.nextLine() + "\n");
        }
        scanner.close();

        WCVDocument doc = new WCVDocument(sb.toString());
        doc.parse();

        return doc;
    }

    private List<Word> ranking(WCVDocument document, CommandLineArguments cmd)
    {
        document.weightFilter(50, new TFRankingAlgo());

        List<Word> words = document.getWords();
        if (words.size() < 10)
            throw new RuntimeException("The input text is too short (" + words.size() + " words)");

        return words;
    }

    private Map<WordPair, Double> computeSimilarity(WCVDocument document, CommandLineArguments cmd)
    {
        SimilarityAlgo similarityAlgo = new CosineCoOccurenceAlgo();
        similarityAlgo.initialize(document);
        similarityAlgo.run();
        return similarityAlgo.getSimilarity();
    }

    private LayoutResult layout(List<Word> words, Map<WordPair, Double> similarity, CommandLineArguments cmd)
    {
        LayoutAlgo layoutAlgo = new WordleAlgo(words, similarity);
        return layoutAlgo.layout();
    }

    private ColorScheme coloring(List<Word> words, Map<WordPair, Double> similarity, CommandLineArguments cmd)
    {
        ColorScheme colorScheme = ColorSchemeCollection.getDefault();
        colorScheme.initialize(words, similarity);
        return colorScheme;
    }

    private void visualize(List<Word> words, Map<WordPair, Double> similarity, LayoutResult layout, ColorScheme colorScheme, CommandLineArguments cmd) throws FileNotFoundException
    {
        List<UIWord> uiWords = UIWord.prepareUIWords(words, layout, colorScheme);
        WordCloudRenderer renderer = new WordCloudRenderer(uiWords, 1280, 1024);
        byte[] content = RenderUtils.createCloud(renderer, cmd.getOutputFormat());

        OutputStream out = null;
        //PrintWriter out = null;
        if (cmd.isAutogenOutputFile() && cmd.getInputFile() != null)
        {
            String inputFile = cmd.getInputFile();
            int index = inputFile.lastIndexOf('.');
            String filename = (index != -1 ? inputFile.substring(0, index) : inputFile);
            filename += "." + cmd.getOutputFormat();
            out = new FileOutputStream(filename);
        }
        else
        {
            out = cmd.getOutputFile() != null ? new FileOutputStream(cmd.getOutputFile()) : System.out;
        }

        try
        {
            out.write(content);
            out.close();
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }
}
