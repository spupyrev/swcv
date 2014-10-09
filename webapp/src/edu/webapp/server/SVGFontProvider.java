package edu.webapp.server;

import edu.cloudy.geom.SWCRectangle;
import edu.cloudy.utils.CommonUtils;
import edu.cloudy.utils.FontUtils;
import edu.cloudy.utils.FontUtils.AWTFontProvider;
import edu.webapp.shared.WCFont;

import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.HashSet;
import java.util.Set;

/**
 * @author spupyrev
 * Aug 26, 2013
 */
public class SVGFontProvider extends AWTFontProvider
{
    private static Set<String> installedFonts;

    private WCFont wcFont;
    private Font font;

    public SVGFontProvider(WCFont wcFont)
    {
        this.wcFont = wcFont;
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

        if (isFontIstalled(wcFont.getName()))
        {
            chosen = new Font(wcFont.getName(), Font.PLAIN, (int)FontUtils.DEFAULT_FONT_SIZE);
        }
        else
        {
            try
            {
                //is it in the folder?
                File fontFile = new File(CommonUtils.getAbsoluteFileName("fonts/" + wcFont.getName() + ".ttf"));
                chosen = Font.createFont(Font.TRUETYPE_FONT, fontFile).deriveFont(FontUtils.DEFAULT_FONT_SIZE);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }

        if (chosen == null)
        {
            //using default font
            chosen = new BufferedImage(1024, 1024, BufferedImage.TYPE_3BYTE_BGR).getGraphics().getFont();
        }
        return chosen;
    }

    private boolean isFontIstalled(String name)
    {
        if (installedFonts == null)
        {
            installedFonts = new HashSet<String>();
            GraphicsEnvironment g = null;
            g = GraphicsEnvironment.getLocalGraphicsEnvironment();
            for (String fontName : g.getAvailableFontFamilyNames())
                installedFonts.add(fontName);
        }

        return installedFonts.contains(name);
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
