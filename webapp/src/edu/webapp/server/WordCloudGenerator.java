package edu.webapp.server;

import java.io.StringWriter;
import java.io.Writer;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.logging.Logger;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.batik.dom.svg.SVGDOMImplementation;
import org.apache.batik.svggen.SVGGraphics2D;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.svg.SVGDocument;

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
import edu.cloudy.layout.MDSAlgo;
import edu.cloudy.layout.SeamCarvingAlgo;
import edu.cloudy.layout.StarForestAlgo;
import edu.cloudy.layout.WordleAlgo;
import edu.cloudy.nlp.ContextDelimiter;
import edu.cloudy.nlp.WCVDocument;
import edu.cloudy.nlp.WCVDynamicDocument;
import edu.cloudy.nlp.WCVSentimentDocument;
import edu.cloudy.nlp.WordPair;
import edu.cloudy.nlp.ranking.LexRankingAlgo;
import edu.cloudy.nlp.ranking.RankingAlgo;
import edu.cloudy.nlp.ranking.TFIDFRankingAlgo;
import edu.cloudy.nlp.ranking.TFRankingAlgo;
import edu.cloudy.nlp.similarity.CosineCoOccurenceAlgo;
import edu.cloudy.nlp.similarity.DiceCoefficientAlgo;
import edu.cloudy.nlp.similarity.EuclideanAlgo;
import edu.cloudy.nlp.similarity.JaccardCoOccurenceAlgo;
import edu.cloudy.nlp.similarity.LexicalSimilarityAlgo;
import edu.cloudy.nlp.similarity.SimilarityAlgo;
import edu.cloudy.ui.WordCloudPanel;
import edu.cloudy.utils.BoundingBoxGenerator;
import edu.cloudy.utils.FontUtils;
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

public class WordCloudGenerator
{
	private static final Logger log = Logger.getLogger(WordCloudGenerator.class.getName());

	public static WordCloud updateWordCloud(int id, String input, WCSetting setting, String ip) throws IllegalArgumentException
	{
		WordCloud updated = buildWordCloud(input, setting, ip);
		updated.setId(saveCloud(id, updated));
		return updated;
	}

	public static WordCloud createWordCloud(String input, WCSetting setting, String ip) throws IllegalArgumentException
	{
		WordCloud newcloud = buildWordCloud(input, setting, ip);
		newcloud.setId(saveCloud(-1, newcloud));
		return newcloud;
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
			DynamicReader r = (DynamicReader) reader;
			cloud = generateWordCloudFromDynamic(input, r.getText1(), r.getText2(), setting, ip);
		}
		else
		{
			if (reader instanceof ISentimentReader && setting.getColorDistribute() == WCSetting.COLOR_DISTRIBUTE.SENTIMENT)
			{
				wcvDocument = new WCVSentimentDocument(((ISentimentReader) reader).getStrChunks());
				text = wcvDocument.getText();
			}
			else if (setting.getColorDistribute() == WCSetting.COLOR_DISTRIBUTE.SENTIMENT && input.contains(ContextDelimiter.SENTIMENT_DELIMITER_TEXT))
			{
				String[] strs = input.split(ContextDelimiter.SENTIMENT_DELIMITER_REGEX);
				wcvDocument = new WCVSentimentDocument(Arrays.asList(strs));
			}
			else
			{
				wcvDocument = new WCVDocument(text);
			}

			// parse text
			wcvDocument.parse();

			// ranking
			wcvDocument.weightFilter(setting.getWordCount(), createRanking(setting.getRankingAlgorithm(), wcvDocument));

			if (wcvDocument.getWords().isEmpty())
				return null;

			// similarity
			SimilarityAlgo similarityAlgo = createSimilarity(setting.getSimilarityAlgorithm());
			similarityAlgo.initialize(wcvDocument);
			similarityAlgo.run();
			Map<WordPair, Double> similarity = similarityAlgo.getSimilarity();

			// algo
			LayoutAlgo layoutAlgo = createLayoutAlgorithm(setting.getLayoutAlgorithm());
			layoutAlgo.setData(wcvDocument.getWords(), similarity);
			BoundingBoxGenerator bbg = new BoundingBoxGenerator();
			layoutAlgo.setConstraints(bbg);
			layoutAlgo.run();

			// colors
			IColorScheme wordColorScheme = getColorScheme(wcvDocument, similarity, setting);

			// Ask to render into the SVG Graphics2D implementation.
			WordCloudPanel panel = new WordCloudPanel(wcvDocument.getWords(), layoutAlgo, null, wordColorScheme, bbg);
			panel.setSize(1024, 800);
			panel.setShowRectangles(setting.isShowRectangles());
			panel.setOpaque(false);

			String svg = getSvg(panel);

			Date timestamp = Calendar.getInstance().getTime();
			cloud = createCloud(setting, input, text, timestamp, svg, "", (int) panel.getActualWidth(), (int) panel.getActualHeight(), 0, 0, ip);

		}

		//metrics
		//computeMetrics(cloud, wcvDocument.getWords(), similarity, layoutAlgo);

		//export
		//WCExporter.saveCloud(cloud);
		//WCExporter.saveCloudAsSVG(timestamp + ".svg", cloud, setting);
		//WCExporter.saveCloudAsHTML(timestamp + ".html", cloud, setting);

