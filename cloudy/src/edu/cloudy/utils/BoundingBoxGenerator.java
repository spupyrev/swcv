package edu.cloudy.utils;

import edu.cloudy.nlp.Word;

public class BoundingBoxGenerator
{
    private double weightToAreaFactor;

    public BoundingBoxGenerator(double weightToAreaFactor)
    {
        this.weightToAreaFactor = weightToAreaFactor;
    }

    public SWCRectangle getBoundingBox(Word w, double weight)
    {
        SWCRectangle bb = FontUtils.getBoundingBox(w.word);

        double origWidth = bb.getWidth();
        double origHeight = bb.getHeight();

        //double scaling = Math.sqrt(weight * weightToAreaFactor / (origWidth * origHeight));
        double scaling = weight;
        return new SWCRectangle(0, 0, origWidth * scaling, origHeight * scaling);
    }

    public double getWeightToAreaFactor()
    {
        return weightToAreaFactor;
    }

}
