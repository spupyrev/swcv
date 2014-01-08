package com.swcwebapp.server;

import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import de.tinloaf.cloudy.utils.FontUtils.AWTFontProvider;
import de.tinloaf.cloudy.utils.SWCRectangle;

/**
 * @author spupyrev
 * Aug 26, 2013
 */
public class SVGFontProvider extends AWTFontProvider
{
    private String FontName;
    
    // FontUtils.initialize(new SVGFontProvider("ActionMan"));
    
    public SVGFontProvider(String FontName) {
        this.FontName = FontName+".ttf";
    }
    
    private String getAbsoluteFileName(String name) {
        return Thread.currentThread().getContextClassLoader().getResource("fonts"+File.separator+name).getFile();
    }
    
    public Font getFont(){
        Font chosen = null;
        
        try {
            chosen = Font.createFont(Font.TRUETYPE_FONT, new File(getAbsoluteFileName(FontName))).deriveFont(100.0F);
        } catch (FontFormatException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        if (chosen == null){
            chosen = new BufferedImage(1000, 1000, BufferedImage.TYPE_3BYTE_BGR).getGraphics().getFont();
        }
        return chosen;
    }
    
    public SWCRectangle getBoundingBox(String text)
    {
        Font font;
        font = getFont();
        FontRenderContext frc = new FontRenderContext(font.getTransform(),true,false);

        GlyphVector gv = font.layoutGlyphVector(frc, text.toCharArray(), 0, text.length(), Font.LAYOUT_LEFT_TO_RIGHT);
        Rectangle2D bb = gv.getPixelBounds(frc, 0, 0);
        
        return new SWCRectangle(bb.getX(), bb.getY(), bb.getWidth(), bb.getHeight());
    }
}
