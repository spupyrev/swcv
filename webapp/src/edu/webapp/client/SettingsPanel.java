package edu.webapp.client;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.user.client.ui.CaptionPanel;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTMLTable.CellFormatter;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Widget;

import edu.webapp.client.ui.GroupedListBox;
import edu.webapp.shared.WCColorScheme;
import edu.webapp.shared.WCColorSchemeCollection;
import edu.webapp.shared.WCFont;
import edu.webapp.shared.WCFontCollection;
import edu.webapp.shared.WCSetting;
import edu.webapp.shared.WCSetting.ASPECT_RATIO;
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

    private ListBox colorSchemeWidget;
    private ListBox rankingWidget;
    private ListBox fontWidget;

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
        rankingWidget = createRankingListBox();
        layout.setWidget(3, 1, rankingWidget);

        cf.setStyleName(0, 2, "adv-cell-label");
        layout.setWidget(0, 2, createLabel("Font:"));
        fontWidget = createFontListBox();
        layout.setWidget(0, 3, fontWidget);

        cf.setStyleName(1, 2, "adv-cell-label");
        layout.setWidget(1, 2, createLabel("Color:"));
        colorSchemeWidget = createColorListBox();
        layout.setWidget(1, 3, colorSchemeWidget);

        cf.setStyleName(2, 2, "adv-cell-label");
        layout.setWidget(2, 2, createLabel("Aspect Ratio:"));
        layout.setWidget(2, 3, createAspectRatioListBox());

        cf.setStyleName(3, 2, "adv-cell-label");
        layout.setWidget(3, 2, createLabel("Language:"));
        layout.setWidget(3, 3, createLanguageListBox());

        // Wrap the content in a DecoratorPanel
        CaptionPanel panel = new CaptionPanel();
        panel.setCaptionText("advanced options for word cloud generation");
        panel.add(layout);
        return panel;
    }

    private ListBox createLanguageListBox()
    {
        final ListBox box = new ListBox();
        box.addItem("English");
        box.addItem("Non-English");

        box.addChangeHandler(new ChangeHandler()
        {
            public void onChange(ChangeEvent event)
            {
                setNonEnglishText(box.getSelectedIndex() != 1);
            }
        });

        return box;
    }

    private void setNonEnglishText(boolean checked)
    {
        RANKING_ALGORITHM rankval = WCSetting.RANKING_ALGORITHM.valueOf(rankingWidget.getValue(rankingWidget.getSelectedIndex()));
        if (checked)
        { // non en setting
            List<Integer> enFonts = getEnOnlyFonts();
            if (rankval == WCSetting.RANKING_ALGORITHM.TF_IDF)
            {
                rankingWidget.setSelectedIndex(0);
                setting.setFont(WCFontCollection.getByName("Archer"));
            }

            if (enFonts.contains(fontWidget.getSelectedIndex()))
            {
                fontWidget.setSelectedIndex(1);
                setting.setFont(WCFontCollection.getByName("ComicSansMS"));
            }

            for (int i = 0; i < fontWidget.getItemCount(); ++i)
                if (enFonts.contains(i))
                    setDisabled(fontWidget, i);

            setDisabled(rankingWidget, findIndex(rankingWidget, WCSetting.RANKING_ALGORITHM.TF_IDF.toString()));
        }
        else
        { // en setting
            for (int i = 0; i < fontWidget.getItemCount(); ++i)
                removeDisabled(fontWidget, i);
            for (int i = 0; i < rankingWidget.getItemCount(); ++i)
                removeDisabled(rankingWidget, i);
        }
    }

    private List<Integer> getEnOnlyFonts()
    {
        List<Integer> indices = new ArrayList<Integer>();

        for (WCFont font : WCFontCollection.list())
            if (font.isEnglishOnly())
                indices.add(findIndex(fontWidget, font.getName()));

        return indices;
    }

    private Widget createLabel(String text)
    {
        Label label = new Label(text);
        label.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);
        return label;
    }

    private void removeDisabled(ListBox b, int index)
    {
        b.getElement().getElementsByTagName("option").getItem(index).removeAttribute("disabled");
    }

    private void setDisabled(ListBox b, int index)
    {
        b.getElement().getElementsByTagName("option").getItem(index).setAttribute("disabled", "disabled");
    }

    private ListBox createColorListBox()
    {
        final GroupedListBox box = new GroupedListBox();
        for (WCColorScheme scheme : WCColorSchemeCollection.list())
            box.addItem(scheme.getType() + " | " + scheme.getDescription(), scheme.getName());

        box.setSelectedIndex(findIndex(box, setting.getColorScheme().getName()));

        box.addChangeHandler(new ChangeHandler()
        {
            public void onChange(ChangeEvent event)
            {
                WCColorScheme value = WCColorSchemeCollection.getByName(box.getValue(box.getSelectedIndex()));
                setting.setColorScheme(value);
            }
        });

        return box;
    }

    private Widget createSimilarityListBox()
    {
        final ListBox box = new ListBox();
        box.addItem("Cosine Coefficient", WCSetting.SIMILARITY_ALGORITHM.COSINE.toString());
        box.addItem("Jaccard Coefficient", WCSetting.SIMILARITY_ALGORITHM.JACCARD.toString());
        box.addItem("Lin's Similarity", WCSetting.SIMILARITY_ALGORITHM.LEXICAL.toString());
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

        return box;
    }

    private Widget createLayoutListBox()
    {
        final ListBox box = new ListBox();
        box.addItem("Wordle (random)", WCSetting.LAYOUT_ALGORITHM.WORDLE.toString());
        box.addItem("Tag Cloud (alphabetical)", WCSetting.LAYOUT_ALGORITHM.TAG_ALPHABETICAL.toString());
        box.addItem("Tag Cloud (rank)", WCSetting.LAYOUT_ALGORITHM.TAG_RANK.toString());
        box.addItem("Force-Directed", WCSetting.LAYOUT_ALGORITHM.MDS.toString());
        box.addItem("Star Forest", WCSetting.LAYOUT_ALGORITHM.STAR.toString());
        box.addItem("Cycle Cover", WCSetting.LAYOUT_ALGORITHM.CYCLE.toString());
        box.addItem("Context Preserving", WCSetting.LAYOUT_ALGORITHM.CPWCV.toString());
        box.addItem("Inflate and Push", WCSetting.LAYOUT_ALGORITHM.INFLATE.toString());
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

        box.setTitle("Number of words to include in the word cloud");

        return box;
    }

    private ListBox createFontListBox()
    {
        final ListBox box = new ListBox();
        for (WCFont font : WCFontCollection.list())
            box.addItem(font.getDescription(), font.getName());

        box.setSelectedIndex(findIndex(box, setting.getFont().getName()));

        box.addChangeHandler(new ChangeHandler()
        {
            public void onChange(ChangeEvent event)
            {
                WCFont value = WCFontCollection.getByName(box.getValue(box.getSelectedIndex()));
                setting.setFont(value);
            }
        });

        return box;
    }

    private ListBox createRankingListBox()
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

        return box;
    }

}
