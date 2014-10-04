package edu.webapp.client;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.CaptionPanel;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTMLTable.CellFormatter;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Widget;

import edu.webapp.shared.WCSetting;
import edu.webapp.shared.WCSetting.ASPECT_RATIO;
import edu.webapp.shared.WCSetting.CLUSTER_ALGORITHM;
import edu.webapp.shared.WCSetting.COLOR_SCHEME;
import edu.webapp.shared.WCSetting.FONT;
import edu.webapp.shared.WCSetting.LAYOUT_ALGORITHM;
import edu.webapp.shared.WCSetting.RANKING_ALGORITHM;
import edu.webapp.shared.WCSetting.SIMILARITY_ALGORITHM;

import java.util.ArrayList;
import java.util.List;

/**
 * @author spupyrev
 * Dec 27, 2013
 */
public class SettingsPanel
{
    private WCSetting setting;
    private boolean enabled;

    private Widget clusterWidget;
    private Widget colorSchemeWidget;
    private Widget rankingWidget;
    private Widget fontWidget;

    public SettingsPanel(WCSetting setting, boolean enabled)
    {
        this.setting = setting;
        this.enabled = enabled;
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
        rankingWidget = createRankingListBox();
        layout.setWidget(3, 1, rankingWidget);

        layout.setWidget(4, 0, createLabel("Cluster Algorithm:"));
        clusterWidget = createClusterListBox();
        layout.setWidget(4, 1, clusterWidget);

        cf.setStyleName(0, 2, "adv-cell-label");
        layout.setWidget(0, 2, createLabel("Font:"));
        fontWidget = createFontListBox();
        layout.setWidget(0, 3, fontWidget);

        cf.setStyleName(1, 2, "adv-cell-label");
        layout.setWidget(1, 2, createLabel("Color Scheme:"));
        colorSchemeWidget = createColorListBox();
        layout.setWidget(1, 3, colorSchemeWidget);

        cf.setStyleName(2, 2, "adv-cell-label");
        layout.setWidget(2, 2, createLabel("Aspect Ratio:"));
        layout.setWidget(2, 3, createAspectRatioListBox());

        cf.setStyleName(3, 2, "adv-cell-label");
        layout.setWidget(3, 2, createLabel("Non-English Text:"));
        layout.setWidget(3, 3, createCheckBox());
        colorSchemeAndDistrCheck();

        // Wrap the content in a DecoratorPanel
        CaptionPanel panel = new CaptionPanel();
        panel.setCaptionText("advanced options for word cloud generation");
        panel.add(layout);
        return panel;
    }

    private Widget createCheckBox()
    {
        final CheckBox box = new CheckBox();
        box.setValue(false);
        box.addClickHandler(new ClickHandler()
        {

            @Override
            public void onClick(ClickEvent event)
            {
                setNonEnglishText(box.getValue());
            }

        });
        return box;
    }

    private void setNonEnglishText(boolean checked)
    {
        ListBox fontsbox = (ListBox)fontWidget;
        ListBox ranksbox = (ListBox)rankingWidget;
        RANKING_ALGORITHM rankval = WCSetting.RANKING_ALGORITHM.valueOf(ranksbox.getValue(ranksbox.getSelectedIndex()));
        if (checked)
        { // non en setting
            List<Integer> enFonts = getEnOnlyFonts();
            if (rankval == WCSetting.RANKING_ALGORITHM.TF_IDF)
            {
                ranksbox.setSelectedIndex(0);
                setting.setFont(WCSetting.FONT.Archer);
            }
            if (enFonts.contains(fontsbox.getSelectedIndex()))
            {
                fontsbox.setSelectedIndex(1);
                setting.setFont(WCSetting.FONT.ComicSansMS);
            }
            for (int i = 0; i < fontsbox.getItemCount(); ++i)
                if (enFonts.contains(i))
                    setDisabled(fontsbox, i);
            setDisabled(ranksbox, findIndex(ranksbox, WCSetting.RANKING_ALGORITHM.TF_IDF.toString()));
        }
        else
        { // en setting
            for (int i = 0; i < fontsbox.getItemCount(); ++i)
                removeDisabled(fontsbox, i);
            for (int i = 0; i < ranksbox.getItemCount(); ++i)
                removeDisabled(ranksbox, i);
        }
    }

