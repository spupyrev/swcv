package edu.webapp.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.VerticalPanel;

import edu.webapp.shared.WordCloud;

/**
 * @author spupyrev
 * Dec 27, 2013
 */
public class WordCloudDialogBox
{
    private DialogBox shadow;
    
    public WordCloudDialogBox(DialogBox shadow)
    {
        this.shadow = shadow;
    }

    public DialogBox create(final WordCloud result)
    {
        // Create a dialog box and set the caption text
        final DialogBox dialogBox = new DialogBox();

        // Create a table to layout the content
        VerticalPanel dialogContents = new VerticalPanel();
        dialogContents.setSpacing(4);

        HorizontalPanel horPanel = new HorizontalPanel();
        horPanel.add(dialogContents);

        // horPanel.add(createMetricsPanel(result));
        dialogBox.setWidget(horPanel);

        HorizontalPanel hp = new HorizontalPanel();
        dialogContents.add(hp);

        // Add a close button at the bottom of the dialog
        Button saveButton = new Button("Save", new ClickHandler()
        {
            public void onClick(ClickEvent event)
            {
                Window.open(GWT.getHostPageBaseURL() + "db/svgs/" + result.getName() + ".svg", "save", "");
            }
        });

        // Add goto share page bottom
        Button goSharePageButton = new Button("Share", new ClickHandler()
        {
            public void onClick(ClickEvent event)
            {
                Window.open(GWT.getHostPageBaseURL() + "db/htmls/" + result.getName() + ".html", "share", "");
            }
        });

        Button closeButton = new Button("Close", new ClickHandler()
        {
            public void onClick(ClickEvent event)
            {
                dialogBox.removeFromParent();
                shadow.removeFromParent();
            }
        });
        saveButton.addStyleName("inconsolataNormal");
        goSharePageButton.addStyleName("inconsolataNormal");
        closeButton.addStyleName("inconsolataNormal");
        hp.add(saveButton);
        hp.add(goSharePageButton);
        hp.add(closeButton);
        dialogContents.setCellHorizontalAlignment(hp, HasHorizontalAlignment.ALIGN_RIGHT);

        // Add an image to the dialog
        HTML html = new HTML(result.getSvg());
        dialogContents.add(html);
        dialogContents.setCellHorizontalAlignment(html, HasHorizontalAlignment.ALIGN_CENTER);
        dialogContents.setCellWidth(html, "" + result.getWidth());
        dialogContents.setCellHeight(html, "" + result.getHeight());

        // Return the dialog box
        return dialogBox;
    }
}
