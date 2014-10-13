package edu.webapp.client.ui;

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Node;
import com.google.gwt.dom.client.NodeList;
import com.google.gwt.dom.client.OptGroupElement;
import com.google.gwt.dom.client.OptionElement;
import com.google.gwt.dom.client.SelectElement;
import com.google.gwt.user.client.ui.ListBox;

/**
 * Extends the standard GWT ListBox to automatically provide OPTGROUP
 * elements to group sections of options.
 * <p>
 * Rather than provide a separate API, it uses the names of the OPTION
 * elements to establish the grouping using a simple syntax. The text
 * before the first "|" character is used as the group name and is
 * used to group elements inside an OPTGROUP of that name. The reset
 * of the text is used as the text of the OPTION.
 * <p>
 * It uses "doubling" for escaping so if "||" appears in the group
 * name, it is converted to a single "|" and the first single "|"
 * after that is used as the delimiter.
 * <p>
 * As a simple example, in a normal listbox I might have:
 * 
 * <pre>
 * - Item 1A
 * - Item 2A
 * - Item 1B
 * - Item 2B
 * </pre>
 * <p>
 * You could imaging using a text prefix to represent groups so that
 * you have:
 * 
 * <pre>
 * - Group A | Item 1
 * - Group A | Item 2
 * - Group B | Item 1
 * - Group B | Item 2
 * </pre>
 * <p>
 * The ListBox would get wide and hard to read. But if you add those
 * same items to a GroupedListBox, it will create OPTGROUPS
 * automatically so that you will have:
 * 
 * <pre>
 * - Group A
 * -- Item 1
 * -- Item 2
 * - Group B
 * -- Item 1
 * -- Item 2
 * </pre>
 * <p>
 * With regard to indexes and selection, it will mostly work the same
 * as a normal ListBox. The one difference is that it will not repeat
 * groups. This means that if you add the items in this order:
 * 
 * <pre>
 * - Group A | Item 1
 * - Group B | Item 1
 * - Group A | Item 2
 * - Group B | Item 2
 * </pre>
 * 
 * then you will rearrange the items to group them:
 * 
 * <pre>
 * - Group A
 * -- Item 1
 * -- Item 2
 * - Group B
 * -- Item 1
 * -- Item 2
 * </pre>
 * 
 * TODO: Remove OPTGROUP when all of the corresponding items are removed.
 * TODO: Test using WebDriver against other browsers (IE!)
 */

public class GroupedListBox extends ListBox
{
    public GroupedListBox()
    {
    }

    public GroupedListBox(boolean isMultipleSelect)
    {
        super(isMultipleSelect);
    }

    @Override
    public void clear()
    {
        super.clear();

        // we need special handling to remove any OPTGROUP elements
        Element elm = getElement();
        while (elm.hasChildNodes())
        {
            elm.removeChild(elm.getFirstChild());
        }
    }

    @Override
    public int getItemCount()
    {
        return super.getItemCount();
    }

    @Override
    public String getItemText(int index)
    {
        return super.getItemText(index);
        //        OptionElement opt = getOption(index);
        //        return (opt != null) ? opt.getInnerText() : null;
    }

    protected OptionElement getOption(int index)
    {
        checkIndex(index);

        // first check ungrouped
        Element elm = getElement();
        int sz = elm.getChildCount();
        int firstGroup = getIndexOfFirstGroup();
        if (index >= 0 && index < firstGroup && index < sz)
        {
            return option(elm.getChild(index));
        }

        // then go through the groups
        int childIndex = index - firstGroup;
        for (int i = firstGroup; i <= index && i < sz; i++)
        {
            Node child = elm.getChild(i);
            if (isGroup(child))
            {
                if (childIndex < child.getChildCount())
                {
                    return option(child.getChild(childIndex));
                }
                else
                {
                    childIndex -= child.getChildCount();
                }
            }
        }
        return null;
    }

    private OptionElement option(Node node)
    {
        if (node == null)
            return null;
        return OptionElement.as(Element.as(node));
    }

    private OptGroupElement optgroup(Node node)
    {
        if (node == null)
            return null;
        return OptGroupElement.as(Element.as(node));
    }

    public int getSelectedIndex()
    {
        return super.getSelectedIndex();
    }

    @Override
    public String getValue(int index)
    {
        return super.getValue(index);
    }

    protected int getIndexOfFirstGroup()
    {
        Element elm = getElement();
        int sz = elm.getChildCount();
        for (int i = 0; i < sz; i++)
        {
            if (isGroup(elm.getChild(i)))
            {
                return i;
            }
        }
        return sz;
    }

