package edu.webapp.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Document;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CaptionPanel;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.SimplePanel;

import edu.webapp.shared.WCSetting;
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
     * used for get new setting from a generated word cloud
     */
    private WCSetting setting;
    private String inputText;
    private int id;

    /**
     * This is the entry point method.
     */
    public void onModuleLoad()
    {
        id = -1;
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
                setting = cloud.getSettings();
                inputText = cloud.getSourceText();
                
                initializeContentPanel(cloud);
                initializeSettingPanel(cloud);

                addSaveAsLinks(cloud);
            }

            public void onFailure(Throwable caught)
            {
                DialogBox errorBox = AppUtils.createErrorBox(caught, null);
                errorBox.center();
                errorBox.show();
            }
        });
        
        createUpdateWordCloudButton();
        createRandomWordCloudButton();
    }

    private void initializeContentPanel(WordCloud cloud)
    {
        SimplePanel panel = createPanel(cloud.getSvg(), cloud.getWidth() + 20, cloud.getHeight() + 20);
        RootPanel rPanel = RootPanel.get("cloud-div");
        rPanel.clear();
        rPanel.add(panel);
        rPanel.setPixelSize(cloud.getWidth() + 20, cloud.getHeight() + 20);
        rPanel.addStyleName("center");

        if (cloud.isDynamic())
        {
            SimplePanel panel2 = createPanel(cloud.getSvg2(), cloud.getWidth2() + 20, cloud.getHeight2() + 20);
            RootPanel rPanel2 = RootPanel.get("cloud-div2");
            rPanel2.clear();
            rPanel2.add(panel2);
            rPanel2.setPixelSize(cloud.getWidth2() + 20, cloud.getHeight2() + 20);
            rPanel2.addStyleName("center");
        }
    }

    private void initializeSettingPanel(WordCloud cloud)
    {
        CaptionPanel settingArea = new SettingsPanel().create(setting);
        settingArea.setCaptionText("options");
        
        RootPanel rPanel = RootPanel.get("cloud-setting");
        rPanel.clear();
        rPanel.add(settingArea);
    }

    private void createUpdateWordCloudButton()
    {
        Button sendButton = Button.wrap(Document.get().getElementById("btn_create_new_wc"));
        sendButton.addClickHandler(new ClickHandler()
        {
            public void onClick(ClickEvent event)
            {
                updateWordCloud(false);
            }
        });
    }

    private void createRandomWordCloudButton()
    {
        Button sendButton = Button.wrap(Document.get().getElementById("btn_create_random_wc"));
        sendButton.addClickHandler(new ClickHandler()
        {
            public void onClick(ClickEvent event)
            {
                updateWordCloud(true);
            }
        });
    }

    private void updateWordCloud(boolean useRandomSetting)
    {
        final DialogBox shadow = AppUtils.createShadow();
        shadow.center();
        shadow.show();

        final DialogBox loadingBox = AppUtils.createLoadingBox();
        loadingBox.show();
        loadingBox.center();

        if (useRandomSetting)
            setting.setRandomSetting();

        service.updateWordCloud(id, inputText, setting, new AsyncCallback<WordCloud>()
        {
            public void onSuccess(WordCloud cloud)
            {
                loadingBox.hide();
                shadow.hide();
                
                initializeContentPanel(cloud);
                initializeSettingPanel(cloud);
                //Window.Location.assign("/cloud.html?id=" + result.getId());
            }

            public void onFailure(Throwable caught)
            {
                loadingBox.hide();
                DialogBox errorBox = AppUtils.createErrorBox(caught, shadow);
                errorBox.center();
                errorBox.show();
            }
        });
    }

    private SimplePanel createPanel(String svg, int width, int height)
    {
        SimplePanel panel = new SimplePanel();
        panel.setPixelSize(width, height);
        panel.addStyleName("center");
        HTML html = new HTML(svg);
        html.setWidth("100%");
        html.setHeight("100%");
        panel.add(html);
        return panel;
    }

    private void addSaveAsLinks(WordCloud cloud)
    {
        Anchor link = Anchor.wrap(Document.get().getElementById("save-as-svg"));
        link.setHref("/cloud/download?ft=svg&id=" + cloud.getId());

        Anchor linkPNG = Anchor.wrap(Document.get().getElementById("save-as-png"));
        linkPNG.setHref("/cloud/download?ft=png&id=" + cloud.getId());

        Anchor linkPDF = Anchor.wrap(Document.get().getElementById("save-as-pdf"));
        linkPDF.setHref("/cloud/download?ft=pdf&id=" + cloud.getId());
    }

}