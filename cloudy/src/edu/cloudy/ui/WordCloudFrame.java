package edu.cloudy.ui;

import edu.cloudy.colors.ColorScheme;
import edu.cloudy.layout.LayoutResult;
import edu.cloudy.nlp.Word;
import edu.cloudy.nlp.WordPair;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;

import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class WordCloudFrame extends JFrame
{
    private static final long serialVersionUID = 6602115306287717309L;

    public WordCloudFrame(List<Word> words, Map<WordPair, Double> similarity, LayoutResult layout, ColorScheme colorScheme)
    {
        JPopupMenu.setDefaultLightWeightPopupEnabled(false);

        initPanel(words, similarity, layout, colorScheme);
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

    private void initPanel(List<Word> words, Map<WordPair, Double> similarity, LayoutResult layout, ColorScheme colorScheme)
    {
        setLayout(new BorderLayout());

        WordCloudPanel panel = new WordCloudPanel(prepareUIWords(words, layout, colorScheme));
        add(BorderLayout.CENTER, panel);
        add(BorderLayout.EAST, new MetricsPanel(words, similarity, layout));

        setJMenuBar(new WordCloudMenuBar(panel));
    }
    
    private List<UIWord> prepareUIWords(List<Word> words, LayoutResult layout, ColorScheme colorScheme)
    {
        List<UIWord> res = new ArrayList<UIWord>();
        for (Word w : words)
        {
            UIWord uiWord = new UIWord();
            uiWord.setText(w.word);
            uiWord.setColor(colorScheme.getColor(w));
            uiWord.setRectangle(layout.getWordPosition(w));

            res.add(uiWord);
        }

        return res;
    }
    
}
