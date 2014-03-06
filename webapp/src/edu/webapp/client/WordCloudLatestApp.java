package edu.webapp.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.shared.DateTimeFormat;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HTMLTable.CellFormatter;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.Widget;

import edu.webapp.shared.WordCloud;

import java.text.ParseException;
import java.util.Date;
import java.util.List;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class WordCloudLatestApp implements EntryPoint
{
    /**
     * Create a remote service proxy to talk to the server-side Greeting
     * service.
     */
    private final WordCloudLatestServiceAsync listService = GWT.create(WordCloudLatestService.class);

    /**
     * This is the entry point method.
     */
    public void onModuleLoad()
    {
        final String debug = Window.Location.getParameter("debug");
        listService.getLatestWordClouds(15, new AsyncCallback<List<WordCloud>>()
        {
            public void onSuccess(List<WordCloud> clouds)
            {
                try
                {
                    Grid table = createTable(clouds, "true".equalsIgnoreCase(debug));
                    RootPanel.get("latestTable").add(table);
                }
                catch (Exception e)
                {
                    throw new RuntimeException(e);
                }
            }

            public void onFailure(Throwable caught)
            {
                DialogBox errorBox = AppUtils.createErrorBox(caught, null);
                errorBox.center();
                errorBox.show();
            }
        });
    }

    private Grid createTable(List<WordCloud> clouds, boolean debug) throws ParseException
    {
        Grid table = new Grid(clouds.size() + 1, debug ? 4 : 3);
        table.addStyleName("latest");
        CellFormatter cf = table.getCellFormatter();

        table.setHTML(0, 0, "<b>id</b>");
        table.setHTML(0, 1, "<b>creation date</b>");
        table.setHTML(0, 2, "<b>source</b>");
        cf.setWidth(0, 0, "10%");
        cf.setWidth(0, 1, "25%");
        cf.setWidth(0, 2, "65%");
        cf.setHorizontalAlignment(0, 0, HasHorizontalAlignment.ALIGN_CENTER);
        cf.setHorizontalAlignment(0, 1, HasHorizontalAlignment.ALIGN_CENTER);
        cf.setHorizontalAlignment(0, 2, HasHorizontalAlignment.ALIGN_CENTER);

        if (debug)
        {
            table.setHTML(0, 3, "<b>ip</b>");
            cf.setWidth(0, 1, "20%");
            cf.setWidth(0, 2, "60%");
            cf.setWidth(0, 3, "10%");
            cf.setHorizontalAlignment(0, 3, HasHorizontalAlignment.ALIGN_CENTER);
        }

        for (int i = 0; i < clouds.size(); i++)
        {
            WordCloud cloud = clouds.get(i);

            table.setHTML(i + 1, 0, "<a href='cloud.html?id=" + cloud.getId() + "'>" + cloud.getId() + "</a>");
            Date dt = cloud.getCreationDateAsDate();
            table.setHTML(i + 1, 1, DateTimeFormat.getFormat("yyyy-MM-dd HH:mm:ss").format(dt));
            table.setWidget(i + 1, 2, createSourceField(cloud.getInputText()));
            cf.setHorizontalAlignment(i + 1, 2, HasHorizontalAlignment.ALIGN_LEFT);

            if (debug)
            {
                table.setHTML(i + 1, 3, cloud.getCreatorIP());
            }
        }

        return table;
    }

    private Widget createSourceField(String inputText)
    {
        inputText = inputText.trim();
        if (inputText.startsWith("http://") || inputText.startsWith("https://"))
            return new Anchor(cutString(inputText), inputText);

        return new HTML(cutString(inputText));
    }

    private String cutString(String inputText)
    {
        if (inputText.length() > 80)
            return inputText.substring(0, 77) + "...";

        return inputText;
    }

}