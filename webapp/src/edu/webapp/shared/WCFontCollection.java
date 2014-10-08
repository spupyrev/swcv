package edu.webapp.shared;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * @author spupyrev
 * Oct 6, 2014
 */
public class WCFontCollection implements Serializable
{
    private static final long serialVersionUID = 8054870954525877210L;
    
    private static List<WCFont> fonts = new ArrayList<WCFont>();

    static
    {
        fonts.add(new WCFont("Arial", "Arial", false));
        fonts.add(new WCFont("Comic Sans MS", "Comic Sans MS", false));
        fonts.add(new WCFont("Courier New", "Courier", false));
        fonts.add(new WCFont("Impact", "Impact", false));
        fonts.add(new WCFont("Times New Roman", "Times New Roman", false));
        
        fonts.add(new WCFont("Archer", "Archer - UofA Official Font", true));
        fonts.add(new WCFont("Crimson", "Crimson - Serif", false));
        //fonts.add(new WCFont("Dearest"));
        fonts.add(new WCFont("Eraser", "Eraser - Hand Drawn", true));
        //fonts.add(new WCFont("Harting"));
        fonts.add(new WCFont("Inconsolata", "Inconsolata - Monospace", true));
        //fonts.add(new WCFont("Kingthings_Gothique", "Kingthings_Gothique - Blackletter", true));
        fonts.add(new WCFont("MaiandraGD", "Maiandra GD", true));
        fonts.add(new WCFont("Monofur", "Monofur - Monospace", false));
        fonts.add(new WCFont("Pacifico", "Pacifico - Script", true));
        //fonts.add(new WCFont("Porcelai"));
        //fonts.add(new WCFont("Report1942"));
        fonts.add(new WCFont("Stentiga", "Stentiga - Sans Serif", true));
        fonts.add(new WCFont("Teen", "Teen - Sans Serif", true));
        //fonts.add(new WCFont("Waker"));
        fonts.add(new WCFont("Wetpet", "Wetpet - Funny font", true));
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
