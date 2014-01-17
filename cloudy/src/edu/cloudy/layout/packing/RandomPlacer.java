package edu.cloudy.layout.packing;

import edu.cloudy.nlp.Word;
import edu.cloudy.utils.BoundingBoxGenerator;
import edu.cloudy.utils.SWCRectangle;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class RandomPlacer implements WordPlacer
{
    private static Random rnd = new Random(123);
    private static double SCALE = 5000;

    private Map<Word, SWCRectangle> wordRectangles = new HashMap<Word, SWCRectangle>();
    private BoundingBoxGenerator bbGenerator;
    private double weightToAreaFactor;
    private boolean avoidOverlaps;

    public RandomPlacer(double weightToAreaFactor, BoundingBoxGenerator bbGenerator, boolean avoidOverlaps)
    {
        this.weightToAreaFactor = weightToAreaFactor;
        this.bbGenerator = bbGenerator;
        this.avoidOverlaps = avoidOverlaps;
    }

    @Override
    public SWCRectangle getRectangleForWord(Word w)
    {
        if (!wordRectangles.containsKey(w))
        {
            SWCRectangle rect = randomRectangle(w);
            if (avoidOverlaps && overlap(rect))
            {
                //make several attempts...
                for (int i = 0; i < 50; i++)
                {
                    rect = randomRectangle(w);
                    if (!overlap(rect))
                        break;
                }

                if (overlap(rect))
                    System.out.println("can't find non-overlapping position");
            }

            wordRectangles.put(w, rect);
        }

        return wordRectangles.get(w);
    }

    private SWCRectangle randomRectangle(Word w)
    {
        return randomRectangle(w, weightToAreaFactor, bbGenerator);
    }

    public static SWCRectangle randomRectangle(Word w, double weightToAreaFactor, BoundingBoxGenerator bbGenerator)
    {
        double x = rnd.nextDouble() * SCALE;
        double y = rnd.nextDouble() * SCALE;
        SWCRectangle bb = bbGenerator.getBoundingBox(w, w.weight * weightToAreaFactor);

        return new SWCRectangle(x, y, bb.getWidth(), bb.getHeight());
    }

    private boolean overlap(SWCRectangle rect)
    {
        for (SWCRectangle o : wordRectangles.values())
            if (o.intersects(rect))
                return true;
        return false;
    }

    @Override
    public boolean contains(Word w)
    {
        return false;
    }
}
