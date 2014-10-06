package edu.webapp.server.readers;

public class DynamicReader implements IDocumentReader
{

    String text1;
    String text2;
    DocumentExtractor extractor;

    public boolean isConnected(String input)
    {
        String[] sources;
        if (input.startsWith("dynamic::"))
        {
            sources = input.split("\\s*dynamic\\:\\:\\s*");
            if (sources.length != 3)// empty first, source1 second, source2 third
                return false;
            extractor = new DocumentExtractor(sources[1]);
            text1 = extractor.getReader().getText(sources[1]);
            extractor = new DocumentExtractor(sources[2]);
            text2 = extractor.getReader().getText(sources[2]);
            return true;
        }
        return false;
    }

    public String getText(String input)
    {
        return text1 + text2;
    }

    public String getText1()
    {
        return text1;
    }

    public String getText2()
    {
        return text2;
    }
}
