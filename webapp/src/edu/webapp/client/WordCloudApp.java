package edu.webapp.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Document;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.*;

import edu.webapp.shared.WCSetting;
import edu.webapp.shared.WordCloud;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class WordCloudApp implements EntryPoint
{
    /**
     * Create a remote service proxy to talk to the server-side Greeting
     * service.
     */
    private final WordCloudServiceAsync wcService = GWT.create(WordCloudService.class);
    private WCSetting setting = new WCSetting();

    /**
     * This is the entry point method.
     */
    public void onModuleLoad()
    {
        TextArea textArea = createTextArea();

        createCreateWordCloudButton(textArea);

        createLuckyButtons();

        createAdvancedArea();

        createShowAdvancedButton();
    }

    private void createShowAdvancedButton()
    {
        final Anchor showAdvancedButton = Anchor.wrap(Document.get().getElementById("adv_link"));
        final Panel settingArea = RootPanel.get("settingContainer");
        showAdvancedButton.addClickHandler(new ClickHandler()
        {
            public void onClick(ClickEvent event)
            {
                if (showAdvancedButton.getText().equals("Show Advanced Options"))
                {
                    settingArea.removeStyleName("hide");
                    showAdvancedButton.setText("Hide Advanced Options");
                }
                else
                {
                    settingArea.addStyleName("hide");
                    showAdvancedButton.setText("Show Advanced Options");
                }
            }
        });
    }

    private void createLuckyButtons()
    {
        createLuckyWikiButton();
        createLuckyTwitterButton();
        createLuckyYoutubeButton();
    }

    private void createLuckyWikiButton()
    {
        Anchor rndWikiButton = Anchor.wrap(Document.get().getElementById("btn_rnd_wiki"));
        final TextArea textArea = TextArea.wrap(Document.get().getElementById("input_text"));
        rndWikiButton.addClickHandler(new ClickHandler()
        {
            public void onClick(ClickEvent event)
            {
                wcService.getRandomWikiUrl(new AsyncCallback<String>()
                {
                    public void onSuccess(String result)
                    {
                        textArea.setText(result);
                    }

                    public void onFailure(Throwable caught)
                    {
                        textArea.setText("http://en.wikipedia.org/wiki/Special:random");
                    }
                });

            }
        });
    }

    private void createLuckyTwitterButton()
    {
        Anchor rndWikiButton = Anchor.wrap(Document.get().getElementById("btn_rnd_twitter"));
        final TextArea textArea = TextArea.wrap(Document.get().getElementById("input_text"));
        rndWikiButton.addClickHandler(new ClickHandler()
        {
            public void onClick(ClickEvent event)
            {
                wcService.getRandomTwitterUrl(new AsyncCallback<String>()
                {
                    public void onSuccess(String result)
                    {
                        textArea.setText(result);
                    }

                    public void onFailure(Throwable caught)
                    {
                        textArea.setText("twitter: twitter");
                    }
                });

            }
        });
    }

    private void createLuckyYoutubeButton()
    {
        Anchor rndWikiButton = Anchor.wrap(Document.get().getElementById("btn_rnd_youtube"));
        final TextArea textArea = TextArea.wrap(Document.get().getElementById("input_text"));
        rndWikiButton.addClickHandler(new ClickHandler()
        {
            public void onClick(ClickEvent event)
            {
                wcService.getRandomYoutubeUrl(new AsyncCallback<String>()
                {
                    public void onSuccess(String result)
                    {
                        textArea.setText(result);
                    }

                    public void onFailure(Throwable caught)
                    {
                        textArea.setText("https://www.youtube.com");
                    }
                });

            }
        });
    }

    private void createAdvancedArea()
    {
        final CaptionPanel settingArea = new SettingsPanel(setting, true).create();
        settingArea.removeStyleName("gwt-DecoratorPanel");
        RootPanel.get("settingContainer").add(settingArea);
    }

    private void createCreateWordCloudButton(final TextArea textArea)
    {
        Button sendButton = Button.wrap(Document.get().getElementById("btn_create_wc"));
        sendButton.addClickHandler(new ClickHandler()
        {
            public void onClick(ClickEvent event)
            {
                createWordCloud();
            }
        });
    }

    private TextArea createTextArea()
    {
        TextArea textArea = TextArea.wrap(Document.get().getElementById("input_text"));
        textArea.addKeyDownHandler(new KeyDownHandler()
        {
            public void onKeyDown(KeyDownEvent event)
            {
                event.preventDefault();
                if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER)
                {
                    createWordCloud();
                }
            }
        });

        return textArea;
    }

    private void createWordCloud()
    {
        TextArea textArea = TextArea.wrap(Document.get().getElementById("input_text"));
        String text = textArea.getText().trim();
        if (!text.isEmpty())
        {
            createWordCloud(text);
        }
        else
        {
            textArea.setFocus(true);
        }
    }

    private void createWordCloud(String text)
    {
        final DialogBox shadow = AppUtils.createShadow();
        shadow.center();
        shadow.show();

        final DialogBox loadingBox = AppUtils.createLoadingBox();
        loadingBox.show();
        loadingBox.center();

        wcService.buildWordCloud(text, setting, new AsyncCallback<WordCloud>()
        {
            public void onSuccess(WordCloud result)
            {
                loadingBox.hide();
                shadow.hide();
                Window.Location.assign("/cloud.html?id=" + result.getId());
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
}