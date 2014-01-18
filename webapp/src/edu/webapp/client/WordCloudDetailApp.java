package edu.webapp.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Document;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.CaptionPanel;
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
        int id = -1;
        try
        {
            id = Integer.valueOf(Window.Location.getParameter("id"));
        }
        catch (NumberFormatException e)
        {
            DialogBox errorBox = AppUtils.createErrorBox(e, null);
            errorBox.center();
            errorBox.show();
            return;
        }

        service.getWordCloud(id, new AsyncCallback<WordCloud>()
        {
            public void onSuccess(WordCloud cloud)
            {
                SimplePanel panel = createPanel(cloud);
                RootPanel.get("cloud-div").add(panel);

                CaptionPanel settingArea = new SettingsPanel(cloud.getSettings(), false).create();
                settingArea.setCaptionText("options");
                RootPanel.get("cloud-setting").add(settingArea);

                setSaveAsLinks(cloud);
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

    private void setSaveAsLinks(WordCloud cloud)
    {
        Anchor link = Anchor.wrap(Document.get().getElementById("save-as-svg"));
        link.setHref("/cloud/download?ft=svg&id=" + cloud.getId());
        
        Anchor linkPNG = Anchor.wrap(Document.get().getElementById("save-as-png"));
        linkPNG.setHref("/cloud/download?ft=png&id=" + cloud.getId());
        
        Anchor linkPDF = Anchor.wrap(Document.get().getElementById("save-as-pdf"));
        linkPDF.setHref("/cloud/download?ft=pdf&id=" + cloud.getId());
    }

}