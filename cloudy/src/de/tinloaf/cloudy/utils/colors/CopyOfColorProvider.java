package de.tinloaf.cloudy.utils.colors;

import java.awt.Color;
import java.util.Random;

public class CopyOfColorProvider {

	private Color orange = new Color(230, 85, 13);
	private Color blue = new Color(49, 130, 189);
	private Color green = new Color(49, 163, 84);;

	private Color[] bear_down = { new Color(150, 0, 23), new Color(0, 23, 57) };
	private Color[] orange_seq = { new Color(166, 54, 3), new Color(230, 85, 13), new Color(253, 141, 60), new Color(253, 174, 107) };
	private Color[] green_seq = { new Color(0, 109, 44), new Color(49, 163, 84), new Color(116, 196, 118), new Color(161, 217, 155) };
	private Color[] blue_seq = { new Color(8, 81, 156), new Color(49, 130, 189), new Color(107, 174, 214), new Color(158, 202, 225) };

	private Color[] trischeme_1 = { new Color(255, 0, 0), new Color(0, 153, 153), new Color(159, 238, 0), new Color(166, 0, 0) };
	private Color[] trischeme_2 = { new Color(0, 255, 255), new Color(255, 170, 0), new Color(255, 0, 0), new Color(0, 99, 99) };
	private Color[] trischeme_3 = { new Color(0, 155, 149), new Color(255, 169, 0), new Color(253, 0, 6), new Color(0, 101, 97) };

	private Color[] similar_1 = { new Color(240, 0, 29), new Color(255, 103, 0), new Color(19, 0, 131), new Color(159, 0, 19) };
	private Color[] similar_2 = { new Color(126, 7, 169), new Color(213, 0, 101), new Color(66, 18, 175), new Color(82, 2, 110) };
	private Color[] similar_3 = { new Color(0, 142, 155), new Color(152, 237, 0), new Color(166, 54, 3), new Color(0, 129, 10) };

	private Color select;
	private Color[] multi_select;
	private Color[] seq_select;

	boolean monocolor = false;
	boolean mulitcolor = false;
	private String db;
	private int wordSize;
	private int indexForRank;

	public CopyOfColorProvider() {
		select = blue;
		wordSize = 0;
		seq_select = similar_2;
	}

	public CopyOfColorProvider(String colorScheme, String distribute, int wordSize) {
		setColorScheme(colorScheme);
		db = distribute;
		indexForRank = 0;
		this.wordSize = wordSize;
	}

	private static final Random dice = new Random(123);

	public Color getColor() {
		if (monocolor == true) {
			return select;
		} else if (mulitcolor == true) {
			return multi_select[dice.nextInt(multi_select.length)];
		} else {
			if ("WORD_RANK".equals(db)) {

				indexForRank++;

				if (indexForRank < (wordSize / 8))
					return seq_select[0];
				else if (indexForRank < (wordSize / 4))
					return seq_select[1];
				else if (indexForRank < (wordSize / 2))
					return seq_select[2];
				else
					return seq_select[3];

			} else {
				int randomindex = dice.nextInt(seq_select.length);
				return seq_select[randomindex];
			}
		}
	}

	private void setColorScheme(String colorScheme) {
		if (colorScheme.equals("GREEN")) {
			select = green;
			monocolor = true;
		} else if (colorScheme.equals("BLUE")) {
			select = blue;
			monocolor = true;
		} else if (colorScheme.equals("ORANGE")) {
			select = orange;
			monocolor = true;
		} else if (colorScheme.equals("GREEN_SEQ")) {
			seq_select = green_seq;
			monocolor = false;
		} else if (colorScheme.equals("BLUE_SEQ")) {
			seq_select = blue_seq;
			monocolor = false;
		} else if (colorScheme.equals("ORANGE_SEQ")) {
			seq_select = orange_seq;
			monocolor = false;
		} else if (colorScheme.equals("TRISCHEME_1")) {
			multi_select = trischeme_1;
			mulitcolor = true;
		} else if (colorScheme.equals("TRISCHEME_2")) {
			multi_select = trischeme_2;
			mulitcolor = true;
		} else if (colorScheme.equals("TRISCHEME_3")) {
			multi_select = trischeme_3;
			mulitcolor = true;
		} else if (colorScheme.equals("SIMILAR_1")) {
			multi_select = similar_1;
			mulitcolor = true;
		} else if (colorScheme.equals("SIMILAR_2")) {
			multi_select = similar_2;
			mulitcolor = true;
		} else if (colorScheme.equals("SIMILAR_3")) {
			multi_select = similar_3;
			mulitcolor = true;
		} else if (colorScheme.equals("BEAR_DOWN")) {
			multi_select = bear_down;
			mulitcolor = true;
		}
	}

}
