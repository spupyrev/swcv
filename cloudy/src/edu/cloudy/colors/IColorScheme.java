package edu.cloudy.colors;

import edu.cloudy.nlp.Word;

import java.awt.Color;

/**
 * @author spupyrev
 * Nov 28, 2013
 */
public interface IColorScheme {
	static final Color ORANGE = new Color(230, 85, 13);
	static final Color BLUE = new Color(49, 130, 189);
	static final Color GREEN = new Color(49, 163, 84);;

	static final Color[] bear_down = { new Color(204, 0, 51), new Color(0, 51, 102) };
	static final Color[] orange_seq = { new Color(166, 54, 3), new Color(230, 85, 13), new Color(253, 141, 60), new Color(253, 174, 107) };
	static final Color[] green_seq = { new Color(0, 109, 44), new Color(49, 163, 84), new Color(116, 196, 118), new Color(161, 217, 155) };
	static final Color[] blue_seq = { new Color(8, 81, 156), new Color(49, 130, 189), new Color(107, 174, 214), new Color(158, 202, 225) };

	static final Color[] trischeme_1 = { new Color(255, 0, 0), new Color(0, 153, 153), new Color(159, 238, 0), new Color(166, 0, 0) };
	static final Color[] trischeme_2 = { new Color(0, 255, 255), new Color(255, 170, 0), new Color(255, 0, 0), new Color(0, 99, 99) };
	static final Color[] trischeme_3 = { new Color(0, 155, 149), new Color(255, 169, 0), new Color(253, 0, 6), new Color(0, 101, 97) };

	static final Color[] similar_1 = { new Color(240, 0, 29), new Color(255, 103, 0), new Color(19, 0, 131), new Color(159, 0, 19) };
	static final Color[] similar_2 = { new Color(126, 7, 169), new Color(213, 0, 101), new Color(66, 18, 175), new Color(82, 2, 110) };
	static final Color[] similar_3 = { new Color(0, 142, 155), new Color(152, 237, 0), new Color(166, 54, 3), new Color(0, 129, 10) };
	
	Color getColor(Word word);
}
