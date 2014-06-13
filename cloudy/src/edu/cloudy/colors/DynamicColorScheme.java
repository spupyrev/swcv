package edu.cloudy.colors;

import java.awt.Color;

import edu.cloudy.nlp.Word;
import edu.cloudy.nlp.Word.DocIndex;

public class DynamicColorScheme implements IColorScheme
{
	private Color[] colorSet;
	
	public DynamicColorScheme(String colorSetName){
		if (colorSetName.equals("REDBLACKBLUE2")){
			colorSet = redblueblack2;
		}else{
			colorSet = redblueblack;
		}
	}

	@Override
	public Color getColor(Word word)
	{
		if (word.documentIndex == DocIndex.First){
			return colorSet[0];
		}else if(word.documentIndex == DocIndex.Second){
			return colorSet[1];
		}else{
			return colorSet[2];
		}
	}
}
