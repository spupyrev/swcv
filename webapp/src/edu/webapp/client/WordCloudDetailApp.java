package edu.webapp.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.SimplePanel;

import edu.webapp.shared.WordCloud;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class WordCloudDetailApp implements EntryPoint
{
    /**
     * Create a remote service proxy to talk to the server-side Greeting
     * service.
     */
    private final WordCloudDetailServiceAsync service = GWT.create(WordCloudDetailService.class);

    /**
     * This is the entry point method.
     */
    public void onModuleLoad()
    {
        int id = Integer.valueOf(Window.Location.getParameter("id"));
        service.getWordCloud(id, new AsyncCallback<WordCloud>()
        {
            public void onSuccess(WordCloud cloud)
            {
                SimplePanel panel = createPanel(cloud);
                RootPanel.get("cloud-div").add(panel);
            }

            public void onFailure(Throwable caught)
            {
                DialogBox errorBox = AppUtils.createErrorBox(caught, null);
                errorBox.center();
                errorBox.show();
            }
        });
    }

    private SimplePanel createPanel(WordCloud cloud)
    {
        SimplePanel panel = new SimplePanel();
        panel.setPixelSize(cloud.getWidth() + 10, cloud.getHeight() + 10);
        panel.addStyleName("center");
        panel.add(new HTML(cloud.getSvg()));
        return panel;
    }

}