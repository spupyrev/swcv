package edu.cloudy.colors;

import edu.cloudy.clustering.IClusterAlgo;
import edu.cloudy.nlp.Word;

import java.awt.Color;
import java.util.List;

/**
 * @author spupyrev
 * Nov 28, 2013
 */
public class ClusterColorScheme implements IColorScheme
{
	private IClusterAlgo clusterAlgo;
	private List<Word> words;

	private Color[] seq;
	private int[] clusterIndex;

	public ClusterColorScheme(IClusterAlgo clusterAlgo, List<Word> words, String colorScheme)
	{
		this.clusterAlgo = clusterAlgo;
		this.words = words;
		setColorScheme(colorScheme);
		sortClusters();
	}

	public ClusterColorScheme(IClusterAlgo clusterAlgo, List<Word> words)
	{
		this.clusterAlgo = clusterAlgo;
		this.words = words;
		seq = colorbrewer_2;
		sortClusters();
	}

	@Override
	public Color getColor(Word word)
	{
		int c = clusterAlgo.getCluster(word);
		int res = clusterIndex[c] % seq.length;
		return seq[res];
	}

	private void sortClusters()
	{
		int K = clusterAlgo.getClusterNumber();
		int[] cnt = new int[K];
		for (Word w : words)
			cnt[clusterAlgo.getCluster(w)]++;

		clusterIndex = new int[K];
		for (int i = 0; i < K; i++)
			clusterIndex[i] = i;

		for (int i = 0; i < K; i++)
			for (int j = i + 1; j < K; j++)
				if (cnt[clusterIndex[i]] < cnt[clusterIndex[j]])
				{
					int tmp = clusterIndex[i];
					clusterIndex[i] = clusterIndex[j];
					clusterIndex[j] = tmp;
				}

		int[] clusterIndexRev = new int[K];
		for (int i = 0; i < K; i++)
			clusterIndexRev[clusterIndex[i]] = i;

		clusterIndex = clusterIndexRev;
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
		else if (colorScheme.equals("SENTIMENT"))
		{
			seq = sentiment;
		}
		else if (colorScheme.equals("SENTIMENT2"))
		{
			seq = sentiment2;
		}
		else if (colorScheme.equals("REDBLUEBLACK"))
		{
			seq = redblueblack;
		}
		else if (colorScheme.equals("BLUEREDBLACK"))
		{
			seq = blueredblack;
		}
		else if (colorScheme.equals("ORANGESEQUENTIAL"))
		{
			seq = orange_sequential;
		}
		else if (colorScheme.equals("BLUESEQUENTIAL"))
		{
			seq = blue_sequential;
		}
		else if (colorScheme.equals("GREENSEQUENTIAL"))
		{
			seq = green_sequential;
		}
	}

}
