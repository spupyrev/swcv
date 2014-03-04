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
public class RandomColorScheme implements IColorScheme
{
    private Map<Word, Color> colors = new HashMap<Word, Color>();
    private Random rnd = new Random(123);

    private Color[] seq_select;

    public RandomColorScheme()
    {
        seq_select = similar_2;
    }

    public Color getColor(Word w)
    {
        if (!colors.containsKey(w))
        {
            //int randomindex = rnd.nextInt(seq_select.length);
            //Color c = seq_select[randomindex];
            //colors.put(w, c);

            int r = 0;
            int g = rnd.nextInt(178);
            int b = 56 + rnd.nextInt(200);
            colors.put(w, new Color(r, g, b));
        }

        return colors.get(w);
    }
}
