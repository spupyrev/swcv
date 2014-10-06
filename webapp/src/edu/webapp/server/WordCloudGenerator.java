package edu.webapp.server;

import edu.cloudy.clustering.IClusterAlgo;
import edu.cloudy.clustering.KMeansPlusPlus;
import edu.cloudy.colors.ClusterColorScheme;
import edu.cloudy.colors.DynamicColorScheme;
import edu.cloudy.colors.IColorScheme;
import edu.cloudy.colors.SentimentColorScheme;
import edu.cloudy.colors.WebColorScheme;
import edu.cloudy.layout.ContextPreservingAlgo;
import edu.cloudy.layout.CycleCoverAlgo;
import edu.cloudy.layout.InflateAndPushAlgo;
import edu.cloudy.layout.LayoutAlgo;
import edu.cloudy.layout.MDSWithFDPackingAlgo;
import edu.cloudy.layout.SeamCarvingAlgo;
import edu.cloudy.layout.StarForestAlgo;
import edu.cloudy.layout.WordleAlgo;
import edu.cloudy.nlp.ContextDelimiter;
import edu.cloudy.nlp.WCVDocument;
import edu.cloudy.nlp.WCVDynamicDocument;
import edu.cloudy.nlp.WCVSentimentDocument;
import edu.cloudy.nlp.Word;
import edu.cloudy.nlp.WordPair;
import edu.cloudy.nlp.ranking.LexRankingAlgo;
import edu.cloudy.nlp.ranking.RankingAlgo;
import edu.cloudy.nlp.ranking.TFIDFRankingAlgo;
import edu.cloudy.nlp.ranking.TFRankingAlgo;
import edu.cloudy.nlp.similarity.CosineCoOccurenceAlgo;
import edu.cloudy.nlp.similarity.EuclideanAlgo;
import edu.cloudy.nlp.similarity.JaccardCoOccurenceAlgo;
import edu.cloudy.nlp.similarity.LexicalSimilarityAlgo;
import edu.cloudy.nlp.similarity.SimilarityAlgo;
import edu.cloudy.ui.UIWord;
import edu.cloudy.ui.WordCloudRenderer;
import edu.cloudy.utils.FontUtils;
import edu.cloudy.utils.SWCRectangle;
import edu.webapp.server.readers.DocumentExtractor;
import edu.webapp.server.readers.DynamicReader;
import edu.webapp.server.readers.IDocumentReader;
import edu.webapp.server.readers.ISentimentReader;
import edu.webapp.server.utils.RandomTwitterTrendExtractor;
import edu.webapp.server.utils.RandomWikiUrlExtractor;
import edu.webapp.server.utils.RandomYoutubeUrlExtractor;
import edu.webapp.shared.WCSetting;
import edu.webapp.shared.WCSetting.COLOR_SCHEME;
import edu.webapp.shared.WCSetting.LAYOUT_ALGORITHM;
import edu.webapp.shared.WCSetting.RANKING_ALGORITHM;
import edu.webapp.shared.WCSetting.SIMILARITY_ALGORITHM;
import edu.webapp.shared.WordCloud;

