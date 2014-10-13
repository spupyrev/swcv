package edu.webapp.shared;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * @author spupyrev
 * Oct 11, 2014
 */
public class WCColorSchemeCollection
{
    public static final String COLOR_SCHEME_TYPE_MONO = "Monochrome";
    public static final String COLOR_SCHEME_TYPE_CLUSTER = "Semantic clusters";
    public static final String COLOR_SCHEME_TYPE_SENTIMENT = "Sentiment (twitter only)";
    public static final String COLOR_SCHEME_TYPE_CONTRAST = "Contrast 2 clouds";

    private static List<WCColorScheme> schemes = new ArrayList<WCColorScheme>();

    static
    {
        schemes.add(new WCColorScheme("BLACK", COLOR_SCHEME_TYPE_MONO, "Black"));
        schemes.add(new WCColorScheme("BLUE", COLOR_SCHEME_TYPE_MONO, "Blue"));
        schemes.add(new WCColorScheme("ORANGE", COLOR_SCHEME_TYPE_MONO, "Orange"));
        schemes.add(new WCColorScheme("GREEN", COLOR_SCHEME_TYPE_MONO, "Green"));

        schemes.add(new WCColorScheme("BEAR_DOWN", COLOR_SCHEME_TYPE_CLUSTER, "BEAR DOWN"));
        schemes.add(new WCColorScheme("BLUESEQUENTIAL", COLOR_SCHEME_TYPE_CLUSTER, "Sequential Blue"));
        schemes.add(new WCColorScheme("ORANGESEQUENTIAL", COLOR_SCHEME_TYPE_CLUSTER, "Sequential Orange"));
        schemes.add(new WCColorScheme("GREENSEQUENTIAL", COLOR_SCHEME_TYPE_CLUSTER, "Sequential Green"));
        schemes.add(new WCColorScheme("BREWER_1", COLOR_SCHEME_TYPE_CLUSTER, "ColorBrewer 1"));
        schemes.add(new WCColorScheme("BREWER_2", COLOR_SCHEME_TYPE_CLUSTER, "ColorBrewer 2"));
        schemes.add(new WCColorScheme("BREWER_3", COLOR_SCHEME_TYPE_CLUSTER, "ColorBrewer 3"));
        schemes.add(new WCColorScheme("TRISCHEME_1", COLOR_SCHEME_TYPE_CLUSTER, "Trinity Scheme 1"));
        schemes.add(new WCColorScheme("TRISCHEME_2", COLOR_SCHEME_TYPE_CLUSTER, "Trinity Scheme 2"));
        schemes.add(new WCColorScheme("TRISCHEME_3", COLOR_SCHEME_TYPE_CLUSTER, "Trinity Scheme 3"));
        schemes.add(new WCColorScheme("SIMILAR_1", COLOR_SCHEME_TYPE_CLUSTER, "Similar Scheme 1"));
        schemes.add(new WCColorScheme("SIMILAR_2", COLOR_SCHEME_TYPE_CLUSTER, "Similar Scheme 2"));
        schemes.add(new WCColorScheme("SIMILAR_3", COLOR_SCHEME_TYPE_CLUSTER, "Similar Scheme 3"));

        schemes.add(new WCColorScheme("SENTIMENT_OB", COLOR_SCHEME_TYPE_SENTIMENT, "Sentiment ORANGE-BLUE"));
        schemes.add(new WCColorScheme("SENTIMENT_GR", COLOR_SCHEME_TYPE_SENTIMENT, "Sentiment GREEN-RED"));

        schemes.add(new WCColorScheme("REDBLUEBLACK", COLOR_SCHEME_TYPE_CONTRAST, "RedBlueBlack"));
        schemes.add(new WCColorScheme("BLUEREDBLACK", COLOR_SCHEME_TYPE_CONTRAST, "BlueRedBlack"));
    }

    public static List<WCColorScheme> list()
    {
        return schemes;
    }

    public static WCColorScheme getDefault()
    {
        return getByName("BREWER_2");
    }

    public static WCColorScheme getByName(String name)
    {
        for (WCColorScheme font : schemes)
            if (font.getName().equals(name))
                return font;

        throw new RuntimeException("color scheme with name '" + name + "' not found");
    }

    public static WCColorScheme getRandom()
    {
        return schemes.get(new Random().nextInt(schemes.size()));
    }
}
