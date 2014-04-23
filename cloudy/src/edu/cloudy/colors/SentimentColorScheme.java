package edu.cloudy.colors;

import java.awt.Color;

import edu.cloudy.nlp.Word;

public class SentimentColorScheme implements IColorScheme
{
	private Color[] colorSet = sentiment;
	@Override
	public Color getColor(Word word)
	{
		double sentiValue = word.getSentiValue();
		if (sentiValue < -.5){
			return colorSet[4];
		}else if(sentiValue < 0){
			return colorSet[3];
		}else if(sentiValue == 0){
			return colorSet[2];
		}else if (sentiValue > 0.5){
			return colorSet[0];
		}else{
			return colorSet[1];
		}
	}

}