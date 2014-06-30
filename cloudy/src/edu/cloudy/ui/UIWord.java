package edu.cloudy.ui;

import edu.cloudy.utils.SWCRectangle;

import java.awt.Color;

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

}
