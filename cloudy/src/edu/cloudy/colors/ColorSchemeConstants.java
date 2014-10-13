package edu.cloudy.colors;

import java.awt.Color;

/**
 * @author spupyrev
 * Oct 11, 2014
 */
public interface ColorSchemeConstants
{
    static final Color ORANGE = new Color(230, 85, 13);
    static final Color BLUE = new Color(49, 130, 189);
    static final Color GREEN = new Color(49, 163, 84);
    static final Color BLACK = new Color(0, 0, 0);

    static final Color[] bear_down = {
            new Color(204, 0, 51),
            new Color(0, 51, 102) };

    static final Color[] orange_sequential = {
        new Color(127,39,4),    // 9
        new Color(166,54,3),    // 8
        new Color(217,72,1),    // 7
        new Color(241,105,19),  // 6
        new Color(253,141,60) };// orange class 5
        
    static final Color[] blue_sequential = {
        new Color(8,48,107),    // 9
        new Color(8,81,156),    // 8
        new Color(33,113,181),  // 7
        new Color(66,146,198),  // 6
        new Color(107,174,214)};// blue class 5
        
    static final Color[] green_sequential = {
        new Color(0,68,27),     // 9
        new Color(0,109,44),    // 8
        new Color(35,139,69),   // 7
        new Color(65,171,93),   // 6
        new Color(116,196,118)};// green class 5
        
    static final Color[] colorbrewer_1 = {
            new Color(166, 206, 227),
            new Color(31, 120, 180),
            new Color(178, 223, 138),
            new Color(51, 160, 44),
            new Color(251, 154, 153),
            new Color(227, 26, 28),
            new Color(253, 191, 111),
            new Color(255, 127, 0),
            new Color(202, 178, 214) };

    static final Color[] colorbrewer_2 = {
            new Color(228, 26, 28),
            new Color(55, 126, 184),
            new Color(77, 175, 74),
            new Color(152, 78, 163),
            new Color(255, 127, 0),
            new Color(166, 86, 40),
            new Color(153, 153, 153),
            new Color(247, 129, 191),
            new Color(50, 50, 50) };

    static final Color[] colorbrewer_3 = {
            new Color(141, 211, 199),
            new Color(255, 255, 179),
            new Color(190, 186, 218),
            new Color(251, 128, 114),
            new Color(128, 177, 211),
            new Color(253, 180, 98),
            new Color(179, 222, 105),
            new Color(252, 205, 229),
            new Color(217, 217, 217) };

    static final Color[] trischeme_1 = {
            new Color(255, 0, 0),
            new Color(0, 153, 153),
            new Color(159, 238, 0),
            new Color(166, 0, 0) };
    static final Color[] trischeme_2 = {
            new Color(0, 235, 235),
            new Color(255, 170, 0),
            new Color(255, 0, 0),
            new Color(0, 99, 99) };
    static final Color[] trischeme_3 = {
            new Color(0, 155, 149),
            new Color(255, 169, 0),
            new Color(253, 0, 6),
            new Color(0, 101, 97) };

    static final Color[] similar_1 = {
            new Color(240, 0, 29),
            new Color(255, 103, 0),
            new Color(19, 0, 131),
            new Color(159, 0, 19) };
    static final Color[] similar_2 = {
            new Color(126, 7, 169),
            new Color(213, 0, 101),
            new Color(66, 18, 175),
            new Color(82, 2, 110) };
    static final Color[] similar_3 = {
            new Color(0, 142, 155),
            new Color(152, 237, 0),
            new Color(166, 54, 3),
            new Color(0, 129, 10) };

    static final Color[] sentiment = {
            new Color(217, 72, 1), // Orange Class7
            new Color(253, 141, 60), // Orange Class5
            new Color(150, 150, 150), // Grey Class5
            new Color(107, 174, 214), // Blue Class5
            new Color(33, 113, 181) }; // Blue Class7   

    static final Color[] sentiment2 = {
            new Color(35,139,69), // Green Class7
            new Color(116,196,118), // Green Class5
            new Color(150, 150, 150), // Grey Class5
            new Color(251,106,74), // Red Class5
            new Color(203,24,29) }; // Red Class7
    
    static final Color[] redblueblack = {
            new Color(255,0,0),
            new Color(0,0,255),
            new Color(0,0,0)
    };
    
    static final Color[] blueredblack = {
            new Color(33,113,181),  
            new Color(203,24,29),
            new Color(0,0,0)
    };

}
