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
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.VerticalPanel;

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
				setting = cloud.getSettings();
				inputText = cloud.getInputText();
				CaptionPanel settingArea = new SettingsPanel(setting, true).create();
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
		createCreateWordCloudButton();
	}

	private void createCreateWordCloudButton()
	{
		Button sendButton = Button.wrap(Document.get().getElementById("btn_create_new_wc"));
		sendButton.addClickHandler(new ClickHandler()
		{
			public void onClick(ClickEvent event)
			{
				createWordCloud();
			}
		});
	}

	private void createWordCloud()
	{
		final DialogBox shadow = AppUtils.createShadow();
		shadow.center();
		shadow.show();

		final DialogBox loadingBox = createLoadingBox();
		loadingBox.show();
		loadingBox.center();
		service.createWordCloud(inputText, setting, new AsyncCallback<WordCloud>()
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

	private DialogBox createLoadingBox()
	{
		final DialogBox box = new DialogBox();
		VerticalPanel rows = new VerticalPanel();
		rows.setSpacing(1);

		HTML html = new HTML("<img src=\"" + GWT.getHostPageBaseURL() + "static/imgs/loader.gif\" alt=\"loading\" />");
		rows.add(html);
		rows.addStyleName("whiteWithBorder");
		rows.setCellHeight(html, "100");
		rows.setCellWidth(html, "300");

		rows.setCellHorizontalAlignment(html, HasHorizontalAlignment.ALIGN_CENTER);
		rows.setCellVerticalAlignment(html, HasVerticalAlignment.ALIGN_MIDDLE);

		HorizontalPanel hp = new HorizontalPanel();
		hp.add(rows);
		box.setWidget(hp);
		box.hide();
		return box;
	}
}