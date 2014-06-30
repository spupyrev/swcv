package edu.cloudy.ui;

import edu.cloudy.colors.IColorScheme;
import edu.cloudy.layout.LayoutAlgo;
import edu.cloudy.nlp.Word;

import javax.swing.JPanel;
import javax.swing.Timer;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

public class WordCloudPanel extends JPanel implements ActionListener
{
    private static final long serialVersionUID = -3332798140563946847L;

    private WordCloudRendererOld renderer;
    private Timer timer;

    public WordCloudPanel(List<Word> words, LayoutAlgo algo, IColorScheme colorScheme)
    {
        renderer = new WordCloudRendererOld(words, algo, colorScheme, 1024, 800);
        setBackground(Color.WHITE);
        timer = new Timer(100, this);
    }

    @Override
    public void paintComponent(Graphics g)
    {
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D)g;

        if (!timer.isRunning())
        {
            renderer.render(g2);;
            //timer.start();
        }
    }

    @Override
    public void actionPerformed(ActionEvent e)
    {
        this.repaint();
        this.revalidate();
    }
    
    public void setShowRectangles(boolean showRectangles)
    {
        renderer.setShowRectangles(showRectangles);
    }

    public void setShowAdjacencies(boolean set)
    {
        renderer.setShowAdjacencies(set);
    }

    public void setShowProximity(boolean set)
    {
        renderer.setShowProximity(set);
    }

    public void setShowConvexHull(boolean showConvexHull)
    {
        renderer.setShowConvexHull(showConvexHull);
    }

    public void setShowWords(boolean b)
    {
        renderer.setShowWords(b);
    }

    public boolean isShowRectangles()
    {
        return renderer.isShowRectangles();
    }

    public boolean isShowConvexHull()
    {
        return renderer.isShowConvexHull();
    }

    public boolean isShowAdjacencies()
    {
        return renderer.isShowAdjacencies();
    }

    public boolean isShowProximity()
    {
        return renderer.isShowProximity();
    }

    public boolean isShowWords()
    {
        return renderer.isShowWords();
    }
}
