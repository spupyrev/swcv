package edu.webapp.server;

import edu.cloudy.utils.FontUtils;
import edu.webapp.shared.WCFont;

import org.apache.batik.svggen.DefaultStyleHandler;
import org.apache.batik.svggen.SVGGeneratorContext;
import org.apache.batik.svggen.SVGSyntax;
import org.w3c.dom.CDATASection;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.svg.SVGSVGElement;

import java.awt.Font;
import java.util.Map;

/**
 * @author spupyrev
 * Oct 8, 2014
 */
public class TextStyleHandler extends DefaultStyleHandler
{
    @Override
    public void setStyle(Element element, Map styleMap, SVGGeneratorContext generatorContext)
    {
        super.setStyle(element, styleMap, generatorContext);

        if ("text".equals(element.getNodeName()))
        {
            element.setAttribute("class", "svg_word");
            ((Element)element.getParentNode()).setAttribute("class", "svg_g");
        }
    }

    public static void appendFontCSS(SVGSVGElement root, WCFont wcFont)
    {
        if (wcFont.isWebSafe())
            return;

        Document document = root.getOwnerDocument();
        Element defs = root.getElementById(SVGSyntax.ID_PREFIX_GENERIC_DEFS);
        Element style = document.createElementNS(SVGSyntax.SVG_NAMESPACE_URI, SVGSyntax.SVG_STYLE_TAG);
        style.setAttributeNS(null, SVGSyntax.SVG_TYPE_ATTRIBUTE, "text/css");
        CDATASection styleSheet = document.createCDATASection("");

        Font font = FontUtils.getFont();
        styleSheet.setData("@font-face { font-family: '" + font.getFamily() + "'; src: url('static/fonts/" + wcFont.getName() + ".ttf');}");
        style.appendChild(styleSheet);
        defs.appendChild(style);
    }
}