    private List<Integer> getEnOnlyFonts()
    {
        List<Integer> indices = new ArrayList<Integer>();
        indices.add(findIndex((ListBox)fontWidget, WCSetting.FONT.Archer.toString()));
        indices.add(findIndex((ListBox)fontWidget, WCSetting.FONT.Eraser.toString()));
        indices.add(findIndex((ListBox)fontWidget, WCSetting.FONT.Inconsolata.toString()));
        indices.add(findIndex((ListBox)fontWidget, WCSetting.FONT.Kingthings_Gothique.toString()));
        indices.add(findIndex((ListBox)fontWidget, WCSetting.FONT.MaiandraGD.toString()));
        indices.add(findIndex((ListBox)fontWidget, WCSetting.FONT.Pacifico.toString()));
        indices.add(findIndex((ListBox)fontWidget, WCSetting.FONT.Stentiga.toString()));
        indices.add(findIndex((ListBox)fontWidget, WCSetting.FONT.Teen.toString()));
        indices.add(findIndex((ListBox)fontWidget, WCSetting.FONT.Wetpet.toString()));
        return indices;
    }

    private Widget createLabel(String text)
    {
        Label label = new Label(text);
        label.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);
        //label.set
        return label;
    }

    private Widget createClusterListBox()
    {
        final ListBox box = new ListBox();
        box.addItem("KMeans++", WCSetting.CLUSTER_ALGORITHM.KMEANS.toString());
        box.addItem("Random", WCSetting.CLUSTER_ALGORITHM.RANDOM.toString());
        box.addItem("Words Rank", WCSetting.CLUSTER_ALGORITHM.WORD_RANK.toString());
        box.addItem("Sentiment (TwitterOnly)", WCSetting.CLUSTER_ALGORITHM.SENTIMENT.toString());
        box.addItem("Dynamic (testing feature)", WCSetting.CLUSTER_ALGORITHM.DYNAMIC.toString());
        box.setSelectedIndex(findIndex(box, setting.getClusterAlgorithm().toString()));

        box.addChangeHandler(new ChangeHandler()
        {
            public void onChange(ChangeEvent event)
            {
                CLUSTER_ALGORITHM value = WCSetting.CLUSTER_ALGORITHM.valueOf(box.getValue(box.getSelectedIndex()));
                setting.setClusterAlgorithm(value);
                colorSchemeAndDistrCheck();
            }
        });

        box.setEnabled(enabled);
        return box;
    }

    private void colorSchemeAndDistrCheck()
    {
        ListBox distbox = (ListBox)clusterWidget;
        CLUSTER_ALGORITHM distValue = WCSetting.CLUSTER_ALGORITHM.valueOf(distbox.getValue(distbox.getSelectedIndex()));
        ListBox schmbox = (ListBox)colorSchemeWidget;
        List<Integer> sentiIndices = getSentiIndices();
        List<Integer> dynamicIndices = getDynamicIndices();
        if (distValue == WCSetting.CLUSTER_ALGORITHM.SENTIMENT)
        {
            if (!sentiIndices.contains(schmbox.getSelectedIndex()))
            {
                schmbox.setSelectedIndex(sentiIndices.get(0));
                setting.setColorScheme(COLOR_SCHEME.SENTIMENT);
            }
            for (Integer i : sentiIndices)
                removeDisabled(schmbox, i);
            for (int i = 0; i < schmbox.getItemCount(); ++i)
                if (!sentiIndices.contains(i))
                    setDisabled(schmbox, i);
        }
        else if (distValue == WCSetting.CLUSTER_ALGORITHM.DYNAMIC)
        {
            if (!dynamicIndices.contains(schmbox.getSelectedIndex()))
            {
                schmbox.setSelectedIndex(dynamicIndices.get(0));
                setting.setColorScheme(COLOR_SCHEME.REDBLUEBLACK);
            }
            for (Integer i : dynamicIndices)
                removeDisabled(schmbox, i);
            for (int i = 0; i < schmbox.getItemCount(); ++i)
                if (!dynamicIndices.contains(i))
                    setDisabled(schmbox, i);
        }
        else
        {
            if (sentiIndices.contains(schmbox.getSelectedIndex()))
            {
                schmbox.setSelectedIndex(0);
                setting.setColorScheme(COLOR_SCHEME.BEAR_DOWN);
            }
            for (Integer i : sentiIndices)
                setDisabled(schmbox, i);
            for (int i = 0; i < schmbox.getItemCount(); ++i)
                if (!sentiIndices.contains(i))
                    removeDisabled(schmbox, i);
        }
    }

    private List<Integer> getDynamicIndices()
    {
        List<Integer> indices = new ArrayList<Integer>();
        indices.add(findIndex((ListBox)colorSchemeWidget, WCSetting.COLOR_SCHEME.REDBLUEBLACK.toString()));
        indices.add(findIndex((ListBox)colorSchemeWidget, WCSetting.COLOR_SCHEME.BLUEREDBLACK.toString()));
        return indices;
    }

    private List<Integer> getSentiIndices()
    {
        List<Integer> indices = new ArrayList<Integer>();
        indices.add(findIndex((ListBox)colorSchemeWidget, WCSetting.COLOR_SCHEME.SENTIMENT.toString()));
        indices.add(findIndex((ListBox)colorSchemeWidget, WCSetting.COLOR_SCHEME.SENTIMENT2.toString()));
        return indices;
    }

    private void removeDisabled(ListBox b, int index)
    {
        b.getElement().getElementsByTagName("option").getItem(index).removeAttribute("disabled");
    }

    private void setDisabled(ListBox b, int index)
    {
        b.getElement().getElementsByTagName("option").getItem(index).setAttribute("disabled", "disabled");
    }

    private Widget createColorListBox()
    {
        final ListBox box = new ListBox();
        box.addItem("BEAR DOWN", WCSetting.COLOR_SCHEME.BEAR_DOWN.toString());
        box.addItem("Black", WCSetting.COLOR_SCHEME.BLACK.toString());
        box.addItem("Blue", WCSetting.COLOR_SCHEME.BLUE.toString());
        box.addItem("Sequential Blue", WCSetting.COLOR_SCHEME.BLUESEQUENTIAL.toString());
        box.addItem("Orange", WCSetting.COLOR_SCHEME.ORANGE.toString());
        box.addItem("Sequential Orange", WCSetting.COLOR_SCHEME.ORANGESEQUENTIAL.toString());
        box.addItem("Green", WCSetting.COLOR_SCHEME.GREEN.toString());
        box.addItem("Sequential Green", WCSetting.COLOR_SCHEME.GREENSEQUENTIAL.toString());
        box.addItem("ColorBrewer 1", WCSetting.COLOR_SCHEME.BREWER_1.toString());
        box.addItem("ColorBrewer 2", WCSetting.COLOR_SCHEME.BREWER_2.toString());
        box.addItem("ColorBrewer 3", WCSetting.COLOR_SCHEME.BREWER_3.toString());
        box.addItem("Trinity Scheme 1", WCSetting.COLOR_SCHEME.TRISCHEME_1.toString());
        box.addItem("Trinity Scheme 2", WCSetting.COLOR_SCHEME.TRISCHEME_2.toString());
        box.addItem("Trinity Scheme 3", WCSetting.COLOR_SCHEME.TRISCHEME_3.toString());
        box.addItem("Similar Scheme 1", WCSetting.COLOR_SCHEME.SIMILAR_1.toString());
        box.addItem("Similar Scheme 2", WCSetting.COLOR_SCHEME.SIMILAR_2.toString());
        box.addItem("Similar Scheme 3", WCSetting.COLOR_SCHEME.SIMILAR_3.toString());
        box.addItem("Sentiment ORANGE-BLUE", WCSetting.COLOR_SCHEME.SENTIMENT.toString());
        box.addItem("Sentiment GREEN-RED", WCSetting.COLOR_SCHEME.SENTIMENT2.toString());
        box.addItem("RedBlueBlack", WCSetting.COLOR_SCHEME.REDBLUEBLACK.toString());
        box.addItem("BlueRedBlack", WCSetting.COLOR_SCHEME.BLUEREDBLACK.toString());

        box.setSelectedIndex(findIndex(box, setting.getColorScheme().toString()));

        box.addChangeHandler(new ChangeHandler()
        {
            public void onChange(ChangeEvent event)
            {
                COLOR_SCHEME value = WCSetting.COLOR_SCHEME.valueOf(box.getValue(box.getSelectedIndex()));
                setting.setColorScheme(value);
            }
        });

        box.setEnabled(enabled);
        return box;
    }

    private Widget createSimilarityListBox()
    {
        final ListBox box = new ListBox();
        box.addItem("Cosine Coefficient", WCSetting.SIMILARITY_ALGORITHM.COSINE.toString());
        box.addItem("Jaccard Coefficient", WCSetting.SIMILARITY_ALGORITHM.JACCARD.toString());
        box.addItem("Lin's Similarity Algorithm", WCSetting.SIMILARITY_ALGORITHM.LEXICAL.toString());
        box.addItem("Euclidean Distance", WCSetting.SIMILARITY_ALGORITHM.MATRIXDIS.toString());

        box.setSelectedIndex(findIndex(box, setting.getSimilarityAlgorithm().toString()));

        box.addChangeHandler(new ChangeHandler()
        {
            public void onChange(ChangeEvent event)
            {
                SIMILARITY_ALGORITHM value = WCSetting.SIMILARITY_ALGORITHM.valueOf(box.getValue(box.getSelectedIndex()));
                setting.setSimilarityAlgorithm(value);
            }
        });

        box.setEnabled(enabled);
        return box;
    }

    private Widget createAspectRatioListBox()
    {
        final ListBox box = new ListBox();
        box.addItem("1:1", WCSetting.ASPECT_RATIO.AR11.toString());
        box.addItem("4:3", WCSetting.ASPECT_RATIO.AR43.toString());
        box.addItem("16:9", WCSetting.ASPECT_RATIO.AR169.toString());
        box.addItem("21:9", WCSetting.ASPECT_RATIO.AR219.toString());

        box.setSelectedIndex(findIndex(box, setting.getAspectRatio().toString()));

        box.addChangeHandler(new ChangeHandler()
        {
            public void onChange(ChangeEvent event)
            {
                ASPECT_RATIO value = WCSetting.ASPECT_RATIO.valueOf(box.getValue(box.getSelectedIndex()));
                setting.setAspectRatio(value);
            }
        });

        box.setEnabled(enabled);
        return box;
    }

    private Widget createLayoutListBox()
    {
        final ListBox box = new ListBox();
        box.addItem("Star Forest", WCSetting.LAYOUT_ALGORITHM.STAR.toString());
        box.addItem("Cycle Cover", WCSetting.LAYOUT_ALGORITHM.CYCLE.toString());
        box.addItem("Wordle (random)", WCSetting.LAYOUT_ALGORITHM.WORDLE.toString());
        box.addItem("Context Preserving", WCSetting.LAYOUT_ALGORITHM.CPWCV.toString());
        box.addItem("Inflate and Push", WCSetting.LAYOUT_ALGORITHM.INFLATE.toString());
        box.addItem("Force-Directed", WCSetting.LAYOUT_ALGORITHM.MDS.toString());
        box.addItem("Seam Carving", WCSetting.LAYOUT_ALGORITHM.SEAM.toString());

        box.setSelectedIndex(findIndex(box, setting.getLayoutAlgorithm().toString()));

        box.addChangeHandler(new ChangeHandler()
        {
            public void onChange(ChangeEvent event)
            {
                LAYOUT_ALGORITHM value = WCSetting.LAYOUT_ALGORITHM.valueOf(box.getValue(box.getSelectedIndex()));
                setting.setLayoutAlgorithm(value);
            }
        });

        box.setEnabled(enabled);
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
        String[] values = new String[] {
                "10",
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

        box.setEnabled(enabled);

        return box;
    }

    private ListBox createFontListBox()
    {
        final ListBox box = new ListBox();
        box.addItem("Archer - UofA Official Font", WCSetting.FONT.Archer.toString());
        box.addItem("Comic Sans MS", WCSetting.FONT.ComicSansMS.toString());
        box.addItem("Crimson - Serif", WCSetting.FONT.Crimson.toString());
        box.addItem("Eraser - Hand Drawn", WCSetting.FONT.Eraser.toString());
        box.addItem("Inconsolata - Monospace", WCSetting.FONT.Inconsolata.toString());
        box.addItem("Kingthings_Gothique - Blackletter", WCSetting.FONT.Kingthings_Gothique.toString());
        box.addItem("Maiandra GD", WCSetting.FONT.MaiandraGD.toString());
        box.addItem("Pacifico - Script", WCSetting.FONT.Pacifico.toString());
        box.addItem("Stentiga - Sans Serif", WCSetting.FONT.Stentiga.toString());
        box.addItem("Teen - Sans Serif", WCSetting.FONT.Teen.toString());
        box.addItem("Monofur - Monospace", WCSetting.FONT.Monofur.toString());
        box.addItem("Wetpet - Funny font", WCSetting.FONT.Wetpet.toString());

        box.setSelectedIndex(findIndex(box, setting.getFont().toString()));

        box.addChangeHandler(new ChangeHandler()
        {
            public void onChange(ChangeEvent event)
            {
                FONT value = WCSetting.FONT.valueOf(box.getValue(box.getSelectedIndex()));
                setting.setFont(value);
            }
        });

        box.setEnabled(enabled);
        return box;
    }

    private Widget createRankingListBox()
    {
        final ListBox box = new ListBox();
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

        box.setEnabled(enabled);
        return box;
    }

}
