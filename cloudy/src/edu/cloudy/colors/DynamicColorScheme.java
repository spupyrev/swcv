package edu.cloudy.colors;

import edu.cloudy.nlp.SWCDocument;
import edu.cloudy.nlp.SWCDynamicDocument;
import edu.cloudy.nlp.Word;

import java.awt.Color;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * coloring scheme for the ``contrasting 2 clouds'' features
 */
public class DynamicColorScheme extends ColorScheme
{
    private Color[] colorSet;

    public DynamicColorScheme(String name, Color[] colorSet)
    {
        super(name, "");
        this.colorSet = colorSet;
    }

    private Map<Word, Color> colors = new HashMap();

    public void initializeDynamic(SWCDocument document)
    {
        if (document instanceof SWCDynamicDocument)
        {
            SWCDynamicDocument doc = (SWCDynamicDocument)document;
            Set<Word> words1 = new HashSet(doc.getDocument1().getWords());
            Set<Word> words2 = new HashSet(doc.getDocument2().getWords());

            for (Word w : document.getWords())
            {
                if (words1.contains(w) && words2.contains(w))
                    colors.put(w, colorSet[2]);
                else if (words1.contains(w))
                    colors.put(w, colorSet[0]);
                else if (words2.contains(w))
                    colors.put(w, colorSet[1]);
            }
        }
        else
        {
            for (Word w : document.getWords())
            {
                colors.put(w, colorSet[2]);
            }
        }
    }

    @Override
    public Color getColor(Word word)
    {
        return colors.get(word);
    }
}
