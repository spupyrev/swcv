package edu.webapp.shared.registry;

import edu.webapp.shared.WCAspectRatio;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * @author spupyrev
 * Oct 11, 2014
 */
public class WCAspectRatioRegistry
{
    private static List<WCAspectRatio> ratios = new ArrayList<WCAspectRatio>();

    static
    {
        ratios.add(new WCAspectRatio("1:1", "1:1", 1));
        ratios.add(new WCAspectRatio("4:3", "4:3", 4.0 / 3.0));
        ratios.add(new WCAspectRatio("16:9", "16:9", 16.0 / 9.0));
        ratios.add(new WCAspectRatio("16:10", "16:10", 16.0 / 10.0));
        ratios.add(new WCAspectRatio("21:9", "21:9", 21.0 / 9.0));
    }

    public static List<WCAspectRatio> list()
    {
        return ratios;
    }

    public static WCAspectRatio getDefault()
    {
        return getById("16:9");
    }

    public static WCAspectRatio getById(String id)
    {
        for (WCAspectRatio algo : ratios)
            if (algo.getId().equals(id))
                return algo;

        throw new RuntimeException("ratio with id '" + id + "' not found");
    }

    public static WCAspectRatio getRandom()
    {
        return ratios.get(new Random().nextInt(ratios.size()));
    }
}
