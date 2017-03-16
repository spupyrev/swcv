package edu.cloudy.layout;

import edu.cloudy.geom.SWCRectangle;
import edu.cloudy.nlp.Word;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.IntStream;

/**
 * @author spupyrev
 * Oct 11, 2014
 */
public class LayoutResult<T>
{
    private Map<T, SWCRectangle> itemPositions;

    public LayoutResult(T[] items, SWCRectangle[] positions)
    {
        itemPositions = new HashMap<T, SWCRectangle>();
        IntStream.range(0, items.length).forEach(i -> itemPositions.put(items[i], positions[i]));
    }

    public SWCRectangle getWordPosition(T w)
    {
        return itemPositions.get(w);
    }
}
