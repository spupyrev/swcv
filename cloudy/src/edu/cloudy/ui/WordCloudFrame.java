package edu.cloudy.ui;

import edu.cloudy.colors.ColorScheme;
import edu.cloudy.layout.LayoutResult;
import edu.cloudy.layout.WordGraph;
import edu.cloudy.render.UIWord;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;

import java.awt.BorderLayout;

public class WordCloudFrame extends JFrame
{
    private static final long serialVersionUID = 6602115306287717309L;

    public WordCloudFrame(WordGraph wordGraph, LayoutResult layout, ColorScheme colorScheme)
    {
        JPopupMenu.setDefaultLightWeightPopupEnabled(false);

        initPanel(wordGraph, layout, colorScheme);
        setTitle("WordCloud");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setVisible(true);

    }

    public WordCloudFrame(JPanel panel)
    {
        JPopupMenu.setDefaultLightWeightPopupEnabled(false);

        add(panel);
        setJMenuBar(new WordCloudMenuBar(panel));

        setTitle("WordCloud");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setVisible(true);
    }

    private void initPanel(WordGraph wordGraph, LayoutResult layout, ColorScheme colorScheme)
    {
        setLayout(new BorderLayout());

        WordCloudPanel panel = new WordCloudPanel(UIWord.prepareUIWords(wordGraph.getWords(), layout, colorScheme));
        add(BorderLayout.CENTER, panel);
        add(BorderLayout.EAST, new MetricsPanel(wordGraph, layout));

        setJMenuBar(new WordCloudMenuBar(panel));
    }
}
