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
    private static List<WCColorScheme> schemes = new ArrayList<WCColorScheme>();

    static
    {
        schemes.add(new WCColorScheme("BEAR_DOWN", "BEAR DOWN"));
        schemes.add(new WCColorScheme("BLACK", "Black"));
        schemes.add(new WCColorScheme("BLUE", "Blue"));
        schemes.add(new WCColorScheme("BLUESEQUENTIAL", "Sequential Blue"));
        schemes.add(new WCColorScheme("ORANGE", "Orange"));
        schemes.add(new WCColorScheme("ORANGESEQUENTIAL", "Sequential Orange"));
        schemes.add(new WCColorScheme("GREEN", "Green"));
        schemes.add(new WCColorScheme("GREENSEQUENTIAL", "Sequential Green"));
        schemes.add(new WCColorScheme("BREWER_1", "ColorBrewer 1"));
        schemes.add(new WCColorScheme("BREWER_2", "ColorBrewer 2"));
        schemes.add(new WCColorScheme("BREWER_3", "ColorBrewer 3"));
        schemes.add(new WCColorScheme("TRISCHEME_1", "Trinity Scheme 1"));
        schemes.add(new WCColorScheme("TRISCHEME_2", "Trinity Scheme 2"));
        schemes.add(new WCColorScheme("TRISCHEME_3", "Trinity Scheme 3"));
        schemes.add(new WCColorScheme("SIMILAR_1", "Similar Scheme 1"));
        schemes.add(new WCColorScheme("SIMILAR_2", "Similar Scheme 2"));
        schemes.add(new WCColorScheme("SIMILAR_3", "Similar Scheme 3"));
        schemes.add(new WCColorScheme("SENTIMENT_OB", "Sentiment ORANGE-BLUE"));
        schemes.add(new WCColorScheme("SENTIMENT_GR", "Sentiment GREEN-RED"));
        schemes.add(new WCColorScheme("REDBLUEBLACK", "RedBlueBlack"));
        schemes.add(new WCColorScheme("BLUEREDBLACK", "BlueRedBlack"));
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
