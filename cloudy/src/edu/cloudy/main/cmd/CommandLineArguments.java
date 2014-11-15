package edu.cloudy.main.cmd;

import edu.cloudy.nlp.ParseOptions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author spupyrev
 * Oct 15, 2014
 */
public class CommandLineArguments
{
    private String[] args;
    private boolean printUsage = false;

    private String inputFile;
    private String outputFile;
    private boolean autogenOutputFile = false;
    private String outputFormat = "svg";

    private int maxWords = 50;
    private int maxWidth = 1280;
    private int maxHeight = 1024;
    private double aspectRatio = 16.0 / 9.0;
    private String layoutAlgorithm = "cp";
    private String rankAlgorithm = "tf";
    private String similarityAlgorithm = "cos";
    private String font = "Arial";
    private String color = "7";

    private ParseOptions parseOptions = new ParseOptions();

    public CommandLineArguments(String[] args)
    {
        this.args = args;
    }

    public void usage()
    {
        System.out.println("Usage: java -jar cloudy.jar [options] [input file]");
        System.out.println("\twhere input file must contain a text with at least 10 distinct words. If no input file is supplied, the program reads from stdin.");
        System.out.println("\tAcceptable options are:");

        System.out.println("\t-sv      - set maximum number of rendered words to 'v', allowed values are between 10 and 500 (50)");
        System.out.println("\t-wv      - set maximum width of drawing to 'v', in pixels (1280)");
        System.out.println("\t-hv      - set maximum height of drawing to 'v', in pixels (1024)");
        System.out.println("\t-aw:h    - set desired aspect ratio (width/height) for the drawing (16:9)");
        System.out.println("\t-fv      - sprecifies font family of the words, it can be anything installed on your machine (Arial)");

        System.out.println("\t-Ccolor  - specifies coloring scheme of the words (7)");
        System.out.println("\t       0 : black");
        System.out.println("\t       1 : green");
        System.out.println("\t       2 : orange");
        System.out.println("\t       3 : blue");
        System.out.println("\t       4 : random");
        System.out.println("\t       5 : dark red-dark blue");
        System.out.println("\t       6 : blue-green-pink");
        System.out.println("\t       7 : red-blue-green-purple-orange");
        System.out.println("\t       8 : pearl aqua-blond-lavender gray-coral pink");
        System.out.println("\t       9 : red-persian green-spring bud-dark apple red");
        System.out.println("\t      10 : turquoise-yellow-red-teal green");
        System.out.println("\t      11 : persian green-yellow-red-teal green");
        System.out.println("\t      12 : red to blue");
        System.out.println("\t      13 : red to purple");
        System.out.println("\t      14 : red to green");

        System.out.println("\t-Llayout - specifies which layout algorithm to use (cp)");
        System.out.println("\t     rnd : Wordle (random)");
        System.out.println("\t     tca : Tag Cloud (words sorted alphabetically)");
        System.out.println("\t     tcr : Tag Cloud (words sorted by rank)");
        System.out.println("\t      cp : Context Preserving");
        System.out.println("\t      sc : Seam Carving");
        System.out.println("\t      ip : Inflate and Push");
        System.out.println("\t     mds : Force-Directed Packing");
        System.out.println("\t      sf : Star Forest");
        System.out.println("\t      cc : Cycle Cover");

        System.out.println("\t-Rrank   - specifies which ranking algorithm to use (tf)");
        System.out.println("\t      tf : Term Frequency");
        System.out.println("\t  tf-idf : Term Frequency normalized by Brown document corpus");
        System.out.println("\t     lex : Graph-based Lexical Centrality");

        System.out.println("\t-Ssim    - specifies which similarity algorithm to use (cos)");
        System.out.println("\t     cos : Cosine Coefficient");
        System.out.println("\t     jac : Jaccard Coefficient");
        System.out.println("\t     lex : Lin's Lexical Similarity");

        System.out.println("\t-ps      - do NOT remove stop words");
        System.out.println("\t-pg      - do NOT group similar words");
        System.out.println("\t-pn      - do NOT remove numbers");
        System.out.println("\t-plv     - set minimum length of words to 'v' (3)");
        
        System.out.println("\t-llang   - set the language of the input text to 'lang' (en)");
        System.out.println("\t      ar : Arabic");
        System.out.println("\t      cs : Czech");
        System.out.println("\t      da : Danish");
        System.out.println("\t      nl : Dutch");
        System.out.println("\t      en : English");
        System.out.println("\t      fi : Finnish");
        System.out.println("\t      fr : French");
        System.out.println("\t      de : German");
        System.out.println("\t      el : Greek");
        System.out.println("\t      hu : Hungarian");
        System.out.println("\t      it : Italian");
        System.out.println("\t      ja : Japanese");
        System.out.println("\t      no : Norwegian");
        System.out.println("\t      pl : Polish");
        System.out.println("\t      pt : Portuguese");
        System.out.println("\t      ru : Russian");
        System.out.println("\t      es : Spanish");
        System.out.println("\t      sv : Swedish");
        System.out.println("\t      tr : Turkish");
        
        System.out.println("\t-ofile   - write output to 'file' (stdout)");
        System.out.println("\t-O       - automatically generate an output filename based on the input filename with a .'format' appended");

        System.out.println("\t-Tformat - set output to one of the supported formats (svg)");
        System.out.println("\t     bmp : Windows Bitmap Format");
        System.out.println("\t     eps : Encapsulated PostScript");
        System.out.println("\t     jpg : JPEG");
        System.out.println("\t     pdf : Portable Document Format");
        System.out.println("\t     png : Portable Network Graphics");
        System.out.println("\t      ps : PostScript");
        System.out.println("\t     svg : Scalable Vector Graphics");
        System.out.println("\t     tif : Tag Image File Format");
        System.out.println("\t-?       - print usage information, then exit");
    }

