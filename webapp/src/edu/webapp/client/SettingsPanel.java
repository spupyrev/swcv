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
import edu.webapp.shared.WCAspectRatio;
import edu.webapp.shared.WCColorScheme;
import edu.webapp.shared.WCFont;
import edu.webapp.shared.WCLayoutAlgo;
import edu.webapp.shared.WCRankingAlgo;
import edu.webapp.shared.WCSetting;
import edu.webapp.shared.WCSimilarityAlgo;
import edu.webapp.shared.registry.WCAspectRatioRegistry;
import edu.webapp.shared.registry.WCColorSchemeRegistry;
import edu.webapp.shared.registry.WCFontRegistry;
import edu.webapp.shared.registry.WCLayoutAlgoRegistry;
import edu.webapp.shared.registry.WCRankingAlgoRegistry;
import edu.webapp.shared.registry.WCSimilarityAlgoRegistry;

import java.util.ArrayList;
import java.util.Arrays;
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
        //TODO
        if (checked)
        { // non en setting
            WCRankingAlgo rankval = WCRankingAlgoRegistry.getById(rankingWidget.getValue(rankingWidget.getSelectedIndex()));
            List<Integer> enFonts = getEnOnlyFonts();

            if (rankval.getId().equals("tf-idf"))
            {
                rankingWidget.setSelectedIndex(0);
                setting.setFont(WCFontRegistry.getByName("Archer"));
            }

            if (enFonts.contains(fontWidget.getSelectedIndex()))
            {
                fontWidget.setSelectedIndex(1);
                setting.setFont(WCFontRegistry.getByName("ComicSansMS"));
            }

            for (int i = 0; i < fontWidget.getItemCount(); ++i)
                if (enFonts.contains(i))
                    setDisabled(fontWidget, i);

            setDisabled(rankingWidget, findIndex(rankingWidget, "tf-df"));
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

        for (WCFont font : WCFontRegistry.list())
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

    private int findIndex(ListBox box, String value)
    {
        for (int i = 0; i < box.getItemCount(); i++)
            if (box.getValue(i).equals(value))
                return i;
        return -1;
    }

    private Widget createAspectRatioListBox()
    {
        final ListBox box = new ListBox();
        for (WCAspectRatio algo : WCAspectRatioRegistry.list())
            box.addItem(algo.getDescription(), algo.getId());

        box.setSelectedIndex(findIndex(box, setting.getAspectRatio().getId()));

        box.addChangeHandler(new ChangeHandler()
        {
            public void onChange(ChangeEvent event)
            {
                WCAspectRatio value = WCAspectRatioRegistry.getById(box.getValue(box.getSelectedIndex()));
                setting.setAspectRatio(value);
            }
        });

        return box;
    }

    private Widget createLayoutListBox()
    {
        final GroupedListBox box = new GroupedListBox();
        for (WCLayoutAlgo algo : WCLayoutAlgoRegistry.list())
            box.addItem(algo.getType() + " | " + algo.getDescription(), algo.getId());

        box.setSelectedIndex(findIndex(box, setting.getLayoutAlgorithm().getId()));

        box.addChangeHandler(new ChangeHandler()
        {
            public void onChange(ChangeEvent event)
            {
                WCLayoutAlgo value = WCLayoutAlgoRegistry.getById(box.getValue(box.getSelectedIndex()));
                setting.setLayoutAlgorithm(value);
            }
        });

        return box;
    }

    private ListBox createNumberListBox()
    {
        final ListBox box = new ListBox();
        List<String> values = Arrays.asList("10", "20", "30", "40", "50", "75", "100", "125", "150", "200", "250", "300");

        for (int i = 0; i < values.size(); i++)
            box.addItem(values.get(i));

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
        for (WCFont font : WCFontRegistry.list())
            box.addItem(font.getDescription(), font.getName());

        box.setSelectedIndex(findIndex(box, setting.getFont().getName()));

        box.addChangeHandler(new ChangeHandler()
        {
            public void onChange(ChangeEvent event)
            {
                WCFont value = WCFontRegistry.getByName(box.getValue(box.getSelectedIndex()));
                setting.setFont(value);
            }
        });

        return box;
    }

    private ListBox createRankingListBox()
    {
        final ListBox box = new ListBox();
        for (WCRankingAlgo algo : WCRankingAlgoRegistry.list())
            box.addItem(algo.getDescription(), algo.getId());

        box.setSelectedIndex(findIndex(box, setting.getRankingAlgorithm().getId()));

        box.addChangeHandler(new ChangeHandler()
        {
            public void onChange(ChangeEvent event)
            {
                WCRankingAlgo value = WCRankingAlgoRegistry.getById(box.getValue(box.getSelectedIndex()));
                setting.setRankingAlgorithm(value);
            }
        });

        return box;
    }

    private ListBox createColorListBox()
    {
        final GroupedListBox box = new GroupedListBox();
        for (WCColorScheme scheme : WCColorSchemeRegistry.list())
            box.addItem(scheme.getType() + " | " + scheme.getDescription(), scheme.getName());

        box.setSelectedIndex(findIndex(box, setting.getColorScheme().getName()));

        box.addChangeHandler(new ChangeHandler()
        {
            public void onChange(ChangeEvent event)
            {
                WCColorScheme value = WCColorSchemeRegistry.getByName(box.getValue(box.getSelectedIndex()));
                setting.setColorScheme(value);
            }
        });

        return box;
    }

    private Widget createSimilarityListBox()
    {
        final ListBox box = new ListBox();
        for (WCSimilarityAlgo font : WCSimilarityAlgoRegistry.list())
            box.addItem(font.getDescription(), font.getId());

        box.setSelectedIndex(findIndex(box, setting.getSimilarityAlgorithm().getId()));

        box.addChangeHandler(new ChangeHandler()
        {
            public void onChange(ChangeEvent event)
            {
                WCSimilarityAlgo value = WCSimilarityAlgoRegistry.getById(box.getValue(box.getSelectedIndex()));
                setting.setSimilarityAlgorithm(value);
            }
        });

        return box;
    }

}
