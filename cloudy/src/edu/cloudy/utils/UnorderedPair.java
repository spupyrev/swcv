package edu.cloudy.utils;

public class UnorderedPair<A,B> {
    private A first;
    private B second;

    public UnorderedPair(A first, B second) {
    	super();
    	this.first = first;
    	this.second = second;
    }

    public int hashCode() {
    	int hashFirst = first != null ? first.hashCode() : 0;
    	int hashSecond = second != null ? second.hashCode() : 0;

    	return (hashFirst + hashSecond) * (hashFirst + hashSecond);
    }

    public boolean equals(Object other) {
    	if (other instanceof UnorderedPair) {
    		UnorderedPair<A, B> otherPair = (UnorderedPair<A, B>) other;
    		return 
    		(((  this.first == otherPair.first ||
    			( this.first != null && otherPair.first != null &&
    			  this.first.equals(otherPair.first))) &&
    		 (	this.second == otherPair.second ||
    			( this.second != null && otherPair.second != null &&
    			  this.second.equals(otherPair.second))) )
    			  ||
    		((  this.first == otherPair.second ||
      			( this.first != null && otherPair.second != null &&
      			  this.first.equals(otherPair.second))) &&
      		 (	this.second == otherPair.first ||
      			( this.second != null && otherPair.first != null &&
      			  this.second.equals(otherPair.first)))));
    	}

    	return false;
    }

    public String toString()
    { 
           return "(" + first + ", " + second + ")"; 
    }

    public A getFirst() {
    	return first;
    }

    public void setFirst(A first) {
    	this.first = first;
    }

    public B getSecond() {
    	return second;
    }

    public void setSecond(B second) {
    	this.second = second;
    }
}
