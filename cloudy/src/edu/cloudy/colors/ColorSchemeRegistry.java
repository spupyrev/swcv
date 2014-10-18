package edu.cloudy.colors;

import java.util.ArrayList;
import java.util.List;

/**
 * @author spupyrev
 * Oct 11, 2014
 * 
 * TODO: refactor names (make them constants?)
 */
public class ColorSchemeRegistry
{
    private static List<ColorScheme> schemes = new ArrayList<ColorScheme>();

    static
    {
        schemes.add(new MonoColorScheme("BLACK", ColorSchemeConstants.BLACK));
        schemes.add(new MonoColorScheme("GREEN", ColorSchemeConstants.GREEN));
        schemes.add(new MonoColorScheme("ORANGE", ColorSchemeConstants.ORANGE));
        schemes.add(new MonoColorScheme("BLUE", ColorSchemeConstants.BLUE));

        schemes.add(new DynamicColorScheme("REDBLUEBLACK", ColorSchemeConstants.redblueblack));
        schemes.add(new DynamicColorScheme("BLUEREDBLACK", ColorSchemeConstants.blueredblack));

        schemes.add(new SentimentColorScheme("SENTIMENT_OB", ColorSchemeConstants.sentiment));
        schemes.add(new SentimentColorScheme("SENTIMENT_GR", ColorSchemeConstants.sentiment2));

        schemes.add(new RandomColorScheme("RANDOM"));

        schemes.add(new ClusterColorScheme("BEAR_DOWN", 2, ColorSchemeConstants.bear_down));
        schemes.add(new ClusterColorScheme("BLUESEQUENTIAL", -1, ColorSchemeConstants.blue_sequential));
        schemes.add(new ClusterColorScheme("ORANGESEQUENTIAL", -1, ColorSchemeConstants.orange_sequential));
        schemes.add(new ClusterColorScheme("GREENSEQUENTIAL", -1, ColorSchemeConstants.green_sequential));
        schemes.add(new ClusterColorScheme("BREWER_1", -1, ColorSchemeConstants.colorbrewer_1));
        schemes.add(new ClusterColorScheme("BREWER_2", -1, ColorSchemeConstants.colorbrewer_2));
        schemes.add(new ClusterColorScheme("BREWER_3", -1, ColorSchemeConstants.colorbrewer_3));
        schemes.add(new ClusterColorScheme("TRISCHEME_1", 4, ColorSchemeConstants.trischeme_1));
        schemes.add(new ClusterColorScheme("TRISCHEME_2", 4, ColorSchemeConstants.trischeme_2));
        schemes.add(new ClusterColorScheme("TRISCHEME_3", 4, ColorSchemeConstants.trischeme_3));
        schemes.add(new ClusterColorScheme("SIMILAR_1", 4, ColorSchemeConstants.similar_1));
        schemes.add(new ClusterColorScheme("SIMILAR_2", 4, ColorSchemeConstants.similar_2));
        schemes.add(new ClusterColorScheme("SIMILAR_3", 4, ColorSchemeConstants.similar_3));
    }

    public static ColorScheme getByName(String name)
    {
        for (ColorScheme font : schemes)
            if (font.getName().equals(name))
                return font;

        throw new RuntimeException("color scheme with name '" + name + "' not found");
    }
    
    public static ColorScheme getDefault()
    {
        return getByName("BREWER_2");
    }

}
