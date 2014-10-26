package edu.webapp.server.readers;

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
		readers.add(new DynamicReader());
		readers.add(new TwitterReader());
        readers.add(new GoogleReader());
        readers.add(new RedditReader());
		readers.add(new DotReader());
		readers.add(new PDFReader());
        readers.add(new YouTubeReader());
        readers.add(new WikipediaReader());
		readers.add(new HtmlReader());
		readers.add(new DefaultReader());
	}

	private String input;

	public DocumentExtractor(String input)
	{
		this.input = input;
	}

	public IDocumentReader getReader()
	{
		for (IDocumentReader reader : readers)
		{
			if (reader.isConnected(input))
			{
				return reader;
			}
		}

		throw new RuntimeException("none of the readers is available");
	}

}
