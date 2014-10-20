package edu.cloudy.main;

import edu.cloudy.colors.ColorScheme;
import edu.cloudy.colors.ColorSchemeRegistry;
import edu.cloudy.layout.LayoutAlgo;
import edu.cloudy.layout.LayoutAlgorithmRegistry;
import edu.cloudy.layout.LayoutResult;
import edu.cloudy.main.cmd.CommandLineArguments;
import edu.cloudy.nlp.SWCDocument;
import edu.cloudy.nlp.Word;
import edu.cloudy.nlp.WordPair;
import edu.cloudy.nlp.ranking.RankingAlgo;
import edu.cloudy.nlp.ranking.RankingAlgorithmRegistry;
import edu.cloudy.nlp.similarity.SimilarityAlgo;
import edu.cloudy.nlp.similarity.SimilarityAlgorithmRegistry;
import edu.cloudy.render.RenderUtils;
import edu.cloudy.render.UIWord;
import edu.cloudy.render.WordCloudRenderer;
import edu.cloudy.utils.FontUtils;
import edu.cloudy.utils.FontUtils.AWTFontProvider;

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

        if (!cmd.parse() || cmd.isPrintUsage())
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
        SWCDocument document = readDocument(cmd);

        //init fonts
        FontUtils.initialize(new AWTFontProvider(cmd.getFont()));
        
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

    private SWCDocument readDocument(CommandLineArguments cmd) throws FileNotFoundException
    {
        Scanner scanner = cmd.getInputFile() != null ? new Scanner(new File(cmd.getInputFile())) : new Scanner(System.in);
        StringBuilder sb = new StringBuilder();
        while (scanner.hasNextLine())
        {
            sb.append(scanner.nextLine() + "\n");
        }
        scanner.close();

        SWCDocument doc = new SWCDocument(sb.toString());
        doc.parse(cmd.getParseOptions());

        return doc;
    }

    private List<Word> ranking(SWCDocument document, CommandLineArguments cmd)
    {
        RankingAlgo algo = RankingAlgorithmRegistry.getById(cmd.getRankAlgorithm());
        document.weightFilter(cmd.getMaxWords(), algo);

        List<Word> words = document.getWords();
        if (words.size() < 10)
            throw new RuntimeException("The input text is too short (" + words.size() + " words)");

        return words;
    }

    private Map<WordPair, Double> computeSimilarity(SWCDocument document, CommandLineArguments cmd)
    {
        SimilarityAlgo algo = SimilarityAlgorithmRegistry.getById(cmd.getSimilarityAlgorithm());
        return algo.computeSimilarity(document);
    }

    private LayoutResult layout(List<Word> words, Map<WordPair, Double> similarity, CommandLineArguments cmd)
    {
        LayoutAlgo algo = LayoutAlgorithmRegistry.getById(cmd.getLayoutAlgorithm());
        algo.setAspectRatio(cmd.getAspectRatio());
        return algo.layout(words, similarity);
    }

    private ColorScheme coloring(List<Word> words, Map<WordPair, Double> similarity, CommandLineArguments cmd)
    {
        ColorScheme colorScheme = ColorSchemeRegistry.getDefault();
        colorScheme.initialize(words, similarity);
        return colorScheme;
    }

    private void visualize(List<Word> words, Map<WordPair, Double> similarity, LayoutResult layout, ColorScheme colorScheme, CommandLineArguments cmd) throws FileNotFoundException
    {
        List<UIWord> uiWords = UIWord.prepareUIWords(words, layout, colorScheme);
        WordCloudRenderer renderer = new WordCloudRenderer(uiWords, cmd.getMaxWidth(), cmd.getMaxHeight());
        byte[] content = RenderUtils.createCloud(renderer, cmd.getOutputFormat());

        OutputStream out = createOutputStream(cmd);

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

    private OutputStream createOutputStream(CommandLineArguments cmd) throws FileNotFoundException
    {
        if (cmd.isAutogenOutputFile() && cmd.getInputFile() != null)
        {
            String inputFile = cmd.getInputFile();
            int index = inputFile.lastIndexOf('.');
            String filename = (index != -1 ? inputFile.substring(0, index) : inputFile);
            filename += "." + cmd.getOutputFormat();
            return new FileOutputStream(filename);
        }
        else
        {
            return cmd.getOutputFile() != null ? new FileOutputStream(cmd.getOutputFile()) : System.out;
        }
    }
}
