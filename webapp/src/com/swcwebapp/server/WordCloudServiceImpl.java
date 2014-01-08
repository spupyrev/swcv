package com.swcwebapp.server;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.swcwebapp.client.WordCloudService;
import com.swcwebapp.server.readers.DocumentExtractor;
import com.swcwebapp.shared.WCSetting;
import com.swcwebapp.shared.WCSetting.LAYOUT_ALGORITHM;
import com.swcwebapp.shared.WCSetting.RANKING_ALGORITHM;
import com.swcwebapp.shared.WCSetting.SIMILARITY_ALGORITHM;
import com.swcwebapp.shared.WordCloud;

import de.tinloaf.cloudy.algos.ContextPreservingAlgo;
import de.tinloaf.cloudy.algos.CycleCoverAlgo;
import de.tinloaf.cloudy.algos.InflateAndPushAlgo;
import de.tinloaf.cloudy.algos.LayoutAlgo;
import de.tinloaf.cloudy.algos.SeamCarvingAlgo;
import de.tinloaf.cloudy.algos.StarForestAlgo;
import de.tinloaf.cloudy.algos.WordleAlgo;
import de.tinloaf.cloudy.metrics.AdjacenciesMetric;
import de.tinloaf.cloudy.metrics.AspectRatioMetric;
import de.tinloaf.cloudy.metrics.DistortionMetric;
import de.tinloaf.cloudy.metrics.SpaceMetric;
import de.tinloaf.cloudy.metrics.TotalWeightMetric;
import de.tinloaf.cloudy.metrics.UniformAreaMetric;
import de.tinloaf.cloudy.similarity.*;
import de.tinloaf.cloudy.text.WCVDocument;
import de.tinloaf.cloudy.text.Word;
import de.tinloaf.cloudy.ui.WordCloudPanel;
import de.tinloaf.cloudy.utils.BoundingBoxGenerator;
import de.tinloaf.cloudy.utils.FontUtils;
import de.tinloaf.cloudy.utils.WordPair;
import de.tinloaf.cloudy.utils.colors.IColorScheme;
import de.tinloaf.cloudy.utils.colors.WebColorScheme;

import org.apache.batik.dom.svg.SVGDOMImplementation;
import org.apache.batik.svggen.SVGGraphics2D;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.svg.SVGDocument;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * The server side implementation of the RPC service.
 */
@SuppressWarnings("serial")
public class WordCloudServiceImpl extends RemoteServiceServlet implements WordCloudService
{
    private static final Logger log = Logger.getLogger(WordCloudServiceImpl.class.getName());

    public WordCloud buildWordCloud(String input, WCSetting setting) throws IllegalArgumentException
    {
        logging(input, setting);
        FontUtils.initialize(new SVGFontProvider(setting.getFont().toString()));

        DocumentExtractor extractor = new DocumentExtractor(input);
        String text = extractor.getText();

        // parse text
        WCVDocument wcvDocument = new WCVDocument(text);
        wcvDocument.parse();

        //ranking
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

        // get svg
        DOMImplementation domImpl = SVGDOMImplementation.getDOMImplementation();

        // Create an instance of org.w3c.dom.Document.
        SVGDocument document = (SVGDocument)domImpl.createDocument(SVGDOMImplementation.SVG_NAMESPACE_URI, "svg", null);
        SVGGraphics2D svgGenerator = new SVGGraphics2D(document);

        // Set colors
        IColorScheme wordColorScheme = new WebColorScheme(setting.getColorScheme().toString(), setting.getColorDistribute().toString(), wcvDocument.getWords().size());

        // Ask to render into the SVG Graphics2D implementation.
        WordCloudPanel panel = new WordCloudPanel(wcvDocument.getWords(), layoutAlgo, null, wordColorScheme);
        panel.setSize(1024, 600);
        panel.setShowRectangles(setting.isShowRectangles());
        panel.paintComponent(svgGenerator);

        Writer out;
        try
        {
            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer transformer = tf.newTransformer();
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            out = new StringWriter();
            transformer.transform(new DOMSource(document), new StreamResult(out));
            out.close();

            out = new StringWriter();
            // out = new OutputStreamWriter(System.out, "UTF-8");
            svgGenerator.stream(out, true);
            out.close();
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }

        String timeStamp = saveSVG(setting, panel, out);

        WordCloud wc = new WordCloud();
        wc.setSettings(setting.toString() + "Text:\n" + text);
        wc.setFileName(timeStamp + ".svg");
        wc.setSvg(out.toString());
        wc.setWidth((int)panel.getActualWidth() + 20);
        wc.setHeight((int)panel.getActualHeight() + 20);
        //computeMetrics(wc, wcvDocument.getWords(), similarity, layoutAlgo);
        return wc;
    }

