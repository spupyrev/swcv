package edu.webapp.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.shared.DateTimeFormat;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HTMLTable.CellFormatter;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.Widget;

import edu.webapp.shared.DBStatistics;
import edu.webapp.shared.WordCloud;

import java.util.Date;
import java.util.List;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class WordCloudLatestApp implements EntryPoint
{
    private static final int NUMBER_OF_LATEST_CLOUDS = 50;
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
        final String debugParameter = Window.Location.getParameter("debug");
        final boolean debug = "true".equalsIgnoreCase(debugParameter);

        if (debug)
        {
            listService.getStatistics(new AsyncCallback<DBStatistics>()
            {
                public void onSuccess(DBStatistics result)
                {
                    RootPanel.get("statTable").add(createStatTable(result));
                }

                public void onFailure(Throwable caught)
                {
                    AppUtils.onFailure(caught);
                }
            });
        }

        listService.getLatestWordClouds(NUMBER_OF_LATEST_CLOUDS, new AsyncCallback<List<WordCloud>>()
        {
            public void onSuccess(List<WordCloud> clouds)
            {
                Grid table = createTable(clouds, debug);
                RootPanel.get("latestTable").add(table);
            }

            public void onFailure(Throwable caught)
            {
                AppUtils.onFailure(caught);
            }
        });
    }

    private Grid createStatTable(DBStatistics result)
    {
        Grid table = new Grid(3, 2);
        table.addStyleName("stat");

        table.setHTML(0, 0, "the number of clouds <b>in total</b>");
        table.setHTML(1, 0, "the number of clouds constructed <b>last month</b>");
        table.setHTML(2, 0, "the number of clouds constructed <b>last week</b>");

        table.setHTML(0, 1, "" + result.getTotal());
        table.setHTML(1, 1, "" + result.getLastMonth());
        table.setHTML(2, 1, "" + result.getLastWeek());

        CellFormatter cf = table.getCellFormatter();
        cf.setWidth(0, 0, "65%");
        cf.setWidth(0, 1, "35%");
        return table;
    }

    private Grid createTable(List<WordCloud> clouds, boolean debug)
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

            table.setHTML(i + 1, 0, "<a href='/cloud.html?id=" + cloud.getId() + "'>" + cloud.getId() + "</a>");
            Date dt = cloud.getCreationDateAsDate();
            table.setHTML(i + 1, 1, DateTimeFormat.getFormat("yyyy-MM-dd HH:mm:ss").format(dt));
            table.setWidget(i + 1, 2, createSourceField(cloud, debug));
            cf.setHorizontalAlignment(i + 1, 2, HasHorizontalAlignment.ALIGN_LEFT);

            if (debug)
            {
                table.setHTML(i + 1, 3, cloud.getCreatorIP());
            }
        }

        return table;
    }

    private Widget createSourceField(WordCloud cloud, boolean debug)
    {
        String inputText = cloud.getInputText().trim();
        if (debug)
        {
            Anchor link = new Anchor(cutString(inputText));
            link.setHref("/cloud/download?ft=source&id=" + cloud.getId());
            return link;
        }
        else
        {
            return new HTML(cutString(inputText));
        }
    }

    private String cutString(String inputText)
    {
        if (inputText.length() > 75)
            return inputText.substring(0, 72) + "...";

        return inputText;
    }

}