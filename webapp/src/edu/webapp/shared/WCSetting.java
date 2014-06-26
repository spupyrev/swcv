package edu.webapp.shared;

import java.io.Serializable;
import java.util.Random;

/**
 * @author spupyrev
 *         Aug 17, 2013
 */
public class WCSetting implements Serializable
{
	private static final long serialVersionUID = 5465297978880066047L;

	public WCSetting()
	{
	}

	public enum COLOR_DISTRIBUTE
	{
		RANDOM, WORD_RANK, KMEANS, SENTIMENT, DYNAMIC
	}

	public enum COLOR_SCHEME
	{
		GREEN, ORANGE, BLUE, BREWER_1, BREWER_2, BREWER_3, SIMILAR_1, SIMILAR_2, SIMILAR_3, TRISCHEME_1, TRISCHEME_2, TRISCHEME_3, BEAR_DOWN, SENTIMENT, SENTIMENT2, REDBLUEBLACK, BLUEREDBLACK, ORANGESEQUENTIAL, BLUESEQUENTIAL, GREENSEQUENTIAL
	}

	public enum LAYOUT_ALGORITHM
	{
		WORDLE, CPWCV, SEAM, INFLATE, STAR, CYCLE, MDS
	}

	public enum SIMILARITY_ALGORITHM
	{
		COSINE, JACCARD, LEXICAL, MATRIXDIS, DICECOEFFI
	}

	public enum RANKING_ALGORITHM
	{
		TF, TF_IDF, LEX
	}

	public enum FONT
	{
		Archer, ComicSansMS, Crimson, Dearest, Eraser, Harting, Inconsolata, Kingthings_Gothique, MaiandraGD, Pacifico, Porcelai, Report1942, Stentiga, Teen, Monofur, Waker, Wetpet
	}

	private COLOR_DISTRIBUTE colorDistribute = COLOR_DISTRIBUTE.KMEANS;
	private COLOR_SCHEME colorScheme = COLOR_SCHEME.BREWER_2;
	private LAYOUT_ALGORITHM layoutAlgorithm = LAYOUT_ALGORITHM.CPWCV;
	private SIMILARITY_ALGORITHM similarityAlgorithm = SIMILARITY_ALGORITHM.COSINE;
	private RANKING_ALGORITHM rankingAlgorithm = RANKING_ALGORITHM.TF;
	private FONT font = FONT.Archer;

	private int wordCount = 50;
	private boolean showRectangles = false;

	public void setRandomSetting()
	{
		Random dice = new Random();
		int pick;

		setWordCount(this.getWordCount());

		pick = dice.nextInt(COLOR_DISTRIBUTE.values().length);
		setColorDistribute(COLOR_DISTRIBUTE.values()[pick]);

		pick = dice.nextInt(COLOR_SCHEME.values().length);
		setColorScheme(COLOR_SCHEME.values()[pick]);

		pick = dice.nextInt(LAYOUT_ALGORITHM.values().length);
		setLayoutAlgorithm(LAYOUT_ALGORITHM.values()[pick]);

		pick = dice.nextInt(SIMILARITY_ALGORITHM.values().length);
		setSimilarityAlgorithm(SIMILARITY_ALGORITHM.values()[pick]);

		pick = dice.nextInt(RANKING_ALGORITHM.values().length);
		setRankingAlgorithm(RANKING_ALGORITHM.values()[pick]);

		pick = dice.nextInt(FONT.values().length);
		setFont(FONT.values()[pick]);
	}

	public COLOR_DISTRIBUTE getColorDistribute()
	{
		return colorDistribute;
	}

	public void setColorDistribute(COLOR_DISTRIBUTE colorDistribute)
	{
		this.colorDistribute = colorDistribute;
	}

	public COLOR_SCHEME getColorScheme()
	{
		return colorScheme;
	}

	public void setColorScheme(COLOR_SCHEME colorScheme)
	{
		this.colorScheme = colorScheme;
	}

	public LAYOUT_ALGORITHM getLayoutAlgorithm()
	{
		return layoutAlgorithm;
	}

	public void setLayoutAlgorithm(LAYOUT_ALGORITHM layoutAlgorithm)
	{
		this.layoutAlgorithm = layoutAlgorithm;
	}

	public SIMILARITY_ALGORITHM getSimilarityAlgorithm()
	{
		return similarityAlgorithm;
	}

	public void setSimilarityAlgorithm(SIMILARITY_ALGORITHM similarityAlgorithm)
	{
		this.similarityAlgorithm = similarityAlgorithm;
	}

	public int getWordCount()
	{
		return wordCount;
	}

	public void setWordCount(int wordCount)
	{
		this.wordCount = wordCount;
	}

	public boolean isShowRectangles()
	{
		return showRectangles;
	}

	public void setShowRectangles(boolean showRectangles)
	{
		this.showRectangles = showRectangles;
	}

	public RANKING_ALGORITHM getRankingAlgorithm()
	{
		return rankingAlgorithm;
	}

	public void setRankingAlgorithm(RANKING_ALGORITHM rankingAlgorithm)
	{
		this.rankingAlgorithm = rankingAlgorithm;
	}

	public FONT getFont()
	{
		return font;
	}

	public void setFont(FONT font)
	{
		this.font = font;
	}

	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		sb.append("\nwordCount:  " + wordCount + "\n");
		sb.append("layout:     " + layoutAlgorithm + "\n");
		sb.append("similarity: " + similarityAlgorithm + "\n");
		sb.append("ranking:    " + rankingAlgorithm + "\n");
		sb.append("colorTheme: " + colorScheme + "\n");
		sb.append("colorDistr: " + colorDistribute + "\n");
		sb.append("font: " + font + "\n");
		return sb.toString();
	}
}
