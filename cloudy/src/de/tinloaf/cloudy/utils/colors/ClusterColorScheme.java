package de.tinloaf.cloudy.utils.colors;

import de.tinloaf.cloudy.clustering.IClusterAlgo;
import de.tinloaf.cloudy.text.Word;

import java.awt.Color;

/**
 * @author spupyrev
 * Nov 28, 2013
 * 
 * TODO: use colorbrewer2!
 */
public class ClusterColorScheme implements IColorScheme {
	private IClusterAlgo clusterAlgo;
	private Color[] seq_select;

	public ClusterColorScheme(IClusterAlgo clusterAlgo) {
		this.clusterAlgo = clusterAlgo;
		seq_select = similar_3;
	}

	@Override
	public Color getColor(Word word) {
		int k = clusterAlgo.getClusterNumber();
		int cl = clusterAlgo.getCluster(word);
		return seq_select[cl % k];
	}

}
