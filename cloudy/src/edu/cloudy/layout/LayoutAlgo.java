package edu.cloudy.layout;

import edu.cloudy.geom.BoundingBoxGenerator;
import edu.cloudy.geom.SWCRectangle;
import edu.cloudy.nlp.Word;

public interface LayoutAlgo
{
    public void setBoundingBoxGenerator(BoundingBoxGenerator bbGenerator);

    public void setAspectRatio(double aspectRatio);

    public void run();

    public SWCRectangle getWordPosition(Word w);
    
    public SWCRectangle getBoundingBox(Word w);
}