    private String saveSVG(WCSetting setting, WordCloudPanel panel, Writer out)
    {
        /**
         * Set path to .../wordcloud/svgs/
         */
        String path = getAbsoluteFileName("");
        path = getParent(path);
        path = getParent(path);
        path = getParent(path);
        path = path + File.separator + "svgs" + File.separator;
        String svg = out.toString();

        /**
         * create a timestamp to name the svg file
         */
        String timeStamp = new SimpleDateFormat("yyyyMMddHHmmssS").format(Calendar.getInstance().getTime());

        /**
         * write svg file
         */
        FileWriter fw;
        try
        {
            fw = new FileWriter(path + timeStamp + ".svg");
            fw.write(svg);
            fw.close();
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }

        /**
        * write a html file for each graph generated
        */
        path = getParent(path);
        path = getParent(path);
        path = path + File.separator + "htmlforclouds" + File.separator;

        FileWriter fWriter = null;
        BufferedWriter writer = null;
        try
        {
            fWriter = new FileWriter(path + timeStamp + ".html");
            writer = new BufferedWriter(fWriter);
            path = getParent(path);
            path = getParent(path);
            path = path + File.separator + "svgs" + File.separator;

            writer.write("<div style=\"width: 1025px; margin: 10px auto\">");
            writer.write("<object data=\"../svgs/" + timeStamp + ".svg\" type=\"image/svg+xml\" height=" + ((int)panel.getActualHeight() + 20)
                    + "px; width=" + ((int)panel.getActualWidth() + 20) + "px></object>");
            writer.write("<div style=\"width=100px\">" + setting.toString() + "</div>");
            writer.write("<div style=\"float: right\">Share The Cloud!<a href=\"https://www.facebook.com/sharer/sharer.php?u=http%3A%2F%2Fwordcloud.cs.arizona.edu%2Fhtmlforclouds%2F"
                    + timeStamp
                    + ".html\" target=\"_blank\"><img src=\"../imgs/facebook.ico\" height=\"24px\" width=\"24px\" Title=\"Share on Facebook\" /></a>");
            writer.write("<a href=\"https://twitter.com/share?url=http%3A%2F%2Fwordcloud.cs.arizona.edu%2Fhtmlforclouds\""
                    + timeStamp
                    + ".html\" target=\"_blank\"> <img src=\"../imgs/twitter.ico\" height=\"24px\" width=\"24px\" Title=\"Tweet on Twitter\" /></a></div>");
            writer.write("<a href=\"http://wordcloud.cs.arizona.edu\">Generate a New Cloud Now!</a>");
            writer.write("</div>");

            writer.newLine(); //this is not actually needed for html files - can make your code more readable though 
            writer.close(); //make sure you close the writer object 
        }
        catch (Exception e)
        {
            //catch any exceptions here
            e.printStackTrace();
        }
        return timeStamp;
    }

    private RankingAlgo createRanking(RANKING_ALGORITHM algo, WCVDocument document)
    {
        if (algo.equals(RANKING_ALGORITHM.TF))
            return new TFRankingAlgo();

        if (algo.equals(RANKING_ALGORITHM.TF_IDF))
            return new TFIDFRankingAlgo();

        if (algo.equals(RANKING_ALGORITHM.LEX))
            return new LexRankingAlgo();

        throw new RuntimeException("something is wrong");
    }

    private void logging(String text, WCSetting setting)
    {
        log.info("running algorithm " + setting.toString());
        log.info("text: " + text);
    }

    private void computeMetrics(WordCloud wc, List<Word> words, Map<WordPair, Double> similarity, LayoutAlgo algo)
    {
        wc.setWordCount(words.size());
        double totalWeight = new TotalWeightMetric().getValue(words, similarity, algo);
        double adj = new AdjacenciesMetric().getValue(words, similarity, algo);
        wc.setAdjacencies(adj / totalWeight);
        wc.setDistortion(new DistortionMetric().getValue(words, similarity, algo));
        wc.setSpace(new SpaceMetric(false).getValue(words, similarity, algo));
        wc.setUniformity(new UniformAreaMetric().getValue(words, similarity, algo));
        wc.setAspectRatio(new AspectRatioMetric().getValue(words, similarity, algo));
    }

    private SimilarityAlgo createSimilarity(SIMILARITY_ALGORITHM algo)
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
            return new DicecoefficientAlgo();

        throw new RuntimeException("something is wrong");
    }

    private LayoutAlgo createLayoutAlgorithm(LAYOUT_ALGORITHM algo)
    {
        if (algo.equals(LAYOUT_ALGORITHM.WORDLE))
            return new WordleAlgo();

        if (algo.equals(LAYOUT_ALGORITHM.CPDWCV))
            return new ContextPreservingAlgo();

        if (algo.equals(LAYOUT_ALGORITHM.SEAM))
            return new SeamCarvingAlgo();

        if (algo.equals(LAYOUT_ALGORITHM.INFLATE))
            return new InflateAndPushAlgo();

        if (algo.equals(LAYOUT_ALGORITHM.STAR))
            return new StarForestAlgo();

        if (algo.equals(LAYOUT_ALGORITHM.CYCLE))
            return new CycleCoverAlgo();

        throw new RuntimeException("something is wrong");
    }

    private String getAbsoluteFileName(String name)
    {
        return Thread.currentThread().getContextClassLoader().getResource(name).getPath();
    }

    private static String getParent(String path)
    {
        return path.substring(0, path.lastIndexOf('/'));
    }

    /**
     * if User get the SVG.
     * Save the cloud setting to log
     */
    public String saveGetSvgLog(WordCloud cloud)
    {
        /**
         * Set path to .../wordcloud/saved/
         */
        String path = getAbsoluteFileName("");
        path = getParent(path);
        path = getParent(path);
        path = getParent(path);
        path = path + File.separator + "saved" + File.separator;

        /**
         * write svg file
         */
        FileWriter fw;
        try
        {
            fw = new FileWriter(path + "saved.log", true);
            fw.write("User saved " + cloud.getFileName() + "\n");
            fw.write(cloud.getSettings());
            fw.write("\n\n");
            fw.close();
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
        return cloud.getFileName();
    }
}
