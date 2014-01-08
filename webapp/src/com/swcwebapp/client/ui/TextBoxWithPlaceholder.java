package com.swcwebapp.client.ui;

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.Widget;

/**
 * A text box that displays a placeholder string when empty
 * 
 * <h3>CSS Style Rules</h3>
 * <ul class='css'>
 * <li>.wogwt-TextBoxWithPlaceholder { primary style }</li>
 * <li>.wogwt-TextBoxWithPlaceholder-placeholder { dependent style set when the placeholder
 * is displayed }</li>
 * </ul>
 */
public class TextBoxWithPlaceholder extends TextArea
{

    /**
     * Creates a TextBoxWithPlaceholder widget that wraps an existing &lt;input
     * type='text'&gt; element.
     * 
     * This element must already be attached to the document. If the element is
     * removed from the document, you must call
     * {@link RootPanel#detachNow(Widget)}.
     * 
     * @param element the element to be wrapped
     */
    public static TextBoxWithPlaceholder wrap(Element element)
    {
        // Assert that the element is attached.
        assert Document.get().getBody().isOrHasChild(element);

        TextBoxWithPlaceholder textBox = new TextBoxWithPlaceholder(element);

        // Mark it attached and remember it for cleanup.
        textBox.onAttach();
        RootPanel.detachOnWindowClose(textBox);

        return textBox;
    }

    private String placeholder = null;
    private boolean isPlaceHolderVisible = false;

    /**
     * Creates an empty password text box.
     */
    public TextBoxWithPlaceholder()
    {
        this(DOM.createTextArea());
    }

    /**
     * This constructor may be used by subclasses to explicitly use an existing
     * element. This element must be an &lt;input&gt; element whose type is
     * 'text'.
     * 
     * @param element the element to be used
     */
    protected TextBoxWithPlaceholder(Element element)
    {
        super(element);
        setupHandlers();
    }

    @Override
    public String getText()
    {
        if (isPlaceHolderVisible)
        {
            return "";
        }
        else
        {
            return super.getText();
        }
    }

    @Override
    public void setText(String text)
    {
        super.setText(text);

        if (text == null || text.length() == 0)
        {
            showPlaceholder();
        }
        else
        {
            hidePlaceholder(text);
        }
    }

    public String getPlaceholder()
    {
        return placeholder;
    }

    public void setPlaceholder(String value)
    {
        if (isPlaceHolderVisible && value != null && !value.equals(placeholder))
        {
            // clear the text so the new placeholder will be displayed
            super.setText("");
        }

        placeholder = value;

        showPlaceholder();
    }

    private void setupHandlers()
    {
        addFocusHandler(new FocusHandler()
        {
            public void onFocus(FocusEvent event)
            {
                hidePlaceholder();
            }
        });

        addBlurHandler(new BlurHandler()
        {
            public void onBlur(BlurEvent event)
            {
                showPlaceholder();
            }
        });
    }

    private void showPlaceholder()
    {
        if (super.getText().equals("") && getPlaceholder() != null)
        {
            super.setText(getPlaceholder());
            isPlaceHolderVisible = true;
        }
    }

    private void hidePlaceholder(String newText)
    {
        if (isPlaceHolderVisible)
        {
            super.setText(newText);
            isPlaceHolderVisible = false;
        }
    }

    private void hidePlaceholder()
    {
        hidePlaceholder("");
    }

}