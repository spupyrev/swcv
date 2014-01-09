package de.tinloaf.cloudy.ui;

import de.tinloaf.cloudy.algos.LayoutAlgo;
import de.tinloaf.cloudy.clustering.IClusterAlgo;
import de.tinloaf.cloudy.text.Word;
import de.tinloaf.cloudy.utils.WordPair;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;

import java.awt.BorderLayout;
import java.util.List;
import java.util.Map;

public class WordCloudFrame extends JFrame {
	private static final long serialVersionUID = 6602115306287717309L;

	public WordCloudFrame(List<Word> words, Map<WordPair, Double> similarity, LayoutAlgo algo, IClusterAlgo clusterAlgo) {
		JPopupMenu.setDefaultLightWeightPopupEnabled(false);

		initPanel(words, similarity, algo, clusterAlgo);
		setTitle("WordCloud");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setExtendedState(JFrame.MAXIMIZED_BOTH);
		setVisible(true);

	}

	public WordCloudFrame(JPanel panel) {
		JPopupMenu.setDefaultLightWeightPopupEnabled(false);

		add(panel);
		setJMenuBar(new WordCloudMenuBar(panel));

		setTitle("WordCloud");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setExtendedState(JFrame.MAXIMIZED_BOTH);
		setVisible(true);
	}

	private void initPanel(List<Word> words, Map<WordPair, Double> similarity, LayoutAlgo algo, IClusterAlgo clusterAlgo) {
		setLayout(new BorderLayout());

		WordCloudPanel panel = new WordCloudPanel(words, algo, clusterAlgo, null);
		add(BorderLayout.CENTER, panel);
		add(BorderLayout.EAST, new MetricsPanel(words, similarity, algo));

		setJMenuBar(new WordCloudMenuBar(panel));
	}
}