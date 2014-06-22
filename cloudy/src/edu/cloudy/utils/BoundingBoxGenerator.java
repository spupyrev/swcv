package edu.cloudy.utils;

import edu.cloudy.nlp.Word;

public class BoundingBoxGenerator
{
    private double scale;

    public BoundingBoxGenerator()
    {
        this.scale = 1.0;
    }

    public BoundingBoxGenerator(double scale)
    {
        this.scale = scale;
    }

    public SWCRectangle getBoundingBox(Word w)
    {
        return getBoundingBox(w, w.weight);
    }

    public SWCRectangle getBoundingBox(Word w, double weight)
    {
        SWCRectangle bb = FontUtils.getBoundingBox(w.word);
        bb.scale(weight * scale);
        return bb;
    }
}
