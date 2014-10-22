package edu.webapp.server;

import edu.cloudy.colors.ColorScheme;
import edu.cloudy.colors.ColorSchemeRegistry;
import edu.cloudy.geom.SWCRectangle;
import edu.cloudy.layout.BaseLayoutAlgo;
import edu.cloudy.layout.LayoutAlgo;
import edu.cloudy.layout.LayoutAlgorithmRegistry;
import edu.cloudy.layout.LayoutResult;
import edu.cloudy.nlp.ParseOptions;
import edu.cloudy.nlp.SWCDocument;
import edu.cloudy.nlp.SWCDynamicDocument;
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
import edu.webapp.server.readers.DocumentExtractor;
import edu.webapp.server.readers.DynamicReader;
import edu.webapp.server.readers.IDocumentReader;
import edu.webapp.server.utils.SentimentAnalysis;
import edu.webapp.shared.WCFont;
import edu.webapp.shared.WCSetting;
import edu.webapp.shared.WordCloud;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author spupyrev
 * 
 * Builds a word cloud for using the given setting
 */
public class WordCloudGenerator
{
    private static final int SCR_WIDTH = 1280;
    private static final int SCR_HEIGHT = 800;

    private static final int MINIMUM_NUMBER_OF_WORDS = 10;

    public WordCloudGenerator()
    {
    }

    public WordCloud buildWordCloud(String input, WCSetting setting, String ip) throws IllegalArgumentException
    {
        logging(input, setting);
        FontUtils.initialize(new SVGFontProvider(setting.getFont()));

        DocumentExtractor extractor = new DocumentExtractor(input);
        IDocumentReader reader = extractor.getReader();

        if (reader instanceof DynamicReader)
        {
            DynamicReader r = (DynamicReader)reader;
            return generateWordCloudFromDynamic(input, r.getText1(), r.getText2(), setting, ip);
        }

        //construct and parse document
        String text = reader.getText(input);
        SWCDocument document = new SWCDocument(text);
        document.parse(getParseOptions(setting));

        //too few words
        if (document.getWords().size() < MINIMUM_NUMBER_OF_WORDS)
            return null;

        //compute sentiment values, if needed
        SentimentAnalysis sa = new SentimentAnalysis();
        if (sa.accept(reader, document, setting))
            sa.computeValues(document);

        List<WordCloudRenderer> renderers = getRenderers(document, setting);

        if (renderers.size() != 1)
            throw new RuntimeException("Wrong number of renderers");

        WordCloudRenderer renderer = renderers.get(0);
        String svg = getSvg(renderer, setting.getFont());
        Date timestamp = Calendar.getInstance().getTime();
        return createCloud(setting, input, text, timestamp, svg, "", (int)renderer.getActualWidth(), (int)renderer.getActualHeight(), 0, 0, ip);
    }

    private String getSvg(WordCloudRenderer renderer, WCFont wcFont)
    {
        try
        {
            byte[] content = RenderUtils.createSVG(renderer, new TextStyleHandler(wcFont));
            return new String(content, "UTF-8");
        }
        catch (UnsupportedEncodingException e)
        {
            throw new RuntimeException(e);
        }
    }

    private List<WordCloudRenderer> getRenderers(SWCDocument document, WCSetting setting)
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

        List<WordCloudRenderer> renderers = new ArrayList<WordCloudRenderer>();
        if (document instanceof SWCDynamicDocument)
        {
            SWCDynamicDocument dynDocument = (SWCDynamicDocument)document;
            List<UIWord> uiWords1 = prepareUIWordsForDynamic(dynDocument.getDoc1().getWords(), layoutAlgo, layout, colorScheme);
            List<UIWord> uiWords2 = prepareUIWordsForDynamic(dynDocument.getDoc2().getWords(), layoutAlgo, layout, colorScheme);

            renderers.add(new WordCloudRenderer(uiWords1, SCR_WIDTH, SCR_HEIGHT));
            renderers.add(new WordCloudRenderer(uiWords2, SCR_WIDTH, SCR_HEIGHT));
        }
        else
        {
            List<UIWord> uiWords = UIWord.prepareUIWords(document.getWords(), layout, colorScheme);
            renderers.add(new WordCloudRenderer(uiWords, SCR_WIDTH, SCR_HEIGHT));
        }

        return renderers;
    }

    private List<UIWord> prepareUIWordsForDynamic(List<Word> words, LayoutAlgo algo, LayoutResult layout, ColorScheme colorScheme)
    {
        List<UIWord> res = new ArrayList<UIWord>();
        for (Word w : words)
        {
            UIWord uiWord = new UIWord();
            uiWord.setText(w.word);
            uiWord.setColor(colorScheme.getColor(w));

            //restore the correct box for the dynamic case
            SWCRectangle original = layout.getWordPosition(w);
            SWCRectangle result = ((BaseLayoutAlgo)algo).getBoundingBox(w);
            result.moveTo(original.getX(), original.getY());

            uiWord.setRectangle(result);

            res.add(uiWord);
        }

        return res;
    }

    private WordCloud generateWordCloudFromDynamic(String input, String text1, String text2, WCSetting setting, String ip)
    {
        //create and parse document
        SWCDynamicDocument document = new SWCDynamicDocument(text1, text2);
        document.parse(getParseOptions(setting));

        if (document.getWords().size() < MINIMUM_NUMBER_OF_WORDS)
            return null;

        List<WordCloudRenderer> renderers = getRenderers(document, setting);

        if (renderers.size() != 2)
            throw new RuntimeException("Wrong number of renderers");

        WordCloudRenderer renderer1 = renderers.get(0);
        String svg1 = getSvg(renderer1, setting.getFont());

        WordCloudRenderer renderer2 = renderers.get(1);
        String svg2 = getSvg(renderer2, setting.getFont());

        Date timestamp = Calendar.getInstance().getTime();

        return createCloud(setting, input, document.getText(), timestamp, svg1, svg2, (int)renderer1.getActualWidth(), (int)renderer1.getActualHeight(), (int)renderer2.getActualWidth(), (int)renderer2.getActualHeight(), ip);
    }

    private WordCloud createCloud(WCSetting setting, String input, String text, Date timestamp, String svg, String svg2, int width, int height, int width2, int height2, String ip)
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
        cloud.setCreationDateAsDate(timestamp);
        cloud.setSettings(setting);
        cloud.setSvg(svg);
        cloud.setSvg2(svg2);
        cloud.setWidth(width);
        cloud.setHeight(height);
        cloud.setWidth2(width2);
        cloud.setHeight2(height2);
        cloud.setCreatorIP(ip);

        return cloud;
    }

    private ParseOptions getParseOptions(WCSetting setting)
    {
        ParseOptions options = new ParseOptions();
        options.setMinWordLength(setting.getMinWordLength());
        options.setRemoveNumbers(setting.isRemoveNumbers());
        options.setRemoveStopwords(setting.isRemoveStopwords());
        options.setStemWords(setting.isStemWords());

        return options;
    }

    private void logging(String text, WCSetting setting)
    {
        //System.out.println("running algorithm " + setting.toString());
    }
}