import org.apache.batik.dom.svg.SVGDOMImplementation;
import org.apache.batik.svggen.SVGGraphics2D;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.svg.SVGDocument;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class WordCloudGenerator
{
    private static final int SCR_WIDTH = 1024;
    private static final int SCR_HEIGHT = 800;

    private static final int MINIMUM_NUMBER_OF_WORDS = 10;

    public static WordCloud updateWordCloud(int id, String input, WCSetting setting, String ip) throws IllegalArgumentException
    {
        WordCloud updated = buildWordCloud(input, setting, ip);
        updated.setId(saveCloud(id, updated));
        return updated;
    }

    public static WordCloud createWordCloud(String input, WCSetting setting, String ip) throws IllegalArgumentException
    {
        WordCloud newCloud = buildWordCloud(input, setting, ip);
        if (newCloud == null)
            return null;
        newCloud.setId(saveCloud(-1, newCloud));
        return newCloud;
    }

    public static WordCloud buildWordCloud(String input, WCSetting setting, String ip) throws IllegalArgumentException
    {
        logging(input, setting);
        FontUtils.initialize(new SVGFontProvider(setting.getFont().toString()));

        DocumentExtractor extractor = new DocumentExtractor(input);
        IDocumentReader reader = extractor.getReader();
        WCVDocument wcvDocument;
        String text = reader.getText(input);

        WordCloud cloud = null;

        if (reader instanceof DynamicReader)
        {
            DynamicReader r = (DynamicReader)reader;
            cloud = generateWordCloudFromDynamic(input, r.getText1(), r.getText2(), setting, ip);
        }
        else
        {
            if (reader instanceof ISentimentReader && setting.getClusterAlgorithm() == WCSetting.CLUSTER_ALGORITHM.SENTIMENT)
            {
                wcvDocument = new WCVSentimentDocument(((ISentimentReader)reader).getStrChunks());
                text = wcvDocument.getText();
            }
            else if (setting.getClusterAlgorithm() == WCSetting.CLUSTER_ALGORITHM.SENTIMENT
                    && input.contains(ContextDelimiter.SENTIMENT_DELIMITER_TEXT))
            {
                String[] strs = input.split(ContextDelimiter.SENTIMENT_DELIMITER_REGEX);
                wcvDocument = new WCVSentimentDocument(Arrays.asList(strs));
            }
            else
            {
                wcvDocument = new WCVDocument(text);
            }

            List<WordCloudRenderer> renderers = getRenderers(wcvDocument, setting);

            if (renderers == null)
                return null;
            if (renderers.size() != 1)
                throw new RuntimeException("Wrong number of renderers");

            // Ask to render into the SVG Graphics2D implementation.
            WordCloudRenderer renderer = renderers.get(0);
            renderer.setShowRectangles(false);

            String svg = getSvg(renderer);

            Date timestamp = Calendar.getInstance().getTime();
            cloud = createCloud(setting, input, text, timestamp, svg, "", (int)renderer.getActualWidth(), (int)renderer.getActualHeight(), 0, 0, ip);
        }

        return cloud;
    }

    private static IColorScheme getColorScheme(WCVDocument wcvDocument, Map<WordPair, Double> similarity, WCSetting setting)
    {
        IColorScheme wordColorScheme = null;
        if (setting.getClusterAlgorithm().equals(WCSetting.CLUSTER_ALGORITHM.KMEANS))
        {
            int K = guessNumberOfClusters(wcvDocument.getWords().size(), setting);

            IClusterAlgo clusterAlgo = new KMeansPlusPlus(K);
            clusterAlgo.run(wcvDocument.getWords(), similarity);

            wordColorScheme = new ClusterColorScheme(clusterAlgo, wcvDocument.getWords(), setting.getColorScheme().toString());
        }
        else if (setting.getClusterAlgorithm().equals(WCSetting.CLUSTER_ALGORITHM.SENTIMENT))
        {
            wordColorScheme = new SentimentColorScheme(setting.getColorScheme().toString());
        }
        else if (setting.getClusterAlgorithm().equals(WCSetting.CLUSTER_ALGORITHM.DYNAMIC))
        {
            wordColorScheme = new DynamicColorScheme(setting.getColorScheme().toString());
        }
        else
        {
            wordColorScheme = new WebColorScheme(setting.getColorScheme().toString(), setting.getClusterAlgorithm().toString(), wcvDocument.getWords().size());
        }
        return wordColorScheme;
    }

    private static String getSvg(WordCloudRenderer renderer)
    {
        // get svg
        DOMImplementation domImpl = SVGDOMImplementation.getDOMImplementation();

        // Create an instance of org.w3c.dom.Document.
        SVGDocument document = (SVGDocument)domImpl.createDocument(SVGDOMImplementation.SVG_NAMESPACE_URI, "svg", null);
        SVGGraphics2D svgGenerator = new SVGGraphics2D(document);

        // rendering the cloud
        renderer.render(svgGenerator);

        Writer writer;
        try
        {
            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer transformer = tf.newTransformer();
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            writer = new StringWriter();
            transformer.transform(new DOMSource(document), new StreamResult(writer));
            writer.close();
            writer = new StringWriter();
            svgGenerator.stream(writer, true);
            writer.close();
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }

        return writer.toString();
    }

    private static List<WordCloudRenderer> getRenderers(WCVDocument document, WCSetting setting)
    {
        List<WordCloudRenderer> renderers = new ArrayList<WordCloudRenderer>();

        document.parse();

        // ranking
        document.weightFilter(setting.getWordCount(), createRanking(setting.getRankingAlgorithm(), document));

        if (document.getWords().size() < MINIMUM_NUMBER_OF_WORDS)
            return null;

        // similarity
        SimilarityAlgo similarityAlgo = createSimilarity(setting.getSimilarityAlgorithm());
        similarityAlgo.initialize(document);
        similarityAlgo.run();
        Map<WordPair, Double> similarity = similarityAlgo.getSimilarity();

        // algo
        LayoutAlgo layoutAlgo = createLayoutAlgorithm(setting.getLayoutAlgorithm(), document.getWords(), similarity);
        layoutAlgo.setAspectRatio(setting.getAspectRatioDouble());
        layoutAlgo.run();

        if (document instanceof WCVDynamicDocument)
        {
            IColorScheme wordColorScheme1 = getColorScheme(((WCVDynamicDocument)document).getDoc1(), similarity, setting);
            IColorScheme wordColorScheme2 = getColorScheme(((WCVDynamicDocument)document).getDoc2(), similarity, setting);
            List<UIWord> uiWords1 = prepareUIWords(((WCVDynamicDocument)document).getDoc1().getWords(), layoutAlgo, wordColorScheme1);
            List<UIWord> uiWords2 = prepareUIWords(((WCVDynamicDocument)document).getDoc2().getWords(), layoutAlgo, wordColorScheme2);

            renderers.add(new WordCloudRenderer(uiWords1, SCR_WIDTH, SCR_HEIGHT));
            renderers.add(new WordCloudRenderer(uiWords2, SCR_WIDTH, SCR_HEIGHT));
        }
        else
        {
            IColorScheme wordColorScheme = getColorScheme(document, similarity, setting);
            List<UIWord> uiWords = prepareUIWords(document.getWords(), layoutAlgo, wordColorScheme);
            renderers.add(new WordCloudRenderer(uiWords, SCR_WIDTH, SCR_HEIGHT));
        }
        return renderers;
    }

    private static List<UIWord> prepareUIWords(List<Word> words, LayoutAlgo layoutAlgo, IColorScheme colorScheme)
    {
        List<UIWord> res = new ArrayList<UIWord>();
        for (Word w : words)
        {
            UIWord uiWord = new UIWord();
            uiWord.setText(w.word);
            uiWord.setColor(colorScheme.getColor(w));

            //restore the correct box for the dynamic case
            SWCRectangle original = layoutAlgo.getWordPosition(w);
            SWCRectangle result = layoutAlgo.getBoundingBox(w);
            result.moveTo(original.getX(), original.getY());

            uiWord.setRectangle(result);

            res.add(uiWord);
        }

        return res;
    }

    private static WordCloud generateWordCloudFromDynamic(String input, String text1, String text2, WCSetting setting, String ip)
    {
        WordCloud cloud = null;
        String svg1, svg2;

        WCVDynamicDocument doc = new WCVDynamicDocument(text1, text2);
        List<WordCloudRenderer> renderers = getRenderers(doc, setting);
        if (renderers == null)
            return null;

        if (renderers.size() != 2)
            throw new RuntimeException("Wrong number of renderers");

        WordCloudRenderer panel1 = renderers.get(0);
        panel1.setShowRectangles(false);
        svg1 = getSvg(panel1);

        WordCloudRenderer panel2 = renderers.get(1);
        panel2.setShowRectangles(false);
        svg2 = getSvg(panel2);

        Date timestamp = Calendar.getInstance().getTime();

        cloud = createCloud(setting, input, doc.getText(), timestamp, svg1, svg2, (int)panel1.getActualWidth(), (int)panel1.getActualHeight(), (int)panel2.getActualWidth(), (int)panel2.getActualHeight(), ip);

        return cloud;
    }

    public static int saveCloud(int id, WordCloud cloud)
    {
        return WCExporter.saveCloud(id, cloud);
    }

    private static WordCloud createCloud(WCSetting setting, String input, String text, Date timestamp, String svg, String svg2, int width, int height, int width2, int height2, String ip)
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

    private static RankingAlgo createRanking(RANKING_ALGORITHM algo, WCVDocument document)
    {
        if (algo.equals(RANKING_ALGORITHM.TF))
            return new TFRankingAlgo();

        if (algo.equals(RANKING_ALGORITHM.TF_IDF))
            return new TFIDFRankingAlgo();

        if (algo.equals(RANKING_ALGORITHM.LEX))
            return new LexRankingAlgo();

        throw new RuntimeException("something is wrong");
    }

    private static void logging(String text, WCSetting setting)
    {
        //log.info("running algorithm " + setting.toString());
        //log.info("text: " + text);
    }

    private static SimilarityAlgo createSimilarity(SIMILARITY_ALGORITHM algo)
    {
        if (algo.equals(SIMILARITY_ALGORITHM.COSINE))
            return new CosineCoOccurenceAlgo();
        if (algo.equals(SIMILARITY_ALGORITHM.JACCARD))
            return new JaccardCoOccurenceAlgo();
        if (algo.equals(SIMILARITY_ALGORITHM.LEXICAL))
            return new LexicalSimilarityAlgo();
        if (algo.equals(SIMILARITY_ALGORITHM.MATRIXDIS))
            return new EuclideanAlgo();

        throw new RuntimeException("something is wrong");
    }

    private static LayoutAlgo createLayoutAlgorithm(LAYOUT_ALGORITHM algo, List<Word> words, Map<WordPair, Double> similarity)
    {
        if (algo.equals(LAYOUT_ALGORITHM.WORDLE))
            return new WordleAlgo(words, similarity);

        if (algo.equals(LAYOUT_ALGORITHM.CPWCV))
            return new ContextPreservingAlgo(words, similarity);

        if (algo.equals(LAYOUT_ALGORITHM.SEAM))
            return new SeamCarvingAlgo(words, similarity);

        if (algo.equals(LAYOUT_ALGORITHM.INFLATE))
            return new InflateAndPushAlgo(words, similarity);

        if (algo.equals(LAYOUT_ALGORITHM.STAR))
            return new StarForestAlgo(words, similarity);

        if (algo.equals(LAYOUT_ALGORITHM.CYCLE))
            return new CycleCoverAlgo(words, similarity);

        if (algo.equals(LAYOUT_ALGORITHM.MDS))
            return new MDSWithFDPackingAlgo(words, similarity);

        throw new RuntimeException("something is wrong");
    }

    public String getRandomWikiUrl()
    {
        return RandomWikiUrlExtractor.getRandomWikiPage();
    }

    public String getRandomTwitterUrl()
    {
        return RandomTwitterTrendExtractor.getRandomTrend();
    }

    public String getRandomYoutubeUrl()
    {
        return RandomYoutubeUrlExtractor.getRandomUrl();
    }

    private static int guessNumberOfClusters(int n, WCSetting setting)
    {
        if (setting.getColorScheme().equals(COLOR_SCHEME.BEAR_DOWN))
            return 2;
        else if (setting.getColorScheme().equals(COLOR_SCHEME.BLACK))
            return 1;
        else if (setting.getColorScheme().equals(COLOR_SCHEME.BLUE))
            return 1;
        else if (setting.getColorScheme().equals(COLOR_SCHEME.ORANGE))
            return 1;
        else if (setting.getColorScheme().equals(COLOR_SCHEME.GREEN))
            return 1;
        else if (setting.getColorScheme().equals(COLOR_SCHEME.TRISCHEME_1))
            return 4;
        else if (setting.getColorScheme().equals(COLOR_SCHEME.TRISCHEME_2))
            return 4;
        else if (setting.getColorScheme().equals(COLOR_SCHEME.TRISCHEME_3))
            return 4;
        else if (setting.getColorScheme().equals(COLOR_SCHEME.SIMILAR_1))
            return 4;
        else if (setting.getColorScheme().equals(COLOR_SCHEME.SIMILAR_2))
            return 4;
        else if (setting.getColorScheme().equals(COLOR_SCHEME.SIMILAR_3))
            return 4;
        else if (setting.getColorScheme().equals(COLOR_SCHEME.SENTIMENT))
            return 5;
        return Math.max((int)Math.sqrt((double)n / 2), 1);
    }

}
