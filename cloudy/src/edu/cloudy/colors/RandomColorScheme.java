package edu.cloudy.colors;

import edu.cloudy.nlp.Word;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * @author spupyrev
 * Nov 28, 2013
 */
public class RandomColorScheme extends ColorScheme
{
    private Map<Word, Color> colors = new HashMap<Word, Color>();
    private Random rnd = new Random(123);

    public RandomColorScheme(String name, String cmdIndex)
    {
        super(name, cmdIndex);
    }

    public Color getColor(Word w)
    {
        if (!colors.containsKey(w))
        {
            int r = 0;
            int g = rnd.nextInt(178);
            int b = 56 + rnd.nextInt(200);
            colors.put(w, new Color(r, g, b));
        }

        return colors.get(w);
    }
}
