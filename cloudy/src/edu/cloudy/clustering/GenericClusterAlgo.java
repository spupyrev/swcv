package edu.cloudy.clustering;

import edu.cloudy.layout.WordGraph;
import edu.cloudy.utils.Logger;

/**
 * @author spupyrev
 * Nov 10, 2014
 */
public class GenericClusterAlgo implements IClusterAlgo
{
    private int desiredClusterNumber;

    public GenericClusterAlgo(int desiredClusterNumber)
    {
        this.desiredClusterNumber = desiredClusterNumber;
    }

    @Override
    public ClusterResult run(WordGraph wordGraph)
    {
        if (desiredClusterNumber != -1)
        {
            IClusterAlgo clusterAlgo = new KMeansPlusPlus(desiredClusterNumber);
            return clusterAlgo.run(wordGraph);
        }
        else
        {
            //trying to guess the "best" number
            int n = wordGraph.getWords().size();
            int K = Math.max((int)Math.sqrt((double)n / 2), 1);

            IClusterAlgo clusterAlgo = new KMeansPlusPlus(K);
            ClusterResult clusterResult = clusterAlgo.run(wordGraph);

            for (int i = 1; i <= 5; i++)
            {
                IClusterAlgo algo2 = new KMeansPlusPlus(K + i);
                ClusterResult clusterResult2 = algo2.run(wordGraph);

                if (clusterResult2.quality() > clusterResult.quality())
                {
                    clusterResult = clusterResult2;
                }
                else
                    break;
            }

            Logger.print("cluster quality: " + clusterResult.getModularity());
            Logger.println(" (K=" + K + ";  #clusters=" + clusterResult.getClusterCount() + ")");

            return clusterResult;
        }
    }
}
