package edu.cloudy.colors;

import edu.cloudy.clustering.IClusterAlgo;
import edu.cloudy.nlp.Word;

import java.awt.Color;

/**
 * @author spupyrev
 * Nov 28, 2013
 * 
 * TODO: use colorbrewer2!
 */
public class ClusterColorScheme implements IColorScheme
{
    private IClusterAlgo clusterAlgo;
    private Color[] seq;

    public ClusterColorScheme(IClusterAlgo clusterAlgo, String colorScheme)
    {
        this.clusterAlgo = clusterAlgo;
        setColorScheme(colorScheme);
    }

    public ClusterColorScheme(IClusterAlgo clusterAlgo)
    {
        this.clusterAlgo = clusterAlgo;
        seq = colorbrewer_2;
    }

    @Override
    public Color getColor(Word word)
    {
        int k = clusterAlgo.getClusterNumber();
        int cl = clusterAlgo.getCluster(word);
        //int res = (int)((double)cl * (double)seq.length / k);
        int res = cl % seq.length;
        return seq[res];
    }

    private void setColorScheme(String colorScheme)
    {
        if (colorScheme.equals("GREEN"))
        {
            seq = new Color[] { GREEN };
        }
        else if (colorScheme.equals("BLUE"))
        {
            seq = new Color[] { BLUE };
        }
        else if (colorScheme.equals("ORANGE"))
        {
            seq = new Color[] { ORANGE };
        }
        else if (colorScheme.equals("BREWER_1"))
        {
            seq = colorbrewer_1;
        }
        else if (colorScheme.equals("BREWER_2"))
        {
            seq = colorbrewer_2;
        }
        else if (colorScheme.equals("BREWER_3"))
        {
            seq = colorbrewer_3;
        }
        else if (colorScheme.equals("TRISCHEME_1"))
        {
            seq = trischeme_1;
        }
        else if (colorScheme.equals("TRISCHEME_2"))
        {
            seq = trischeme_2;
        }
        else if (colorScheme.equals("TRISCHEME_3"))
        {
            seq = trischeme_3;
        }
        else if (colorScheme.equals("SIMILAR_1"))
        {
            seq = similar_1;
        }
        else if (colorScheme.equals("SIMILAR_2"))
        {
            seq = similar_2;
        }
        else if (colorScheme.equals("SIMILAR_3"))
        {
            seq = similar_3;
        }
        else if (colorScheme.equals("BEAR_DOWN"))
        {
            seq = bear_down;
        }
    }

}
