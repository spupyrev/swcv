package edu.webapp.shared.registry;

import edu.webapp.shared.WCFont;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * @author spupyrev
 * Oct 6, 2014
 */
public class WCFontRegistry implements Serializable
{
    private static final long serialVersionUID = 8054870954525877210L;

    private static List<WCFont> fonts = new ArrayList<WCFont>();

    static
    {
        //websafe fonts
        fonts.add(new WCFont("Arial", "Arial", true, false));
        fonts.add(new WCFont("Comic Sans MS", "Comic Sans MS", true, false));
        fonts.add(new WCFont("Courier New", "Courier", true, false));
        fonts.add(new WCFont("Impact", "Impact", true, false));
        fonts.add(new WCFont("Times New Roman", "Times New Roman", true, false));

        //our new fonts
        fonts.add(new WCFont("Archer", "Archer - UofA Official Font", false, true));
        fonts.add(new WCFont("Crimson", "Crimson - Serif", false, false));
        //fonts.add(new WCFont("Dearest"));
        fonts.add(new WCFont("Eraser", "Eraser - Hand Drawn", false, true));
        //fonts.add(new WCFont("Harting"));
        fonts.add(new WCFont("Inconsolata", "Inconsolata - Monospace", false, true));
        //fonts.add(new WCFont("Kingthings_Gothique", "Kingthings_Gothique - Blackletter", true));
        fonts.add(new WCFont("MaiandraGD", "Maiandra GD", false, true));
        //fonts.add(new WCFont("Monofur", "Monofur - Monospace", false, false));
        fonts.add(new WCFont("Pacifico", "Pacifico - Script", false, true));
        //fonts.add(new WCFont("Porcelai"));
        //fonts.add(new WCFont("Report1942"));
        fonts.add(new WCFont("Stentiga", "Stentiga - Sans Serif", false, true));
        fonts.add(new WCFont("Teen", "Teen - Sans Serif", false, true));
        //fonts.add(new WCFont("Waker"));
        fonts.add(new WCFont("Wetpet", "Wetpet - Funny font", false, true));
    }

    public static List<WCFont> list()
    {
        return fonts;
    }

    public static WCFont getDefault()
    {
        return getByName("Arial");
    }

    public static WCFont getByName(String name)
    {
        for (WCFont font : fonts)
            if (font.getName().equals(name))
                return font;

        throw new RuntimeException("font with name '" + name + "' not found");
    }

    public static WCFont getRandom()
    {
        return fonts.get(new Random().nextInt(fonts.size()));
    }
}
