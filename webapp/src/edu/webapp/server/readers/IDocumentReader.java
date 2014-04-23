package edu.webapp.server.readers;

import java.util.List;

/**
 * @author spupyrev
 * Nov 23, 2013
 */
public interface IDocumentReader
{
    boolean isConnected(String input);
    String getText(String input);
}
