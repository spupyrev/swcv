package com.swcwebapp.server.readers;

import java.util.ArrayList;
import java.util.List;

/**
 * @author spupyrev
 * Nov 23, 2013
 */
public class DocumentExtractor
{
    private static List<IDocumentReader> readers;

    static
    {
        readers = new ArrayList<IDocumentReader>();
        readers.add(new DotReader());
        readers.add(new PDFReader());
        readers.add(new YouTubeReader());
        readers.add(new HtmlReader());
        readers.add(new DefaultReader());
    }

    private String input;

    public DocumentExtractor(String input)
    {
        this.input = input;
    }

    public String getText()
    {
        for (IDocumentReader reader : readers)
        {
            if (reader.isConnected(input))
                return reader.getText(input);
        }

        throw new RuntimeException("none of the readers is available");
    }
}
