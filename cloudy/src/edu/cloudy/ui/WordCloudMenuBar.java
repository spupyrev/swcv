package edu.cloudy.ui;

import org.apache.batik.dom.GenericDOMImplementation;
import org.apache.batik.svggen.SVGGraphics2D;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.KeyStroke;
import javax.swing.filechooser.FileFilter;

import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;

/**
 * @author spupyrev
 * May 3, 2013
 */
public class WordCloudMenuBar extends JMenuBar
{
    private static final long serialVersionUID = 1L;

    public WordCloudMenuBar(JPanel panel)
    {
        add(createFileMenu(panel));

        if (panel instanceof WordCloudPanel)
            add(createSettingsMenu((WordCloudPanel)panel));
    }

    public JMenu createFileMenu(final JPanel panel)
    {
        JMenu fileMenu = new JMenu();
        fileMenu.setText("File");

        //SaveAs png
        Action exportPNGAction = new AbstractAction("save as png")
        {
            private static final long serialVersionUID = 1L;

            public void actionPerformed(ActionEvent e)
            {
                String selectedFile = chooseOpenSaveFile(getParent(), new PNGFileFilter());
                if (selectedFile != null)
                {
                    if (!selectedFile.endsWith(".png"))
                    {
                        selectedFile = selectedFile.concat(".png");
                    }

                    exportPNG(panel, selectedFile);
                }
            }

        };
        configAction(exportPNGAction, "control S", 'z');
        fileMenu.add(exportPNGAction);

        //SaveAs svg
        Action exportSVGAction = new AbstractAction("save as svg")
        {
            private static final long serialVersionUID = 1L;

            public void actionPerformed(ActionEvent e)
            {
                String selectedFile = chooseOpenSaveFile(getParent(), new SVGFileFilter());
                if (selectedFile != null)
                {
                    if (!selectedFile.endsWith(".svg"))
                    {
                        selectedFile = selectedFile.concat(".svg");
                    }

                    exportSVG(panel, selectedFile);
                }
            }
        };
        configAction(exportSVGAction, "control D", 'z');
        fileMenu.add(exportSVGAction);

        fileMenu.add(new JSeparator());

        //eXit
        JMenuItem exitMenuItem = new JMenuItem();
        exitMenuItem.setText("Exit");
        exitMenuItem.setMnemonic('x');
        exitMenuItem.setAction(new AbstractAction("Exit")
        {
            private static final long serialVersionUID = 1L;

            public void actionPerformed(ActionEvent e)
            {
                System.exit(0);
            }
        });
        fileMenu.add(exitMenuItem);

        return fileMenu;
    }

    public JMenu createSettingsMenu(final WordCloudPanel panel)
    {
        JMenu menu = new JMenu();
        menu.setText("Settings");

        //Show edges
        final String showEdgesActionName = "hide borders";
        final Action showEdgesAction = new AbstractAction(showEdgesActionName)
        {
            private static final long serialVersionUID = 1L;

            public void actionPerformed(ActionEvent e)
            {
                Object value = getValue(showEdgesActionName);
                if (value == null || value.equals("false"))
                {
                    putValue(showEdgesActionName, "true");
                    putValue(Action.NAME, "show borders");
                    panel.setShowRectangles(false);
                }
                else
                {
                    putValue(showEdgesActionName, "false");
                    putValue(Action.NAME, "hide borders");
                    panel.setShowRectangles(true);
                }
                panel.repaint();
            }
        };
        configAction(showEdgesAction, "alt B", 'z');
        menu.add(showEdgesAction);

        //show words
        final String showWordsActionName = "hide words";
        final Action showWordsAction = new AbstractAction(showWordsActionName)
        {
            private static final long serialVersionUID = 1L;

            public void actionPerformed(ActionEvent e)
            {
                Object value = getValue(showWordsActionName);
                if (value == null || value.equals("false"))
                {
                    putValue(showWordsActionName, "true");
                    putValue(Action.NAME, "show words");
                    panel.setShowWords(false);
                }
                else
                {
                    putValue(showWordsActionName, "false");
                    putValue(Action.NAME, "hide words");
                    panel.setShowWords(true);
                }
                panel.repaint();
            }
        };
        configAction(showWordsAction, "alt W", 'z');
        menu.add(showWordsAction);
        if (!panel.isShowWords())
            showWordsAction.actionPerformed(null);

        // show adjacencies
        final String showAdjacenciesName = "hide adjacencies";
        final Action showAdjacenciesAction = new AbstractAction(showAdjacenciesName)
        {
            private static final long serialVersionUID = 1L;

            public void actionPerformed(ActionEvent e)
            {
                Object value = getValue(showAdjacenciesName);
                if (value == null || value.equals("false"))
                {
                    putValue(showAdjacenciesName, "true");
                    putValue(Action.NAME, "show adjacencies");
                    panel.setShowAdjacencies(false);
                }
                else
                {
                    putValue(showAdjacenciesName, "false");
                    putValue(Action.NAME, "hide adjacencies");
                    panel.setShowAdjacencies(true);
                }
                panel.repaint();
            }
        };
        configAction(showAdjacenciesAction, "alt A", 'z');
        menu.add(showAdjacenciesAction);
        if (!panel.isShowAdjacencies())
            showAdjacenciesAction.actionPerformed(null);

        // show adjacencies
        final String showProximityName = "hide proximity";
        final Action showProximityAction = new AbstractAction(showProximityName)
        {
            private static final long serialVersionUID = 1L;

            public void actionPerformed(ActionEvent e)
            {
                Object value = getValue(showProximityName);
                if (value == null || value.equals("false"))
                {
                    putValue(showProximityName, "true");
                    putValue(Action.NAME, "show proximity");
                    panel.setShowProximity(false);
                }
                else
                {
                    putValue(showProximityName, "false");
                    putValue(Action.NAME, "hide proximity");
                    panel.setShowProximity(true);
                }
                panel.repaint();
            }
        };
        configAction(showProximityAction, "alt P", 'z');
        menu.add(showProximityAction);
        if (!panel.isShowProximity())
            showProximityAction.actionPerformed(null);

        //Show convex hull
        final String showConvexHull = "show convex hull";
        final Action showConvexHullAction = new AbstractAction(showConvexHull)
        {
            private static final long serialVersionUID = 1L;

            public void actionPerformed(ActionEvent e)
            {
                Object value = getValue(showConvexHull);
                if (value == null || value.equals("false"))
                {
                    putValue(showConvexHull, "true");
                    putValue(Action.NAME, "hide convex hull");
                    panel.setShowConvexHull(true);
                }
                else
                {
                    putValue(showConvexHull, "false");
                    putValue(Action.NAME, "show convex hull");
                    panel.setShowConvexHull(false);
                }
                panel.repaint();
            }
        };
        configAction(showConvexHullAction, "alt H", 'z');
        menu.add(showConvexHullAction);

        return menu;
    }

