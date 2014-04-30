package edu.cloudy.nlp;

import java.awt.Point;
import java.util.HashSet;
import java.util.Set;

public class Word implements Comparable<Word>
{
	public String word;
	public String stem;
	public double weight;
	public double sentiValue;
	
	// debug info
	public int posCount,negCount,neuCount;
	// debug info
	public double totalCount;

	private Set<Integer> sentences;
	private Set<Point> coordinate;

	public Word(String word, double weight)
	{
		this.word = word;
		this.stem = null;
		this.weight = weight;
		this.sentiValue = 0;
		this.coordinate = new HashSet<Point>();
		this.sentences = new HashSet<Integer>();
	}

	public void addCoordinate(Point id)
	{
		coordinate.add(id);
	}

	public void addCoordinate(Set<Point> id)
	{
		coordinate.addAll(id);
	}

	public void addSentence(int id)
	{
		sentences.add(id);
	}

	public void addSentences(Set<Integer> ids)
	{
		sentences.addAll(ids);
	}

	public Set<Point> getCoordinates()
	{
		return coordinate;
	}

	public Set<Integer> getSentences()
	{
		return sentences;
	}

	@Override
	public int hashCode()
	{
		return word.hashCode();
	}

	@Override
	public boolean equals(Object o)
	{
		if (!(o instanceof Word))
		{
			return false;
		}

		return word.equals(((Word) o).word);
	}

	@Override
	public int compareTo(Word o)
	{
		return Double.compare(weight, o.weight);
	}

	public void setSentiValue(double sentiValue)
	{
		this.sentiValue = sentiValue;
	}

	public double getSentiValue()
	{
		return this.sentiValue;
	}

	public void setSentCount(int posCount, int negCount, int neuCount, double totalCount)
	{
		this.posCount = posCount;
		this.negCount = negCount;
		this.neuCount = neuCount;
		this.totalCount = totalCount;
	}
}
