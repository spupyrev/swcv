package com.swcwebapp.server;

import de.tinloaf.cloudy.utils.CommonUtils;
import de.tinloaf.cloudy.utils.FontUtils.AWTFontProvider;
import de.tinloaf.cloudy.utils.SWCRectangle;

import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * @author spupyrev
 * Aug 26, 2013
 */
public class SVGFontProvider extends AWTFontProvider
{
    private String fontName;
    private Font font;

    public SVGFontProvider(String fontName)
    {
        this.fontName = fontName + ".ttf";
    }

    public Font getFont()
    {
        if (font == null)
            font = initFont();

        return font;
    }

    public Font initFont()
    {
        Font chosen = null;

        try
        {
            chosen = Font.createFont(Font.TRUETYPE_FONT, new File(CommonUtils.getAbsoluteFileName("fonts/" + fontName))).deriveFont(100.0F);
        }
        catch (FontFormatException e)
        {
            e.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        if (chosen == null)
        {
            chosen = new BufferedImage(1000, 1000, BufferedImage.TYPE_3BYTE_BGR).getGraphics().getFont();
        }
        return chosen;
    }

    public SWCRectangle getBoundingBox(String text)
    {
        Font font;
        font = getFont();
        FontRenderContext frc = new FontRenderContext(font.getTransform(), true, false);

        GlyphVector gv = font.layoutGlyphVector(frc, text.toCharArray(), 0, text.length(), Font.LAYOUT_LEFT_TO_RIGHT);
        Rectangle2D bb = gv.getPixelBounds(frc, 0, 0);

        return new SWCRectangle(bb.getX(), bb.getY(), bb.getWidth(), bb.getHeight());
    }

}
