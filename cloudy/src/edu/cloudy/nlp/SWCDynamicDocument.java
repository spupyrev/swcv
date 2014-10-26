package edu.cloudy.nlp;

import edu.cloudy.colors.ColorScheme;
import edu.cloudy.colors.DynamicColorScheme;
import edu.cloudy.geom.BoundingBoxGenerator;
import edu.cloudy.geom.SWCRectangle;
import edu.cloudy.layout.LayoutResult;
import edu.cloudy.nlp.ranking.RankingAlgo;
import edu.cloudy.render.UIWord;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SWCDynamicDocument extends SWCDocument
{
    private SWCDocument document1;
    private SWCDocument document2;

    public SWCDynamicDocument(String text1, String text2)
    {
        super(text1 + ContextDelimiter.DYNAMIC_DELIMITER_TEXT + text2);

        document1 = new SWCDocument(text1);
        document2 = new SWCDocument(text2);
    }

    @Override
    public void parse(ParseOptions parseOptions)
    {
        super.parse(parseOptions);

        document1.parse(parseOptions);
        document2.parse(parseOptions);
    }

    @Override
    public void weightFilter(int maxCount, RankingAlgo rankingAlgo)
    {
        document1.weightFilter(maxCount, rankingAlgo);
        document2.weightFilter(maxCount, rankingAlgo);

        Set<Word> commonWords = new HashSet();
        for (Word w : document1.getWords())
            if (!commonWords.contains(w))
                commonWords.add((Word)w.clone());
        
        for (Word w : document2.getWords())
            if (!commonWords.contains(w))
                commonWords.add((Word)w.clone());

        Map<Word, Word> words1 = new HashMap();
        document1.getWords().forEach(w -> words1.put(w, w));
        
        Map<Word, Word> words2 = new HashMap();
        document2.getWords().forEach(w -> words2.put(w, w));
        
        for (Word w : commonWords)
        {
            Word w1 = words1.getOrDefault(w, w);
            w.weight = Math.max(w.weight, w1.weight);
            
            Word w2 = words2.getOrDefault(w, w);
            w.weight = Math.max(w.weight, w2.weight);
        }
        
        setWords(new ArrayList(commonWords));
        Collections.sort(getWords(), Comparator.reverseOrder());
        rescaleWeights();
    }

    public SWCDocument getDocument1()
    {
        return document1;
    }

    public SWCDocument getDocument2()
    {
        return document2;
    }

    @Override
    public List<UIWord> prepareUIWords(LayoutResult layout, ColorScheme colorScheme)
    {
        if (colorScheme instanceof DynamicColorScheme)
        {
            ((DynamicColorScheme)colorScheme).initializeDynamic(this);
        }
        
        List<UIWord> uiWords1 = fixRectangleDimensions(document1, layout, colorScheme);
        List<UIWord> uiWords2 = fixRectangleDimensions(document2, layout, colorScheme);

        double maxY = uiWords1.stream().mapToDouble(w -> w.getRectangle().getMaxY()).max().orElse(0);
        double minY = uiWords1.stream().mapToDouble(w -> w.getRectangle().getMinY()).min().orElse(0);
        double minY2 = uiWords2.stream().mapToDouble(w -> w.getRectangle().getY()).min().orElse(0);

        uiWords2.forEach(w -> w.getRectangle().move(0, maxY - minY2 + (maxY - minY) / 10));

        List<UIWord> res = new ArrayList();
        res.addAll(uiWords1);
        res.addAll(uiWords2);
        return res;
    }

    private List<UIWord> fixRectangleDimensions(SWCDocument document, LayoutResult layout, ColorScheme colorScheme)
    {
        List<UIWord> uiWords2 = new ArrayList();
        for (Word w : document.getWords())
        {
            UIWord uiWord = new UIWord();
            uiWord.setText(w.word);
            uiWord.setColor(colorScheme.getColor(w));

            //restore the correct box for the dynamic case
            SWCRectangle original = layout.getWordPosition(w);
            SWCRectangle result = new BoundingBoxGenerator().getBoundingBox(w);
            result.moveTo(original.getX(), original.getY());
            uiWord.setRectangle(result);

            uiWords2.add(uiWord);
        }
        return uiWords2;
    }
}
