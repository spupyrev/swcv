package edu.cloudy.render;

import org.apache.batik.dom.svg.SVGDOMImplementation;
import org.apache.batik.svggen.SVGGeneratorContext;
import org.apache.batik.svggen.SVGGraphics2D;
import org.apache.batik.transcoder.SVGAbstractTranscoder;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.TIFFTranscoder;
import org.apache.fop.render.ps.EPSTranscoder;
import org.apache.fop.render.ps.PSTranscoder;
import org.apache.fop.svg.PDFTranscoder;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.svg.SVGSVGElement;

import javax.imageio.ImageIO;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.io.Writer;

/**
 * @author spupyrev
 * Oct 16, 2014
 */
public class RenderUtils
{
    public static byte[] createBitmap(WordCloudRenderer renderer, String format)
    {
        boolean supportTransparency = ("png".equalsIgnoreCase(format) || "gif".equalsIgnoreCase(format));
        int imageType = (supportTransparency ? BufferedImage.TYPE_INT_ARGB : BufferedImage.TYPE_INT_RGB);
        BufferedImage img = new BufferedImage((int)renderer.getWidth(), (int)renderer.getHeight(), imageType);
        Graphics2D g2d = img.createGraphics();
        if (!supportTransparency)
        {
            g2d.setBackground(Color.WHITE);
            g2d.clearRect(0, 0, (int)renderer.getWidth(), (int)renderer.getHeight());
        }
        renderer.render(g2d);
        g2d.dispose();

        //resize
        img = img.getSubimage(0, 0, (int)renderer.getActualWidth() + 20, (int)renderer.getActualHeight() + 20);

        ByteArrayOutputStream writer;
        try
        {
            writer = new ByteArrayOutputStream();
            ImageIO.write(img, format, writer);

            writer.close();
            return writer.toByteArray();
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    public static byte[] createSVG(WordCloudRenderer renderer)
    {
        return createSVG(renderer, new SVGTextStyleHandler());
    }

    public static byte[] createSVG(WordCloudRenderer renderer, SVGTextStyleHandler styleHandler)
    {
        // Create an instance of org.w3c.dom.Document.
        DOMImplementation domImpl = SVGDOMImplementation.getDOMImplementation();
        Document document = domImpl.createDocument(SVGDOMImplementation.SVG_NAMESPACE_URI, "svg", null);

        // Configure the SVGGraphics2D
        SVGGeneratorContext ctx = SVGGeneratorContext.createDefault(document);
        ctx.setStyleHandler(styleHandler);
        SVGGraphics2D svgGenerator = new SVGGraphics2D(ctx, false);

        // rendering the cloud
        renderer.render(svgGenerator);

        SVGSVGElement root = (SVGSVGElement)svgGenerator.getRoot();
        styleHandler.postRenderAction(root);

        try
        {
            Writer writer = new StringWriter();
            svgGenerator.stream(root, writer);
            writer.close();

            return writer.toString().getBytes("UTF-8");
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    public static byte[] createVector(WordCloudRenderer renderer, SVGAbstractTranscoder transcoder)
    {
        byte[] svg = createSVG(renderer);

        // Set the transcoding hints
        transcoder.addTranscodingHint(SVGAbstractTranscoder.KEY_WIDTH, new Float(renderer.getActualWidth() + 20));
        transcoder.addTranscodingHint(SVGAbstractTranscoder.KEY_HEIGHT, new Float(renderer.getActualHeight() + 20));

        // Create the transcoder input
        InputStream is = new ByteArrayInputStream(svg);
        TranscoderInput input = new TranscoderInput(is);

        // Create the transcoder output
        ByteArrayOutputStream ostream = new ByteArrayOutputStream();
        TranscoderOutput output = new TranscoderOutput(ostream);

        try
        {
            // Save the image
            transcoder.transcode(input, output);

            // Flush and close the stream
            ostream.flush();
            ostream.close();
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }

        return ostream.toByteArray();
    }

    public static byte[] createCloud(WordCloudRenderer renderer, String format)
    {
        if ("svg".equalsIgnoreCase(format))
            return createSVG(renderer);
        if ("pdf".equalsIgnoreCase(format))
            return createVector(renderer, new PDFTranscoder());
        if ("eps".equalsIgnoreCase(format))
            return createVector(renderer, new EPSTranscoder());
        if ("ps".equalsIgnoreCase(format))
            return createVector(renderer, new PSTranscoder());
        if ("tif".equalsIgnoreCase(format))
            return createVector(renderer, new TIFFTranscoder());
        else if ("png".equalsIgnoreCase(format) || "bmp".equalsIgnoreCase(format) || "gif".equalsIgnoreCase(format))
            return createBitmap(renderer, format);
        else if ("jpeg".equalsIgnoreCase(format) || "jpg".equalsIgnoreCase(format))
            return createBitmap(renderer, "jpeg");

        throw new RuntimeException("unrecognized format '" + format + "'");
    }

}
