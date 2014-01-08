package com.swcwebapp.client;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.user.client.ui.CaptionPanel;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.client.ui.HTMLTable.CellFormatter;
import com.swcwebapp.shared.WCSetting;
import com.swcwebapp.shared.WCSetting.COLOR_DISTRIBUTE;
import com.swcwebapp.shared.WCSetting.COLOR_SCHEME;
import com.swcwebapp.shared.WCSetting.FONT;
import com.swcwebapp.shared.WCSetting.LAYOUT_ALGORITHM;
import com.swcwebapp.shared.WCSetting.RANKING_ALGORITHM;
import com.swcwebapp.shared.WCSetting.SIMILARITY_ALGORITHM;

/**
 * @author spupyrev
 * Dec 27, 2013
 */
public class SettingsPanel
{
    private WCSetting setting;

    public SettingsPanel(WCSetting setting)
    {
        this.setting = setting;
    }

    public CaptionPanel create()
    {
        FlexTable layout = new FlexTable();

        CellFormatter cf = layout.getCellFormatter();
        // Add some standard form options
        layout.setWidget(0, 0, createLabel("Number of Words:"));
        layout.setWidget(0, 1, createNumberListBox());
        layout.setWidget(1, 0, createLabel("Layout Algorithm:"));
        layout.setWidget(1, 1, createLayoutListBox());
        layout.setWidget(2, 0, createLabel("Similarity Algorithm:"));
        layout.setWidget(2, 1, createSimilarityListBox());
        layout.setWidget(3, 0, createLabel("Ranking Algorithm:"));
        layout.setWidget(3, 1, createRankingListBox());
        
        layout.setWidget(0, 2, createLabel("Font:"));
        cf.setStyleName(0, 2, "adv-cell-label");
        layout.setWidget(0, 3, createFontListBox());
        layout.setWidget(1, 2, createLabel("Color Scheme:"));
        cf.setStyleName(1, 2, "adv-cell-label");
        layout.setWidget(1, 3, createColorListBox());
        layout.setWidget(2, 2, createLabel("Color Distribute Scheme:"));
        cf.setStyleName(2, 2, "adv-cell-label");
        layout.setWidget(2, 3, createColorDistributeListBox());

        // Wrap the content in a DecoratorPanel
        CaptionPanel panel = new CaptionPanel();
        panel.setCaptionText("advanced options for word cloud generation");
        panel.add(layout);
        return panel;
    }

