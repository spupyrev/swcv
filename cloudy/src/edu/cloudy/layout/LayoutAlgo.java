package edu.cloudy.layout;

import edu.cloudy.nlp.Word;
import edu.cloudy.utils.BoundingBoxGenerator;
import edu.cloudy.utils.SWCRectangle;

public interface LayoutAlgo
{
    public void setBoundingBoxGenerator(BoundingBoxGenerator bbGenerator);

    public void setAspectRatio(double aspectRatio);

    public void run();

    public SWCRectangle getWordPosition(Word w);
    
    public SWCRectangle getBoundingBox(Word w);
}
