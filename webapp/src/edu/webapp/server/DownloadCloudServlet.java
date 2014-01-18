package edu.webapp.server;

import edu.webapp.server.db.DBUtils;
import edu.webapp.shared.DBCloudNotFoundException;
import edu.webapp.shared.WordCloud;

import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.PNGTranscoder;
import org.apache.fop.svg.PDFTranscoder;

import javax.servlet.Servlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author spupyrev
 * Jan 18, 2014
 */
public class DownloadCloudServlet extends HttpServlet implements Servlet
{
    private static final long serialVersionUID = 2331432618820037810L;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
    {
        int id = -1;
        WordCloud cloud;

        try
        {
            id = Integer.valueOf(request.getParameter("id"));
            cloud = DBUtils.getCloud(id);
        }
        catch (DBCloudNotFoundException e)
        {
            System.err.println(e);
            return;
        }

        try
        {
            String format = request.getParameter("ft");
            byte[] fileContentByte = null;

            if ("svg".equals(format))
            {
                response.setContentType("image/svg+xml");
                response.setCharacterEncoding("UTF-8");
                response.setHeader("Content-Disposition", "filename=cloud_" + id + ".svg");

                fileContentByte = cloud.getSvg().getBytes();
            }
            else if ("png".equals(format))
            {
                response.setContentType("image/png");
                response.setCharacterEncoding("UTF-8");
                response.setHeader("Content-Disposition", "filename=cloud_" + id + ".png");

                fileContentByte = convertToPNG(cloud);
            }
            else if ("pdf".equals(format))
            {
                response.setContentType("application/pdf");
                response.setCharacterEncoding("UTF-8");
                response.setHeader("Content-Disposition", "filename=cloud_" + id + ".pdf");

                fileContentByte = convertToPDF(cloud);
            }
            else
            {
                System.err.println("wrong cloud format: '" + format + "'");
                return;
            }

            response.getOutputStream().write(fileContentByte);
            response.setStatus(HttpServletResponse.SC_OK);
        }
        catch (Exception e)
        {
            System.err.println(e.getMessage());
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    private byte[] convertToPNG(WordCloud cloud) throws TranscoderException, IOException
    {
        // Create a JPEG transcoder
        PNGTranscoder t = new PNGTranscoder();

        // Set the transcoding hints
        t.addTranscodingHint(PNGTranscoder.KEY_WIDTH, new Float(cloud.getWidth() + 20));
        t.addTranscodingHint(PNGTranscoder.KEY_HEIGHT, new Float(cloud.getHeight() + 20));

        // Create the transcoder input
        InputStream is = new ByteArrayInputStream(cloud.getSvg().getBytes());
        TranscoderInput input = new TranscoderInput(is);

        // Create the transcoder output
        ByteArrayOutputStream ostream = new ByteArrayOutputStream();
        TranscoderOutput output = new TranscoderOutput(ostream);

        // Save the image
        t.transcode(input, output);

        // Flush and close the stream
        ostream.flush();
        ostream.close();
        return ostream.toByteArray();
    }

    private byte[] convertToPDF(WordCloud cloud) throws TranscoderException, IOException
    {
        // Create a JPEG transcoder
        PDFTranscoder t = new PDFTranscoder();

        // Set the transcoding hints
        t.addTranscodingHint(PDFTranscoder.KEY_WIDTH, new Float(cloud.getWidth() + 20));
        t.addTranscodingHint(PDFTranscoder.KEY_HEIGHT, new Float(cloud.getHeight() + 20));

        // Create the transcoder input
        InputStream is = new ByteArrayInputStream(cloud.getSvg().getBytes());
        TranscoderInput input = new TranscoderInput(is);

        // Create the transcoder output
        ByteArrayOutputStream ostream = new ByteArrayOutputStream();
        TranscoderOutput output = new TranscoderOutput(ostream);

        // Save the image
        t.transcode(input, output);

        // Flush and close the stream
        ostream.flush();
        ostream.close();
        return ostream.toByteArray();
    }
}
