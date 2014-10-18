package edu.cloudy.layout;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

/**
 * @author spupyrev
 * Oct 17, 2014
 */
public class LayoutAlgorithmRegistry
{
    private static Map<String, Class<? extends LayoutAlgo>> algorithms = new HashMap<String, Class<? extends LayoutAlgo>>();

    static
    {
        algorithms.put("rnd", WordleAlgo.class);
        algorithms.put("tca", TagCloudAlphabeticalAlgo.class);
        algorithms.put("tcr", TagCloudRankAlgo.class);
        algorithms.put("cp", ContextPreservingAlgo.class);
        algorithms.put("sc", SeamCarvingAlgo.class);
        algorithms.put("ip", InflateAndPushAlgo.class);
        algorithms.put("sf", StarForestAlgo.class);
        algorithms.put("cc", CycleCoverAlgo.class);
        algorithms.put("mds", MDSWithFDPackingAlgo.class);
    }

    public static LayoutAlgo getById(String id)
    {
        try
        {
            Class<? extends LayoutAlgo> cls = algorithms.get(id);
            Constructor<? extends LayoutAlgo> ctor = cls.getConstructor();
            return ctor.newInstance();
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }
}