    protected boolean isGroup(Node node)
    {
        return "OPTGROUP".equals(node.getNodeName());
    }

    protected boolean isMatchingGroup(Node child, String group)
    {
        if (isGroup(child))
        {
            OptGroupElement optgroup = optgroup(child);
            return group.equals(optgroup.getLabel());
        }
        else
        {
            return false;
        }
    }

    protected OptGroupElement findOptGroupElement(String name)
    {
        if (name == null)
            return null;
        NodeList<Element> optgroups = getElement().getElementsByTagName("OPTGROUP");
        for (int i = 0; i < optgroups.getLength(); i++)
        {
            Element optgroup = optgroups.getItem(i);
            if (isMatchingGroup(optgroup, name))
            {
                return OptGroupElement.as(optgroup);
            }
        }
        return null;
    }

    protected int getIndexInGroup(String group, int index)
    {
        if (group == null)
            return index;

        int adjusted = index;
        Element elm = getElement();
        int sz = elm.getChildCount();
        for (int i = 0; i < sz; i++)
        {
            Node child = elm.getChild(i);
            if (isMatchingGroup(child, group))
            {
                break;
            }
            if (isGroup(child))
            {
                adjusted -= child.getChildCount();
            }
            else
            {
                adjusted -= 1;
            }
        }
        return adjusted;
    }

    protected OptionElement createOption(String item, String value)
    {
        OptionElement option = Document.get().createOptionElement();
        option.setText(item);
        option.setInnerText(item);
        option.setValue(value);
        return option;
    }

    @Override
    public void insertItem(String item, String value, int index)
    {
        // find the delimiter if there is one
        int pipe = (item != null) ? item.indexOf('|') : -1;
        while (pipe != -1 && pipe + 1 != item.length() && item.charAt(pipe + 1) == '|')
        {
            pipe = item.indexOf('|', pipe + 2);
        }

        // extract the group if we found a delimiter
        String group = null;
        if (pipe != -1)
        {
            group = item.substring(0, pipe).trim();
            item = item.substring(pipe + 1).trim();

            // make sure we convert || -> | in the group name
            group = group.replace("||", "|");
        }

        Element parent = getSelectElement();
        Node before = null;

        if (group != null)
        {
            OptGroupElement optgroup = findOptGroupElement(group);
            if (optgroup != null)
            {
                // add it to this optgroup
                parent = optgroup;

                // adjust the index to inside the group
                int adjusted = getIndexInGroup(group, index);

                // we had a real index (wasn't negative which means
                // add to the end), but it was too low for this group.
                // put it at the beginning of the group.
                if (adjusted < 0 && index >= 0)
                {
                    adjusted = 0;
                }

                // check the range and if it's out of range, we'll
                // just add it to the end
                // of the group (before == null)
                if (0 <= adjusted && adjusted < optgroup.getChildCount())
                {
                    before = optgroup.getChild(adjusted);
                }
            }
            else
            {
                // add a new group and add the item to it
                optgroup = Document.get().createOptGroupElement();
                optgroup.setLabel(group);
                parent.appendChild(optgroup);
                parent = optgroup;
                before = null;
            }
        }
        else
        {
            // make sure we're not past the initial "group" of
            // ungrouped options
            int max = getIndexOfFirstGroup();
            if (index < 0 || index > max)
            {
                before = (max < parent.getChildCount()) ? parent.getChild(max) : null;
            }
            else if (0 <= index && index < parent.getChildCount())
            {
                before = parent.getChild(index);
            }
        }

        OptionElement option = createOption(item, value);
        parent.insertBefore(option, before);
    }

    @Override
    public boolean isItemSelected(int index)
    {
        return super.isItemSelected(index);
    }

    @Override
    public void removeItem(int index)
    {
        super.removeItem(index);
    }

    @Override
    public void setItemSelected(int index, boolean selected)
    {
        super.setItemSelected(index, selected);
    }

    @Override
    public void setItemText(int index, String text)
    {
        super.setItemText(index, text);
    }

    @Override
    public void setSelectedIndex(int index)
    {
        super.setSelectedIndex(index);
    }

    @Override
    public void setValue(int index, String value)
    {
        super.setValue(index, value);
    }

    protected SelectElement getSelectElement()
    {
        return getElement().cast();
    }

    protected void checkIndex(int index)
    {
        if (index < 0 || index >= getItemCount())
        {
            throw new IndexOutOfBoundsException();
        }
    }

}