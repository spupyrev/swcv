package edu.webapp.server.readers;

import java.util.List;

public interface ISentimentReader
{
	/**
	 * @return Chunks of Strings.
	 */
	public List<String> getStrChunks();
}
