package edu.webapp.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * @author spupyrev
 * Jan 15, 2014
 */
public class AppUtils
{
    public static DialogBox createShadow()
    {
        final DialogBox box = new DialogBox();
        VerticalPanel rows = new VerticalPanel();
        rows.setSpacing(1);
        HTML html = new HTML("<div></div>");
        rows.add(html);
        rows.addStyleName("blackTransparent");
        rows.setCellHeight(html, "" + Window.getClientHeight());
        rows.setCellWidth(html, "" + Window.getClientWidth());

        rows.setCellHorizontalAlignment(html, HasHorizontalAlignment.ALIGN_CENTER);
        rows.setCellVerticalAlignment(html, HasVerticalAlignment.ALIGN_MIDDLE);

        HorizontalPanel hp = new HorizontalPanel();
        hp.add(rows);
        box.setWidget(hp);
        return box;
    }

    public static DialogBox createLoadingBox()
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

    public static DialogBox createErrorBox(Throwable caught, final DialogBox shadow)
    {
        String message = "An error occurred while attempting to contact the server:<br/>" + caught.getClass() + ": " + caught.getMessage();
        return createMessageBox(message, shadow);
    }

    public static DialogBox createMessageBox(String message, final DialogBox shadow)
    {
        // Create a dialog box and set the caption text
        final DialogBox dialogBox = new DialogBox();

        // Create a table to layout the content
        VerticalPanel dialogContents = new VerticalPanel();
        dialogContents.setSpacing(4);
        dialogBox.setWidget(dialogContents);

        // Add an image to the dialog
        HTML html = new HTML(message);
        dialogContents.add(html);
        dialogContents.setCellHorizontalAlignment(html, HasHorizontalAlignment.ALIGN_CENTER);

        // Add a close button at the bottom of the dialog
        Button closeButton = new Button("Close", new ClickHandler()
        {
            public void onClick(ClickEvent event)
            {
                if (shadow != null)
                    shadow.hide();
                dialogBox.hide();
            }
        });

        dialogContents.add(closeButton);
        dialogContents.setCellHorizontalAlignment(closeButton, HasHorizontalAlignment.ALIGN_RIGHT);
        dialogBox.addStyleName("errorBox inconsolataNormal");
        // Return the dialog box
        return dialogBox;
    }
    
    public static void onFailure(Throwable caught)
    {
        DialogBox errorBox = AppUtils.createErrorBox(caught, null);
        errorBox.center();
        errorBox.show();
    }
}
