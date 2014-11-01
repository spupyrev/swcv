package edu.webapp.server;

import edu.cloudy.colors.ColorScheme;
import edu.cloudy.colors.ColorSchemeRegistry;
import edu.cloudy.layout.LayoutAlgo;
import edu.cloudy.layout.LayoutAlgorithmRegistry;
import edu.cloudy.layout.LayoutResult;
import edu.cloudy.nlp.ContextDelimiter;
import edu.cloudy.nlp.ParseOptions;
import edu.cloudy.nlp.SWCDocument;
import edu.cloudy.nlp.SWCDynamicDocument;
import edu.cloudy.nlp.WordPair;
import edu.cloudy.nlp.ranking.RankingAlgo;
import edu.cloudy.nlp.ranking.RankingAlgorithmRegistry;
import edu.cloudy.nlp.similarity.SimilarityAlgo;
import edu.cloudy.nlp.similarity.SimilarityAlgorithmRegistry;
import edu.cloudy.render.RenderUtils;
import edu.cloudy.render.UIWord;
import edu.cloudy.render.WordCloudRenderer;
import edu.cloudy.utils.FontUtils;
import edu.webapp.server.readers.DocumentExtractor;
import edu.webapp.server.readers.DynamicReader;
import edu.webapp.server.readers.IDocumentReader;
import edu.webapp.server.utils.SentimentAnalysis;
import edu.webapp.shared.WCSettings;
import edu.webapp.shared.WordCloud;

import java.util.Calendar;
import java.util.List;
import java.util.Map;

/**
 * @author spupyrev
 * 
 * Builds a word cloud for using the given setting
 */
public class WordCloudGenerator
{
    private static final int SCR_WIDTH = 1024;
    private static final int SCR_HEIGHT = 600;

    private static final int MINIMUM_NUMBER_OF_WORDS = 10;

    public WordCloudGenerator()
    {
    }

    public WordCloud buildWordCloud(String input, WCSettings setting, String ip) throws IllegalArgumentException
    {
        logging(input, setting);
        FontUtils.initialize(new SVGFontProvider(setting.getFont()));

        //find appropriate text reader
        IDocumentReader reader = new DocumentExtractor(input).getReader();

        //read input text and parse document
        SWCDocument document = createDocument(input, reader, reader.getText(input));
        document.parse(getParseOptions(setting));

        //too few words
        if (document.getWords().size() < MINIMUM_NUMBER_OF_WORDS)
            return null;

        //compute sentiment values, if needed
        SentimentAnalysis sa = new SentimentAnalysis();
        if (sa.accept(reader, document, setting))
            sa.computeValues(document);

        WordCloudRenderer renderer = getRenderer(document, setting);
        String svg = RenderUtils.createSVGAsString(renderer, new TextStyleHandler(setting.getFont()));
        return createCloud(setting, input, reader.getText(input), svg, (int)renderer.getActualWidth(), (int)renderer.getActualHeight(), ip);
    }

    private SWCDocument createDocument(String input, IDocumentReader reader, String text)
    {
        if (reader instanceof DynamicReader)
        {
            DynamicReader r = (DynamicReader)reader;
            return new SWCDynamicDocument(r.getText1(), r.getText2());
        }
        else if (text.contains(ContextDelimiter.DYNAMIC_DELIMITER_TEXT))
        {
            String[] sources = input.split(ContextDelimiter.DYNAMIC_DELIMITER_REGEX);
            return new SWCDynamicDocument(sources[0], sources[1]);
        }
        else
        {
            return new SWCDocument(text);
        }
    }

    private WordCloudRenderer getRenderer(SWCDocument document, WCSettings setting)
    {
        // ranking
        RankingAlgo rankingAlgo = RankingAlgorithmRegistry.getById(setting.getRankingAlgorithm().getId());
        document.weightFilter(setting.getWordCount(), rankingAlgo);

        // similarity
        SimilarityAlgo similarityAlgo = SimilarityAlgorithmRegistry.getById(setting.getSimilarityAlgorithm().getId());
        Map<WordPair, Double> similarity = similarityAlgo.computeSimilarity(document);

        // layout
        LayoutAlgo layoutAlgo = LayoutAlgorithmRegistry.getById(setting.getLayoutAlgorithm().getId());
        layoutAlgo.setAspectRatio(setting.getAspectRatio().getValue());
        LayoutResult layout = layoutAlgo.layout(document.getWords(), similarity);

        // coloring
        ColorScheme colorScheme = ColorSchemeRegistry.getByName(setting.getColorScheme().getName());
        colorScheme.initialize(document.getWords(), similarity);

        List<UIWord> uiWords = document.prepareUIWords(layout, colorScheme);
        return new WordCloudRenderer(uiWords, SCR_WIDTH, SCR_HEIGHT);
    }

    private WordCloud createCloud(WCSettings setting, String input, String text, String svg, int width, int height, String ip)
    {
        WordCloud cloud = new WordCloud();
        if (!input.startsWith("http://") && !input.startsWith("https://") && !input.startsWith("twitter:") && input.length() > 80)
        {
            cloud.setInputText(input.substring(0, 77) + "...");
        }
        else
        {
            cloud.setInputText(input);
        }

        cloud.setSourceText(text);
        cloud.setCreationDateAsDate(Calendar.getInstance().getTime());
        cloud.setSettings(setting);
        cloud.setSvg(svg);
        cloud.setWidth(width);
        cloud.setHeight(height);
        cloud.setCreatorIP(ip);

        return cloud;
    }

    private ParseOptions getParseOptions(WCSettings setting)
    {
        ParseOptions options = new ParseOptions();
        options.setMinWordLength(setting.getMinWordLength());
        options.setRemoveNumbers(setting.isRemoveNumbers());
        options.setRemoveStopwords(setting.isRemoveStopwords());
        options.setStemWords(setting.isStemWords());
        options.setLanguage(setting.getLanguage());

        return options;
    }

    private void logging(String text, WCSettings setting)
    {
        //System.out.println("running algorithm " + setting.toString());
    }
}
