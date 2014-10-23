package edu.cloudy.nlp.lang;

import java.util.HashMap;
import java.util.Map;

/**
 * @author spupyrev
 * Oct 17, 2014
 */
public class LanguageRegistry
{
    private static Map<String, Language> langs = new HashMap<String, Language>();

    static
    {
        langs.put("ar", new Language("ar", "opennlp/en-token.bin", "opennlp/en-sent.bin", "opennlp/ar-stopwords.txt"));
        langs.put("cs", new Language("cs", "opennlp/en-token.bin", "opennlp/en-sent.bin", "opennlp/cs-stopwords.txt"));
        langs.put("da", new Language("da", "opennlp/da-token.bin", "opennlp/da-sent.bin", "opennlp/da-stopwords.txt"));
        langs.put("de", new Language("de", "opennlp/de-token.bin", "opennlp/de-sent.bin", "opennlp/de-stopwords.txt"));
        langs.put("el", new Language("el", "opennlp/en-token.bin", "opennlp/en-sent.bin", "opennlp/el-stopwords.txt"));
        langs.put("en", new Language("en", "opennlp/en-token.bin", "opennlp/en-sent.bin", "opennlp/en-stopwords.txt"));
        langs.put("es", new Language("es", "opennlp/en-token.bin", "opennlp/en-sent.bin", "opennlp/es-stopwords.txt"));
        langs.put("fi", new Language("fi", "opennlp/en-token.bin", "opennlp/en-sent.bin", "opennlp/fi-stopwords.txt"));
        langs.put("fr", new Language("fr", "opennlp/en-token.bin", "opennlp/en-sent.bin", "opennlp/fr-stopwords.txt"));
        langs.put("hu", new Language("hu", "opennlp/en-token.bin", "opennlp/en-sent.bin", "opennlp/hu-stopwords.txt"));
        langs.put("it", new Language("it", "opennlp/en-token.bin", "opennlp/en-sent.bin", "opennlp/it-stopwords.txt"));
        langs.put("ja", new Language("ja", "opennlp/en-token.bin", "opennlp/en-sent.bin", "opennlp/ja-stopwords.txt"));
        langs.put("nl", new Language("nl", "opennlp/nl-token.bin", "opennlp/nl-sent.bin", "opennlp/nl-stopwords.txt"));
        langs.put("no", new Language("no", "opennlp/en-token.bin", "opennlp/en-sent.bin", "opennlp/no-stopwords.txt"));
        langs.put("pl", new Language("pl", "opennlp/pt-token.bin", "opennlp/pt-sent.bin", "opennlp/pl-stopwords.txt"));
        langs.put("pt", new Language("pt", "opennlp/en-token.bin", "opennlp/en-sent.bin", "opennlp/pt-stopwords.txt"));
        langs.put("ru", new Language("ru", "opennlp/en-token.bin", "opennlp/en-sent.bin", "opennlp/ru-stopwords.txt"));
        langs.put("sv", new Language("sv", "opennlp/sv-token.bin", "opennlp/sv-sent.bin", "opennlp/sv-stopwords.txt"));
        langs.put("tr", new Language("tr", "opennlp/en-token.bin", "opennlp/en-sent.bin", "opennlp/tr-stopwords.txt"));
    }

    public static Language getById(String id)
    {
        return langs.get(id);
    }
}