    private static List<BaseArgumentParser> parsers;

    static
    {
        parsers = new ArrayList<BaseArgumentParser>();

        parsers.add(new StringArgumentParser("-o", (cmd, value) -> cmd.outputFile = value));
        parsers.add(new StringArgumentParser("-O", (cmd, value) -> cmd.autogenOutputFile = true));
        parsers.add(new StringArgumentParser("-T", (cmd, value) -> cmd.outputFormat = value));
        parsers.add(new StringArgumentParser("-C", (cmd, value) -> cmd.color = value));
        parsers.add(new StringArgumentParser("-L", (cmd, value) -> cmd.layoutAlgorithm = value));
        parsers.add(new StringArgumentParser("-R", (cmd, value) -> cmd.rankAlgorithm = value));
        parsers.add(new StringArgumentParser("-S", (cmd, value) -> cmd.similarityAlgorithm = value));
        parsers.add(new StringArgumentParser("-a", (cmd, value) ->
        {
            String[] tmp = value.split(":");
            double w = Double.valueOf(tmp[0]);
            double h = Double.valueOf(tmp[1]);
            cmd.aspectRatio = w / h;
        }));
        parsers.add(new IntegerArgumentParser("-s", 10, 500, (cmd, value) -> cmd.maxWords = value));
        parsers.add(new IntegerArgumentParser("-w", 256, 8192, (cmd, value) -> cmd.maxWidth = value));
        parsers.add(new IntegerArgumentParser("-h", 192, 4800, (cmd, value) -> cmd.maxHeight = value));
        parsers.add(new StringArgumentParser("-ps", (cmd, value) -> cmd.parseOptions.setRemoveStopwords(false)));
        parsers.add(new StringArgumentParser("-pg", (cmd, value) -> cmd.parseOptions.setStemWords(false)));
        parsers.add(new StringArgumentParser("-pn", (cmd, value) -> cmd.parseOptions.setRemoveNumbers(false)));
        parsers.add(new IntegerArgumentParser("-pl", 1, 30, (cmd, value) -> cmd.parseOptions.setMinWordLength(value)));
        parsers.add(new StringArgumentParser("-l", (cmd, value) -> cmd.parseOptions.setLanguage(value)));
        parsers.add(new StringArgumentParser("-f", (cmd, value) -> cmd.font = value));
        parsers.add(new StringArgumentParser("-?", (cmd, value) -> cmd.printUsage = true));
        parsers.add(new StringArgumentParser("-", (cmd, value) -> System.out.println("unrecosgnized option '-" + value + "'")));
        parsers.add(new StringArgumentParser("", (cmd, value) -> cmd.inputFile = value));
    }

    public boolean parse()
    {
        Arrays.asList(args).forEach(a -> parseOption(a));
        return true;
    }

    private void parseOption(String option)
    {
        for (BaseArgumentParser parser : parsers)
            if (parser.accept(option))
            {
                parser.apply(this, option);
                break;
            }
    }

    public String getInputFile()
    {
        return inputFile;
    }

    public String getOutputFile()
    {
        return outputFile;
    }

    public boolean isAutogenOutputFile()
    {
        return autogenOutputFile;
    }

    public String getOutputFormat()
    {
        return outputFormat;
    }

    public int getMaxWidth()
    {
        return maxWidth;
    }

    public int getMaxHeight()
    {
        return maxHeight;
    }

    public boolean isPrintUsage()
    {
        return printUsage;
    }

    public String getLayoutAlgorithm()
    {
        return layoutAlgorithm;
    }

    public String getRankAlgorithm()
    {
        return rankAlgorithm;
    }

    public int getMaxWords()
    {
        return maxWords;
    }

    public String getSimilarityAlgorithm()
    {
        return similarityAlgorithm;
    }

    public double getAspectRatio()
    {
        return aspectRatio;
    }

    public ParseOptions getParseOptions()
    {
        return parseOptions;
    }

    public String getFont()
    {
        return font;
    }

    public String getColor()
    {
        return color;
    }

}
