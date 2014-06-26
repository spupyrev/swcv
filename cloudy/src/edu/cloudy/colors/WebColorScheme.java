package edu.cloudy.colors;

import edu.cloudy.nlp.Word;

import java.awt.Color;
import java.util.Random;

/**
 * @author spupyrev
 * Nov 28, 2013
 * 
 * TODO: refactor!
 */
public class WebColorScheme implements IColorScheme
{
	private Color select;
	private Color[] multi_select;
	private Color[] seq_select;

	boolean monocolor = false;
	boolean mulitcolor = false;

	private String db;
	private int wordSize;
	private int indexForRank;

	public WebColorScheme(String colorSchemeName, String distribute, int wordSize)
	{
		setColorScheme(colorSchemeName);
		db = distribute;
		indexForRank = 0;
		this.wordSize = wordSize;
	}

	private static final Random dice = new Random(123);

	@Override
	public Color getColor(Word word)
	{
		if (monocolor == true)
		{
			return select;
		}
		else if (mulitcolor == true)
		{
			return multi_select[dice.nextInt(multi_select.length)];
		}
		else
		{
			if ("WORD_RANK".equals(db))
			{
				indexForRank++;

				if (indexForRank < (wordSize / 8))
					return seq_select[0];
				else if (indexForRank < (wordSize / 4))
					return seq_select[1];
				else if (indexForRank < (wordSize / 2))
					return seq_select[2];
				else
					return seq_select[3];
			}
			else
			{
				int randomindex = dice.nextInt(seq_select.length);
				return seq_select[randomindex];
			}
		}
	}

	private void setColorScheme(String colorScheme)
	{
		if (colorScheme.equals("GREEN"))
		{
			select = GREEN;
			monocolor = true;
		}
		else if (colorScheme.equals("BLUE"))
		{
			select = BLUE;
			monocolor = true;
		}
		else if (colorScheme.equals("ORANGE"))
		{
			select = ORANGE;
			monocolor = true;
		}
		else if (colorScheme.equals("BREWER_1"))
		{
			seq_select = colorbrewer_1;
			monocolor = false;
		}
		else if (colorScheme.equals("BREWER_2"))
		{
			seq_select = colorbrewer_2;
			monocolor = false;
		}
		else if (colorScheme.equals("BREWER_3"))
		{
			seq_select = colorbrewer_3;
			monocolor = false;
		}
		else if (colorScheme.equals("TRISCHEME_1"))
		{
			multi_select = trischeme_1;
			mulitcolor = true;
		}
		else if (colorScheme.equals("TRISCHEME_2"))
		{
			multi_select = trischeme_2;
			mulitcolor = true;
		}
		else if (colorScheme.equals("TRISCHEME_3"))
		{
			multi_select = trischeme_3;
			mulitcolor = true;
		}
		else if (colorScheme.equals("SIMILAR_1"))
		{
			multi_select = similar_1;
			mulitcolor = true;
		}
		else if (colorScheme.equals("SIMILAR_2"))
		{
			multi_select = similar_2;
			mulitcolor = true;
		}
		else if (colorScheme.equals("SIMILAR_3"))
		{
			multi_select = similar_3;
			mulitcolor = true;
		}
		else if (colorScheme.equals("BEAR_DOWN"))
		{
			multi_select = bear_down;
			mulitcolor = true;
		}
		else if (colorScheme.equals("SENTIMENT2"))
		{
			multi_select = sentiment2;
			mulitcolor = true;
		}
		else if (colorScheme.equals("SENTIMENT2"))
		{
			multi_select = sentiment2;
			mulitcolor = true;
		}
		else if (colorScheme.equals("REDBLUEBLACK"))
		{
			multi_select = redblueblack;
			mulitcolor = true;
		}
		else if (colorScheme.equals("BLUEREDBLACK"))
		{
			multi_select = blueredblack;
			mulitcolor = true;
		}
		else if (colorScheme.equals("ORANGESEQUENTIAL"))
		{
			multi_select = orange_sequential;
			mulitcolor = true;
		}
		else if (colorScheme.equals("BLUESEQUENTIAL"))
		{
			multi_select = blue_sequential;
			mulitcolor = true;
		}
		else if (colorScheme.equals("GREENSEQUENTIAL"))
		{
			multi_select = green_sequential;
			mulitcolor = true;
		}
	}

}
