package edu.webapp.server;

import edu.cloudy.utils.CommonUtils;
import edu.cloudy.utils.FontUtils.AWTFontProvider;
import edu.cloudy.utils.SWCRectangle;
import edu.webapp.shared.WCFont;

import java.awt.Font;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;

/**
 * @author spupyrev
 * Aug 26, 2013
 */
public class SVGFontProvider extends AWTFontProvider
{
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

        try
        {
            //is it installed in the system?
            chosen = new Font(wcFont.getName(), Font.PLAIN, 80);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        if (chosen == null)
        {

            try
            {
                //is it in the folder?
                chosen = Font.createFont(Font.TRUETYPE_FONT, new File(CommonUtils.getAbsoluteFileName("fonts/" + wcFont.getName() + ".ttf"))).deriveFont(80.0F);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }

        if (chosen == null)
        {
            //using default font
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
