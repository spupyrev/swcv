package edu.cloudy.nlp;

import edu.cloudy.nlp.ranking.RankingAlgo;
import edu.cloudy.nlp.stemming.BaseStemmer;
import edu.cloudy.nlp.stemming.PorterStemmer;
import edu.cloudy.utils.CommonUtils;
import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;
import opennlp.tools.tokenize.Tokenizer;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;

import org.lemurproject.kstem.KrovetzStemmer;

import java.awt.Point;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author spupyrev
 * 
 * The base object representing the input text
 */
public class SWCDocument
{
    private String text;
    private List<Word> words;

    /**
     * This is a default constructor used by WCVDocument4Sentiment
     */
    public SWCDocument()
    {
        this.text = null;
    }

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

    public void setText(String text)
    {
        this.text = text;
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
        text = TextUtils.splitSentences(text);

        Tokenizer tokenizer = buildTokenizer();
        String[] sentences = buildSentences();
        Set<String> stopwords = (parseOptions.isRemoveStopwords() ? buildStopwords() : new HashSet<String>());

        //LovinsStemmer stemmer = new LovinsStemmer();
        BaseStemmer stemmer = new PorterStemmer();
        //stem => word
        Map<String, Word> wordMap = new HashMap<String, Word>();
        //stem => list of original words
        Map<String, List<String>> stemMap = new HashMap<String, List<String>>();

        for (int i = 0; i < sentences.length; i++)
        {
            String[] temp = tokenizer.tokenize(sentences[i]);
            for (int j = 0; j < temp.length; j++)
            {
                String currentWord = temp[j].toLowerCase();
                //words of length >= 3
                if (currentWord.length() < parseOptions.getMinWordLength())
                    continue;

                //only consider words starting with letters
                if (!isWord(currentWord, parseOptions))
                    continue;

                String currentStem = getStemmedWord(currentWord, stemmer, parseOptions);

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
                wordMap.get(currentStem).addCoordinate(new Point(j, i));
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

    public String[] buildSentences()
    {
        InputStream modelIn1;
        modelIn1 = CommonUtils.getResourceAsStream("opennlp/en-sent.bin");

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
        String sentences[] = sentenceDetector.sentDetect(text);
        return sentences;
    }

    private Tokenizer buildTokenizer()
    {
        InputStream modelIn2;
        modelIn2 = CommonUtils.getResourceAsStream("opennlp/en-token.bin");
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

    private String getStemmedWord(String word, BaseStemmer stemmer, ParseOptions parseOptions)
    {
        if (!parseOptions.isStemWords())
            return word;

        KrovetzStemmer krovetstem = new KrovetzStemmer();
        String prestemmed = krovetstem.stem(word);
        return stemmer.stem(prestemmed);
    }

    private Set<String> buildStopwords()
    {
        Set<String> stopWords = new HashSet<String>();

        try
        {
            BufferedReader br = new BufferedReader(new InputStreamReader(CommonUtils.getResourceAsStream("opennlp/stopwords-en.txt")));
            String line;
            while ((line = br.readLine()) != null)
            {
                stopWords.add(line.toLowerCase().trim());
            }
            br.close();
        }
        catch (IOException e)
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
        Collections.sort(words);
        Collections.reverse(words);

        if (words.size() > maxWords)
            words = words.subList(0, maxWords);
        rescaleWeights(words, 5);
    }

    /**
     * scaling weights from 1 to upper
     */
    public void rescaleWeights(List<Word> words, double upper)
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

}