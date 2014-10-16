package edu.cloudy.render;

import edu.cloudy.colors.ColorScheme;
import edu.cloudy.geom.SWCRectangle;
import edu.cloudy.layout.LayoutResult;
import edu.cloudy.nlp.Word;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

/**
 * @author spupyrev
 * Jun 30, 2014
 * 
 * the word object rendered on the screen
 */
public class UIWord
{
    private String text;
    private SWCRectangle rectangle;
    private Color color;

    public String getText()
    {
        return text;
    }

    public void setText(String text)
    {
        this.text = text;
    }

    public SWCRectangle getRectangle()
    {
        return rectangle;
    }

    public void setRectangle(SWCRectangle rectangle)
    {
        this.rectangle = rectangle;
    }

    public Color getColor()
    {
        return color;
    }

    public void setColor(Color color)
    {
        this.color = color;
    }

    public static List<UIWord> prepareUIWords(List<Word> words, LayoutResult layout, ColorScheme colorScheme)
    {
        List<UIWord> res = new ArrayList<UIWord>();
        for (Word w : words)
        {
            UIWord uiWord = new UIWord();
            uiWord.setText(w.word);
            uiWord.setColor(colorScheme.getColor(w));
            uiWord.setRectangle(layout.getWordPosition(w));

            res.add(uiWord);
        }

        return res;
    }
}