		return cloud;
	}

	private static IColorScheme getColorScheme(WCVDocument wcvDocument, Map<WordPair, Double> similarity, WCSetting setting)
	{
		IColorScheme wordColorScheme = null;
		if (setting.getColorDistribute().equals(WCSetting.COLOR_DISTRIBUTE.KMEANS))
		{
			int K = guessNumberOfClusters(wcvDocument.getWords().size(), setting);

			IClusterAlgo clusterAlgo = new KMeansPlusPlus(K);
			clusterAlgo.run(wcvDocument.getWords(), similarity);

			wordColorScheme = new ClusterColorScheme(clusterAlgo, wcvDocument.getWords(), setting.getColorScheme().toString());
		}
		else if (setting.getColorDistribute().equals(WCSetting.COLOR_DISTRIBUTE.SENTIMENT))
		{
			wordColorScheme = new SentimentColorScheme(setting.getColorScheme().toString());
		}
		else if (setting.getColorDistribute().equals(WCSetting.COLOR_DISTRIBUTE.DYNAMIC))
		{
			wordColorScheme = new DynamicColorScheme(setting.getColorScheme().toString());
		}
		else
		{
			wordColorScheme = new WebColorScheme(setting.getColorScheme().toString(), setting.getColorDistribute().toString(), wcvDocument.getWords().size());
		}
		return wordColorScheme;
	}

	private static String getSvg(WordCloudPanel panel)
	{
		// get svg
		DOMImplementation domImpl = SVGDOMImplementation.getDOMImplementation();

		// Create an instance of org.w3c.dom.Document.
		SVGDocument document = (SVGDocument) domImpl.createDocument(SVGDOMImplementation.SVG_NAMESPACE_URI, "svg", null);
		SVGGraphics2D svgGenerator = new SVGGraphics2D(document);

		// two Clouds
		panel.paintComponent(svgGenerator);
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

	private static WordCloud generateWordCloudFromDynamic(String input, String text1, String text2, WCSetting setting, String ip)
	{
		WordCloud cloud = null;
		String svg1, svg2, text;

		WCVDynamicDocument doc = new WCVDynamicDocument(text1, text2);
		doc.parse();
		doc.weightFilter(setting.getWordCount(), createRanking(setting.getRankingAlgorithm(), doc));

		text = doc.getText();

		SimilarityAlgo similarityAlgo = createSimilarity(setting.getSimilarityAlgorithm());
		similarityAlgo.initialize(doc);
		similarityAlgo.run();
		Map<WordPair, Double> similarity = similarityAlgo.getSimilarity();

		LayoutAlgo layoutAlgo = createLayoutAlgorithm(setting.getLayoutAlgorithm());
		layoutAlgo.setData(doc.getWords(), similarity);
		BoundingBoxGenerator bbg = new BoundingBoxGenerator();
		layoutAlgo.setConstraints(bbg);
		layoutAlgo.run();

		IColorScheme wordColorScheme = getColorScheme(doc.getDoc1(), similarity, setting);
		IColorScheme wordColorScheme2 = getColorScheme(doc.getDoc2(), similarity, setting);

		WordCloudPanel panel1 = new WordCloudPanel(doc.getDoc1().getWords(), layoutAlgo, null, wordColorScheme, bbg);
		panel1.setSize(1024, 800);
		panel1.setShowRectangles(setting.isShowRectangles());
		panel1.setOpaque(false);

		svg1 = getSvg(panel1);

		WordCloudPanel panel2 = new WordCloudPanel(doc.getDoc2().getWords(), layoutAlgo, null, wordColorScheme2, bbg);
		panel2.setSize(1024, 800);
		panel2.setShowRectangles(setting.isShowRectangles());
		panel2.setOpaque(false);

		svg2 = getSvg(panel2);

		Date timestamp = Calendar.getInstance().getTime();

		cloud = createCloud(setting, input, text, timestamp, svg1, svg2, (int) panel1.getActualWidth(), (int) panel1.getActualHeight(), (int) panel2.getActualWidth(), (int) panel2.getActualHeight(),
				ip);

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
		//cloud.setCreatorIP(getThreadLocalRequest().getRemoteAddr());
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
		log.info("running algorithm " + setting.toString());
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
		if (algo.equals(SIMILARITY_ALGORITHM.DICECOEFFI))
			return new DiceCoefficientAlgo();

		throw new RuntimeException("something is wrong");
	}

	private static LayoutAlgo createLayoutAlgorithm(LAYOUT_ALGORITHM algo)
	{
		if (algo.equals(LAYOUT_ALGORITHM.WORDLE))
			return new WordleAlgo();

		if (algo.equals(LAYOUT_ALGORITHM.CPWCV))
			return new ContextPreservingAlgo();

		if (algo.equals(LAYOUT_ALGORITHM.SEAM))
			return new SeamCarvingAlgo();

		if (algo.equals(LAYOUT_ALGORITHM.INFLATE))
			return new InflateAndPushAlgo();

		if (algo.equals(LAYOUT_ALGORITHM.STAR))
			return new StarForestAlgo();

		if (algo.equals(LAYOUT_ALGORITHM.CYCLE))
			return new CycleCoverAlgo();

		if (algo.equals(LAYOUT_ALGORITHM.MDS))
			return new MDSAlgo();

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
		return Math.max((int) Math.sqrt((double) n / 2), 1);
	}

}
