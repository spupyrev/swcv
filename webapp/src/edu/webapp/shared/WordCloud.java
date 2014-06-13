package edu.webapp.shared;

import com.google.gwt.i18n.shared.DateTimeFormat;
import com.google.gwt.i18n.shared.DefaultDateTimeFormatInfo;

import java.io.Serializable;
import java.util.Date;

/**
 * @author spupyrev
 * Aug 17, 2013
 */
public class WordCloud implements Serializable
{
    private static final long serialVersionUID = 8810113025963123088L;

    private int id;

    private String inputText;
    private String sourceText;
    private WCSetting settings;

    private String creationDate;
    private String svg;
    private String svg2;
    private int width;
    private int height;

    private WCMetrics metrics;
    
    private String creatorIP;

	private int width2;

	private int height2;

    public WordCloud()
    {
    	svg2 = "";
    }

    public String getCreatorIP()
    {
        return creatorIP;
    }

    public void setCreatorIP(String creatorIP)
    {
        this.creatorIP = creatorIP;
    }

    public int getId()
    {
        return id;
    }

    public void setId(int id)
    {
        this.id = id;
    }

    public String getInputText()
    {
        return inputText;
    }

    public void setInputText(String inputText)
    {
        this.inputText = inputText;
    }

    public WCSetting getSettings()
    {
        return settings;
    }

    public void setSettings(WCSetting settings)
    {
        this.settings = settings;
    }

    public String getCreationDate()
    {
        return creationDate;
    }

    public void setCreationDate(String creationDate)
    {
        this.creationDate = creationDate;
    }

    public String getSvg()
    {
        return svg;
    }

    public void setSvg(String svg)
    {
        this.svg = svg;
    }

    public String getSvg2()
    {
        return svg2;
    }

    public void setSvg2(String svg)
    {
        this.svg2 = svg;
    }
    
    public int getWidth()
    {
        return width;
    }

    public void setWidth(int width)
    {
        this.width = width;
    }

    public int getHeight()
    {
        return height;
    }

    public void setHeight(int height)
    {
        this.height = height;
    }

    public WCMetrics getMetrics()
    {
        return metrics;
    }

    public void setMetrics(WCMetrics metrics)
    {
        this.metrics = metrics;
    }

    private static final String DATE_FORMAT = "yyyyMMddHHmmssS";
    private static final DefaultDateTimeFormatInfo info = new DefaultDateTimeFormatInfo();
    private static final DateTimeFormat dtf = new DateTimeFormat(WordCloud.DATE_FORMAT, info) {};
    
    public Date getCreationDateAsDate()
    {
        return dtf.parse(creationDate);
    }

    public void setCreationDateAsDate(Date creationDate)
    {
        this.creationDate = dtf.format(creationDate);
    }

	public String getSourceText()
	{
		return sourceText;
	}

	public void setSourceText(String sourceText)
	{
		this.sourceText = sourceText;
	}

	public void setWidth2(int width2)
	{
		this.width2 = width2;
	}

	public int getWidth2(){
		return this.width2;
	}
	
	public void setHeight2(int height2)
	{
		this.height2 = height2;
	}
	
	public int getHeight2(){
		return this.height2;
	}

}
