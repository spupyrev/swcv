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
import com.google.gwt.user.client.ui.IntegerBox;
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

    public SettingsPanel()
    {
    }

    public CaptionPanel create(WCSetting setting)
    {
        this.setting = setting;

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

        addParseOptions(layout, cf);
        
        addTooltips(layout);

        // Wrap the content in a DecoratorPanel
        CaptionPanel panel = new CaptionPanel();
        panel.setCaptionText("advanced options for word cloud generation");
        panel.add(layout);
        return panel;
    }

    private void addParseOptions(FlexTable layout, CellFormatter cf)
    {
        cf.setStyleName(0, 4, "adv-cell-label");
        layout.setWidget(0, 4, createLabel("Remove Common Words:"));
        layout.setWidget(0, 5, createCheckboxStopwords());

        cf.setStyleName(1, 4, "adv-cell-label");
        layout.setWidget(1, 4, createLabel("Group Similar Words:"));
        layout.setWidget(1, 5, createCheckboxStem());

        cf.setStyleName(2, 4, "adv-cell-label");
        layout.setWidget(2, 4, createLabel("Remove Numbers:"));
        layout.setWidget(2, 5, createCheckboxRemoveNumbers());

        cf.setStyleName(3, 4, "adv-cell-label");
        layout.setWidget(3, 4, createLabel("Minimum Word Length:"));
        layout.setWidget(3, 5, createIntegerField());

    }

    private void addTooltips(FlexTable layout)
    {
        for (int i = 0; i < layout.getRowCount(); i++)
        {
            layout.getWidget(i, 0).setTitle(layout.getWidget(i, 1).getTitle());
            layout.getWidget(i, 2).setTitle(layout.getWidget(i, 3).getTitle());
            layout.getWidget(i, 4).setTitle(layout.getWidget(i, 5).getTitle());
        }
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

        box.setTitle("Language of text");

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
        label.addStyleName("small");
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

        box.setTitle("Desired aspect ratio of the drawing");

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

        box.setTitle("Layout method for the word cloud");
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

        box.setTitle("Font family of the words");
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

        box.setTitle("Ranking method for computing word importance, which determines font size of each word");
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

        box.setTitle("Color theme of the words");
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

        box.setTitle("Similarity method for computing relatedness between words; similar words tend to be placed together");
        return box;
    }

    private Widget createCheckboxStopwords()
    {
        final CheckBox box = new CheckBox();
        box.setValue(setting.isRemoveStopwords());

        box.addClickHandler(new ClickHandler()
        {
            @Override
            public void onClick(ClickEvent event)
            {
                setting.setRemoveStopwords(box.getValue());
            }
        });

        box.setTitle("Exclude common stop words from the result\ne.g., 'the', 'is', 'at', 'which', 'on' etc");
        return box;
    }

    private Widget createCheckboxStem()
    {
        final CheckBox box = new CheckBox();
        box.setValue(setting.isStemWords());

        box.addClickHandler(new ClickHandler()
        {
            @Override
            public void onClick(ClickEvent event)
            {
                setting.setStemWords(box.getValue());
            }
        });

        box.setTitle("Combine similar words\ne.g., 'dance', 'dancer', 'danced', 'dancing' -> 'dance'");
        return box;
    }

    private Widget createCheckboxRemoveNumbers()
    {
        final CheckBox box = new CheckBox();
        box.setValue(setting.isRemoveNumbers());

        box.addClickHandler(new ClickHandler()
        {
            @Override
            public void onClick(ClickEvent event)
            {
                setting.setRemoveNumbers(box.getValue());
            }
        });

        box.setTitle("Remove numbers and punctuation characters from the result");
        return box;
    }

    private Widget createIntegerField()
    {
        final IntegerBox box = new IntegerBox();
        box.setValue(setting.getMinWordLength());
        box.setMaxLength(2);
        box.setWidth("15px");

        box.addChangeHandler(new ChangeHandler()
        {
            @Override
            public void onChange(ChangeEvent event)
            {
                setting.setMinWordLength(box.getValue());
            }
        });

        box.setTitle("Specify the minimum number of characters in a word");
        return box;
    }

}
