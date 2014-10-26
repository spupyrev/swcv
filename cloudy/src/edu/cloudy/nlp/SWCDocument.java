package edu.cloudy.nlp;

import edu.cloudy.colors.ColorScheme;
import edu.cloudy.layout.LayoutResult;
import edu.cloudy.nlp.ranking.RankingAlgo;
import edu.cloudy.render.UIWord;
import edu.cloudy.utils.CommonUtils;
import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;
import opennlp.tools.tokenize.Tokenizer;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * @author spupyrev
 * 
 * The base object representing the input text
 */
public class SWCDocument
{
    private String text;
    private List<Word> words;
    private List<String> sentences;

    public SWCDocument(String text)
    {
        this.text = text;
    }

    public void setWords(List<Word> list)
    {
        this.words = list;
    }

    public List<Word> getWords()
    {
        return words;
    }

    public List<String> getSentences()
    {
        return sentences;
    }

    public String getText()
    {
        return text;
    }

    /**
     * Parse document:
     * 1. wordify (extract sentences and words)
     * 2. stem
     * 3. remove stopwords
     */
    public void parse(ParseOptions parseOptions)
    {
        Tokenizer tokenizer = buildTokenizer(parseOptions);
        List<String> sentences = buildSentences(parseOptions);
        Set<String> stopwords = (parseOptions.isRemoveStopwords() ? buildStopwords(parseOptions) : Collections.EMPTY_SET);

        //stem => word
        Map<String, Word> wordMap = new HashMap<String, Word>();
        //stem => list of original words
        Map<String, List<String>> stemMap = new HashMap<String, List<String>>();

        for (int i = 0; i < sentences.size(); i++)
        {
            String[] temp = tokenizer.tokenize(sentences.get(i));
            for (int j = 0; j < temp.length; j++)
            {
                String currentWord = temp[j].toLowerCase();
                //words of length >= 3
                if (currentWord.length() < parseOptions.getMinWordLength())
                    continue;

                //only consider words starting with letters
                if (!isWord(currentWord, parseOptions))
                    continue;

                String currentStem = getStemmedWord(currentWord, parseOptions);

                //skip stopwords
                if (stopwords.contains(currentWord) || stopwords.contains(currentStem))
                    continue;

                if (!wordMap.containsKey(currentStem))
                {
                    wordMap.put(currentStem, new Word("", 0.0));
                    stemMap.put(currentStem, new ArrayList<String>());
                }

                wordMap.get(currentStem).stem = currentStem;
                wordMap.get(currentStem).addSentence(i);
                stemMap.get(currentStem).add(temp[j]);
            }
        }

        //restore the most popular word variant
        words = new ArrayList<Word>();
        for (String stem : wordMap.keySet())
        {
            Map<String, Integer> variants = new HashMap<String, Integer>();
            for (String w : stemMap.get(stem))
            {
                if (!variants.containsKey(w))
                    variants.put(w, 0);

                variants.put(w, variants.get(w) + 1);
            }

            String bestVariant = null;
            for (String variant : variants.keySet())
                if (bestVariant == null || variants.get(variant) > variants.get(bestVariant))
                    bestVariant = variant;

            wordMap.get(stem).word = bestVariant;
            words.add(wordMap.get(stem));
        }
    }

    private boolean isWord(String currentWord, ParseOptions parseOptions)
    {
        char firstCharacter = currentWord.charAt(0);
        if (!Character.isLetter(firstCharacter) && firstCharacter != '#')
            return false;

        for (int i = 1; i < currentWord.length(); i++)
        {
            char c = currentWord.charAt(i);
            boolean isLetter = Character.isLetter(c) || c == '-';
            if (parseOptions.isRemoveNumbers() && !isLetter)
                return false;
        }

        return true;
    }

    private List<String> buildSentences(ParseOptions parseOptions)
    {
        if (sentences != null)
            return sentences;

        InputStream modelIn1;
        modelIn1 = CommonUtils.getResourceAsStream(parseOptions.getLanguage().getSentFile());

        SentenceModel model1 = null;
        try
        {
            model1 = new SentenceModel(modelIn1);
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
        finally
        {
            if (modelIn1 != null)
            {
                try
                {
                    modelIn1.close();
                }
                catch (IOException e)
                {
                    throw new RuntimeException(e);
                }
            }
        }
        SentenceDetectorME sentenceDetector = new SentenceDetectorME(model1);

        // Split into sentences
        sentences = Arrays.asList(sentenceDetector.sentDetect(text));
        sentences = TextUtils.splitSentences(sentences);
        return sentences;
    }

    private Tokenizer buildTokenizer(ParseOptions parseOptions)
    {
        InputStream modelIn2 = CommonUtils.getResourceAsStream(parseOptions.getLanguage().getTokenFile());
        TokenizerModel model2 = null;
        try
        {
            model2 = new TokenizerModel(modelIn2);
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
        finally
        {
            if (modelIn2 != null)
            {
                try
                {
                    modelIn2.close();
                }
                catch (IOException e)
                {
                    throw new RuntimeException(e);
                }
            }
        }
        Tokenizer tokenizer = new TokenizerME(model2);
        return tokenizer;
    }

    private String getStemmedWord(String word, ParseOptions parseOptions)
    {
        if (!parseOptions.isStemWords())
            return word;

        return TextUtils.stem(word, parseOptions);
    }

    private Set<String> buildStopwords(ParseOptions parseOptions)
    {
        Set<String> stopWords = new HashSet<String>();

        try
        {
            Scanner br = new Scanner(CommonUtils.getResourceAsStream(parseOptions.getLanguage().getStopwordsFile()), "UTF-8");
            while (br.hasNext())
            {
                String token = br.next();
                String word = token.toLowerCase().trim();
                stopWords.add(word);
            }
            br.close();
        }
        catch (IllegalArgumentException e)
        {
            throw new RuntimeException(e);
        }

        return stopWords;
    }

    /**
     * Keep the most important words
     */
    public void weightFilter(int maxWords, RankingAlgo rankingAlgo)
    {
        rankingAlgo.buildWeights(this);
        Collections.sort(words, Comparator.reverseOrder());

        if (words.size() > maxWords)
            words = words.subList(0, maxWords);

        rescaleWeights();
    }

    /**
     * scaling weights from 1 to 5
     */
    protected void rescaleWeights()
    {
        rescaleWeights(5);
    }

    /**
     * scaling weights from 1 to upper
     */
    private void rescaleWeights(double upper)
    {
        if (words.size() <= 1)
            return;

        double mn = words.get(words.size() - 1).weight;
        for (Word w : words)
            w.weight /= mn;

        double mx = words.get(0).weight;
        double diff = mx - 1.0;
        for (Word w : words)
        {
            double d = w.weight - 1.0;
            if (diff < 1e-6)
                w.weight = 1.0;
            else
                w.weight = 1.0 + (d / diff) * (upper - 1.0);
        }
    }

    public List<UIWord> prepareUIWords(LayoutResult layout, ColorScheme colorScheme)
    {
        return UIWord.prepareUIWords(words, layout, colorScheme);
    }
}
