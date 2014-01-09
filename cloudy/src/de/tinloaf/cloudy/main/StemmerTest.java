package de.tinloaf.cloudy.main;

import de.tinloaf.cloudy.text.LovinsStemmer;
import de.tinloaf.cloudy.text.PlingStemmer;
import de.tinloaf.cloudy.text.PorterStemmer;
import de.tinloaf.cloudy.text.PreKrovetzStemmer;
import de.tinloaf.cloudy.text.AbstractStemmer;

public class StemmerTest {
	public static void main(String[] args){
		AbstractStemmer a = new LovinsStemmer();
		AbstractStemmer b = new PorterStemmer();
		AbstractStemmer c = new PreKrovetzStemmer();
		AbstractStemmer d = new PlingStemmer();
		String[] prestemmed={"dance","dances","dancing","dancer","danced","movie","movies","building","biulds","biult","matrix","matrices","visualization","visualizes","visualized"};
		for(String bb:prestemmed){
			System.out.println("Lovins:"+ bb+"==>"+a.stem(bb)+"==>"+a.stem(c.stem(bb)));
			System.out.println("Porter:"+ bb+"==>"+b.stem(bb)+"==>"+b.stem(c.stem(bb)));
			//System.out.println("Pling:"+ bb+"==>"+d.stem(bb)+"==>"+d.stem(c.stem(bb)));

		}
	}
}
