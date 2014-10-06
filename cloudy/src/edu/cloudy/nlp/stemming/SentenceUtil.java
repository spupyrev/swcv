package edu.cloudy.nlp.stemming;

import edu.cloudy.nlp.Word;
import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SentenceUtil
{
    public static Map<String, List<Integer>> strToSentenceIndices(String text, List<Word> words) throws FileNotFoundException
    {
        Map<String, List<Integer>> ret = new HashMap<String, List<Integer>>();

        Set<String> knownWords = new HashSet<String>();

        for (Word w : words)
        {
            knownWords.add(w.word.toLowerCase());
        }

        InputStream modelIn1 = new FileInputStream("opennlp/models/en-sent.bin");
        SentenceModel model1 = null;
        try
        {
            model1 = new SentenceModel(modelIn1);
        }
        catch (IOException e)
        {
            e.printStackTrace();
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
                }
            }
        }
        SentenceDetectorME sentenceDetector = new SentenceDetectorME(model1);

        // Initialize the tokenizer
        InputStream modelIn2 = new FileInputStream("opennlp/models/en-token.bin");
        TokenizerModel model2 = null;
        try
        {
            model2 = new TokenizerModel(modelIn2);
        }
        catch (IOException e)
        {
            e.printStackTrace();
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
                }
            }
        }
        TokenizerME tokenizer = new TokenizerME(model2);

        // Split into sentences
        String sentences[] = sentenceDetector.sentDetect(text);

        for (int i = 0; i < sentences.length; i++)
        {
            String[] temp = tokenizer.tokenize(sentences[i]);
            for (int j = 0; j < temp.length; j++)
            {
                temp[j] = temp[j].toLowerCase();
                if (!Character.isLetter(temp[j].charAt(0)))
                {
                    continue;
                }

                if (!knownWords.contains(temp[j]))
                {
                    // skip unknown words
                    continue;
                }

                if (!ret.containsKey(temp[j]))
                {
                    ret.put(temp[j], new LinkedList<Integer>());
                }
                ret.get(temp[j]).add(i);
            }
        }

        return ret;
    }

}
