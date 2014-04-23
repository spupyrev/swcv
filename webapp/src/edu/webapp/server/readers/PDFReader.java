package edu.webapp.server.readers;

import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import org.apache.pdfbox.pdfparser.PDFParser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.util.PDFTextStripper;

public class PDFReader implements IDocumentReader
{
    private String text;

    public boolean isConnected(String url)
    {
        if (!url.endsWith(".pdf"))
            return false;

        if (getFile(url))
            return true;

        return false;
    }

    private boolean getFile(String url)
    {
        try
        {
            URL u = new URL(url);
            URLConnection con = u.openConnection();
            InputStream in = con.getInputStream();
            PDFParser p = new PDFParser(in);
            p.parse();
            PDDocument pdoc = new PDDocument(p.getDocument());
            PDFTextStripper pts = new PDFTextStripper();
            text = pts.getText(pdoc);
            pdoc.close();

            return true;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return false;
        }
    }

    public String getText(String url)
    {
        return text;
    }
}