    private void configAction(Action a, String acc, char mn)
    {
        a.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(acc));
        a.putValue(Action.MNEMONIC_KEY, new Integer(Character.getNumericValue(mn) + 55));
    }

    private String chooseOpenSaveFile(Component parent, FileFilter fileFilter)
    {
        JFileChooser chooser = getOpenSaveJFileChooser(fileFilter);

        int returnVal = chooser.showSaveDialog(parent);
        if (returnVal == JFileChooser.APPROVE_OPTION)
        {
            return chooser.getSelectedFile().getAbsolutePath();
        }

        return null;
    }

    private JFileChooser openSaveFileChooser;

    private JFileChooser getOpenSaveJFileChooser(FileFilter fileFilter)
    {
        if (openSaveFileChooser == null)
        {
            openSaveFileChooser = new JFileChooser(getStartDirectory());
            openSaveFileChooser.setAcceptAllFileFilterUsed(false);
            openSaveFileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        }
        openSaveFileChooser.setFileFilter(fileFilter);

        return openSaveFileChooser;
    }

    private String getStartDirectory()
    {
        return System.getProperty("user.dir") + "/resources";
    }

    private void exportPNG(final JPanel panel, String selectedFile)
    {
        BufferedImage img = new BufferedImage(panel.getWidth(), panel.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = img.createGraphics();
        panel.printAll(g2d);
        g2d.dispose();
        try
        {
            ImageIO.write(img, "png", new File(selectedFile));
        }
        catch (IOException ex)
        {
            ex.printStackTrace();
        }
    }

    private void exportSVG(final JPanel panel, String selectedFile)
    {
        // Get a DOMImplementation.
        DOMImplementation domImpl = GenericDOMImplementation.getDOMImplementation();

        // Create an instance of org.w3c.dom.Document.
        String svgNS = "http://www.w3.org/2000/svg";
        Document document = domImpl.createDocument(svgNS, "svg", null);

        // Create an instance of the SVG Generator.
        SVGGraphics2D svgGenerator = new SVGGraphics2D(document);

        // Ask to render into the SVG Graphics2D implementation.
        if (panel instanceof FlexWordlePanel)
            ((FlexWordlePanel)panel).draw(svgGenerator, panel.getWidth(), panel.getHeight());
        else
            panel.paint(svgGenerator);

        // Finally, stream out SVG to the standard output using
        // UTF-8 encoding.
        boolean useCSS = true; // we want to use CSS style attributes
        Writer out;
        try
        {
            out = new OutputStreamWriter(new FileOutputStream(selectedFile), "UTF-8");
            svgGenerator.stream(out, useCSS);
            out.close();
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    public static class PNGFileFilter extends FileFilter
    {
        @Override
        public boolean accept(java.io.File file)
        {
            return file.isDirectory() || file.getName().endsWith(".png");
        }

        @Override
        public String getDescription()
        {
            return "Standard PNG Image writer (.png)";
        }
    }

    public static class SVGFileFilter extends FileFilter
    {
        @Override
        public boolean accept(java.io.File file)
        {
            return file.isDirectory() || file.getName().endsWith(".svg");
        }

        @Override
        public String getDescription()
        {
            return "SVG (*.svg)";
        }
    }
}
