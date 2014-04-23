package edu.webapp.server;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

import java.io.StringWriter;
import java.io.Writer;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
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
import edu.cloudy.nlp.WCVDocument;
import edu.cloudy.nlp.WCVDocument4Sentiment;
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
import edu.webapp.server.readers.IDocumentReader;
import edu.webapp.server.readers.ISentimentReader;
import edu.webapp.server.utils.RandomTwitterTrendExtractor;
import edu.webapp.server.utils.RandomWikiUrlExtractor;
import edu.webapp.server.utils.RandomYoutubeUrlExtractor;
import edu.webapp.shared.WCSetting;
import edu.webapp.shared.WordCloud;
import edu.webapp.shared.WCSetting.COLOR_SCHEME;
import edu.webapp.shared.WCSetting.LAYOUT_ALGORITHM;
import edu.webapp.shared.WCSetting.RANKING_ALGORITHM;
import edu.webapp.shared.WCSetting.SIMILARITY_ALGORITHM;

public class WordCloudGenerator
{
	private static final Logger log = Logger.getLogger(WordCloudGenerator.class.getName());

	public static WordCloud buildWordCloud(String input, WCSetting setting, String ip) throws IllegalArgumentException
	{
		logging(input, setting);
		FontUtils.initialize(new SVGFontProvider(setting.getFont().toString()));

		DocumentExtractor extractor = new DocumentExtractor(input);
		IDocumentReader reader = extractor.getText();
		WCVDocument wcvDocument;
		String text = reader.getText(input);
		
		if (reader instanceof ISentimentReader)
			wcvDocument = new WCVDocument4Sentiment(((ISentimentReader) reader).getStrChunks());
		else
			wcvDocument = new WCVDocument(text);
		
		// parse text
		wcvDocument.parse();

		// ranking
		wcvDocument.weightFilter(setting.getWordCount(), createRanking(setting.getRankingAlgorithm(), wcvDocument));

		// similarity
		SimilarityAlgo similarityAlgo = createSimilarity(setting.getSimilarityAlgorithm());
		similarityAlgo.initialize(wcvDocument);
		similarityAlgo.run();
		Map<WordPair, Double> similarity = similarityAlgo.getSimilarity();

		// algo
		LayoutAlgo layoutAlgo = createLayoutAlgorithm(setting.getLayoutAlgorithm());
		layoutAlgo.setData(wcvDocument.getWords(), similarity);
		layoutAlgo.setConstraints(new BoundingBoxGenerator(25000.0));
		layoutAlgo.run();

		// colors
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
			wordColorScheme = new SentimentColorScheme();
		}
		else
		{
			wordColorScheme = new WebColorScheme(setting.getColorScheme().toString(), setting.getColorDistribute().toString(), wcvDocument.getWords().size());
		}

		// get svg
		DOMImplementation domImpl = SVGDOMImplementation.getDOMImplementation();

		// Create an instance of org.w3c.dom.Document.
		SVGDocument document = (SVGDocument) domImpl.createDocument(SVGDOMImplementation.SVG_NAMESPACE_URI, "svg", null);
		SVGGraphics2D svgGenerator = new SVGGraphics2D(document);

		// Ask to render into the SVG Graphics2D implementation.
		WordCloudPanel panel = new WordCloudPanel(wcvDocument.getWords(), layoutAlgo, null, wordColorScheme);
		panel.setSize(1024, 800);
		panel.setShowRectangles(setting.isShowRectangles());
		panel.setOpaque(false);
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

		Date timestamp = Calendar.getInstance().getTime();
		WordCloud cloud = createCloud(setting, input, text, timestamp, writer.toString(), (int) panel.getActualWidth(), (int) panel.getActualHeight(), ip);

		//metrics
		//computeMetrics(cloud, wcvDocument.getWords(), similarity, layoutAlgo);

		//export
		WCExporter.saveCloud(cloud);
		//WCExporter.saveCloudAsSVG(timestamp + ".svg", cloud, setting);
		//WCExporter.saveCloudAsHTML(timestamp + ".html", cloud, setting);

		return cloud;
	}

	private static WordCloud createCloud(WCSetting setting, String input, String text, Date timestamp, String svg, int width, int height, String ip)
	{
		WordCloud cloud = new WordCloud();
		cloud.setInputText(input);
		cloud.setCreationDateAsDate(timestamp);
		cloud.setSettings(setting);
		cloud.setSvg(svg);
		cloud.setWidth(width);
		cloud.setHeight(height);

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
