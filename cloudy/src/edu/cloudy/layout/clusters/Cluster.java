package edu.cloudy.layout.clusters;

import edu.cloudy.geom.SWCPoint;
import edu.cloudy.geom.SWCRectangle;
import edu.cloudy.nlp.Word;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Cluster
{
    public Map<Word, SWCRectangle> wordPositions = new HashMap<Word, SWCRectangle>();
    public SWCPoint center;

    public SWCRectangle getBoundingBox()
    {
        SWCRectangle bb = new SWCRectangle();
        for (Word w : wordPositions.keySet())
        {
            SWCRectangle r = actualWordPosition(w);
            bb.add(r);
        }

        return bb;
    }

    public String toString()
    {
        StringBuffer sb = new StringBuffer();
        for (Word w : wordPositions.keySet())
            sb.append(" " + w.word);
        return sb.toString();
    }

    public boolean overlap(List<Cluster> list)
    {
        for (Cluster c : list)
        {
            if (c.equals(this))
                continue;

            if (overlap(c))
                return true;
        }

        return false;
    }

    public boolean overlap(Cluster other)
    {
        for (Word w1 : wordPositions.keySet())
            for (Word w2 : other.wordPositions.keySet())
            {
                SWCRectangle rect1 = actualWordPosition(w1);
                SWCRectangle rect2 = other.actualWordPosition(w2);

                if (rect1.intersects(rect2, 1.0))
                    return true;
            }

        return false;
    }

    public SWCRectangle actualWordPosition(Word word)
    {
        SWCRectangle r1 = wordPositions.get(word);
        return new SWCRectangle(r1.getX() + center.x(), r1.getY() + center.y(), r1.getWidth(), r1.getHeight());
    }

}