    private Widget createLabel(String text)
    {
        Label label = new Label(text);
        label.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);
        //label.set
        return label;
    }

    private Widget createColorDistributeListBox()
    {
        final ListBox box = new ListBox();
        box.addStyleName("inconsolataNormal");
        box.addItem("Random", WCSetting.COLOR_DISTRIBUTE.RANDOM.toString());
        box.addItem("Words Rank", WCSetting.COLOR_DISTRIBUTE.WORD_RANK.toString());

        setting.setColorDistribute(WCSetting.COLOR_DISTRIBUTE.RANDOM);
        box.setSelectedIndex(findIndex(box, setting.getColorDistribute().toString()));

        box.addChangeHandler(new ChangeHandler()
        {
            public void onChange(ChangeEvent event)
            {
                COLOR_DISTRIBUTE value = WCSetting.COLOR_DISTRIBUTE.valueOf(box.getValue(box.getSelectedIndex()));
                setting.setColorDistribute(value);
            }
        });

        return box;
    }

    private Widget createColorListBox()
    {
        final ListBox box = new ListBox();
        box.addStyleName("inconsolataNormal");
        box.addItem("BEAR DOWN!", WCSetting.COLOR_SCHEME.BEAR_DOWN.toString());
        box.addItem("Blue", WCSetting.COLOR_SCHEME.BLUE.toString());
        box.addItem("Orange", WCSetting.COLOR_SCHEME.ORANGE.toString());
        box.addItem("Green", WCSetting.COLOR_SCHEME.GREEN.toString());
        box.addItem("Sequential Blue", WCSetting.COLOR_SCHEME.BLUE_SEQ.toString());
        box.addItem("Sequential Orange", WCSetting.COLOR_SCHEME.ORANGE_SEQ.toString());
        box.addItem("Sequential Green", WCSetting.COLOR_SCHEME.GREEN_SEQ.toString());
        box.addItem("Trinity Scheme 1", WCSetting.COLOR_SCHEME.TRISCHEME_1.toString());
        box.addItem("Trinity Scheme 2", WCSetting.COLOR_SCHEME.TRISCHEME_2.toString());
        box.addItem("Trinity Scheme 3", WCSetting.COLOR_SCHEME.TRISCHEME_3.toString());
        box.addItem("Similar Scheme 1", WCSetting.COLOR_SCHEME.SIMILAR_1.toString());
        box.addItem("Similar Scheme 2", WCSetting.COLOR_SCHEME.SIMILAR_2.toString());
        box.addItem("Similar Scheme 3", WCSetting.COLOR_SCHEME.SIMILAR_3.toString());

        setting.setColorScheme(WCSetting.COLOR_SCHEME.BEAR_DOWN);
        box.setSelectedIndex(findIndex(box, setting.getColorScheme().toString()));

        box.addChangeHandler(new ChangeHandler()
        {
            public void onChange(ChangeEvent event)
            {
                COLOR_SCHEME value = WCSetting.COLOR_SCHEME.valueOf(box.getValue(box.getSelectedIndex()));
                setting.setColorScheme(value);
            }
        });

        return box;
    }

    private Widget createSimilarityListBox()
    {
        final ListBox box = new ListBox();
        box.addStyleName("inconsolataNormal");
        box.addItem("Cosine Coefficient", WCSetting.SIMILARITY_ALGORITHM.COSINE.toString());
        box.addItem("Jaccard Coefficient", WCSetting.SIMILARITY_ALGORITHM.JACCARD.toString());
        box.addItem("Lin's Similarity Algorithm", WCSetting.SIMILARITY_ALGORITHM.LEXICAL.toString());
        box.addItem("Euclidean Distance", WCSetting.SIMILARITY_ALGORITHM.MATRIXDIS.toString());
        box.addItem("Dice Coefficient", WCSetting.SIMILARITY_ALGORITHM.DICECOEFFI.toString());

        box.setSelectedIndex(findIndex(box, setting.getSimilarityAlgorithm().toString()));

        box.addChangeHandler(new ChangeHandler()
        {
            public void onChange(ChangeEvent event)
            {
                SIMILARITY_ALGORITHM value = WCSetting.SIMILARITY_ALGORITHM.valueOf(box.getValue(box.getSelectedIndex()));
                setting.setSimilarityAlgorithm(value);
            }
        });

        return box;
    }

    private Widget createLayoutListBox()
    {
        final ListBox box = new ListBox();
        box.addStyleName("inconsolataNormal");
        box.addItem("Star Forest", WCSetting.LAYOUT_ALGORITHM.STAR.toString());
        box.addItem("Cycle Cover", WCSetting.LAYOUT_ALGORITHM.CYCLE.toString());
        box.addItem("Wordle (random)", WCSetting.LAYOUT_ALGORITHM.WORDLE.toString());
        box.addItem("Context Preserving", WCSetting.LAYOUT_ALGORITHM.CPDWCV.toString());
        box.addItem("Seam Carving", WCSetting.LAYOUT_ALGORITHM.SEAM.toString());
        box.addItem("Inflate and Push", WCSetting.LAYOUT_ALGORITHM.INFLATE.toString());

        box.setSelectedIndex(findIndex(box, setting.getLayoutAlgorithm().toString()));

        box.addChangeHandler(new ChangeHandler()
        {
            public void onChange(ChangeEvent event)
            {
                LAYOUT_ALGORITHM value = WCSetting.LAYOUT_ALGORITHM.valueOf(box.getValue(box.getSelectedIndex()));
                setting.setLayoutAlgorithm(value);
            }
        });

        return box;
    }

    private int findIndex(ListBox box, String value)
    {
        for (int i = 0; i < box.getItemCount(); i++)
            if (box.getValue(i).equals(value))
                return i;
        return -1;
    }

    private ListBox createNumberListBox()
    {
        final ListBox box = new ListBox();
        box.addStyleName("inconsolataNormal");
        String[] values = new String[] {
                "5",
                "10",
                "15",
                "20",
                "30",
                "40",
                "50",
                "75",
                "100",
                "125",
                "150",
                "200" };

        for (int i = 0; i < values.length; i++)
            box.addItem(values[i]);

        box.setSelectedIndex(findIndex(box, String.valueOf(setting.getWordCount())));

        box.addChangeHandler(new ChangeHandler()
        {
            public void onChange(ChangeEvent event)
            {
                int value = Integer.parseInt(box.getValue(box.getSelectedIndex()));
                setting.setWordCount(value);
            }
        });

        return box;
    }

    private ListBox createFontListBox()
    {
        final ListBox box = new ListBox();
        box.addStyleName("inconsolataNormal");
        box.addItem("Archer - UofA Official Font", WCSetting.FONT.Archer.toString());
        box.addItem("Crimson - Serif", WCSetting.FONT.Crimson.toString());
        box.addItem("Dearest - Blackletter", WCSetting.FONT.Dearest.toString());

        box.setSelectedIndex(findIndex(box, setting.getFont().toString()));

        box.addChangeHandler(new ChangeHandler()
        {
            public void onChange(ChangeEvent event)
            {
                FONT value = WCSetting.FONT.valueOf(box.getValue(box.getSelectedIndex()));
                setting.setFont(value);
            }
        });

        return box;
    }

    private Widget createRankingListBox()
    {
        final ListBox box = new ListBox();
        box.addStyleName("inconsolataNormal");
        box.addItem("Frequency", WCSetting.RANKING_ALGORITHM.TF.toString());
        box.addItem("TF/ICF - BrownCorpus", WCSetting.RANKING_ALGORITHM.TF_IDF.toString());
        box.addItem("LexRank", WCSetting.RANKING_ALGORITHM.LEX.toString());

        box.setSelectedIndex(findIndex(box, setting.getRankingAlgorithm().toString()));

        box.addChangeHandler(new ChangeHandler()
        {
            public void onChange(ChangeEvent event)
            {
                RANKING_ALGORITHM value = WCSetting.RANKING_ALGORITHM.valueOf(box.getValue(box.getSelectedIndex()));
                setting.setRankingAlgorithm(value);
            }
        });

        return box;
    }

}
