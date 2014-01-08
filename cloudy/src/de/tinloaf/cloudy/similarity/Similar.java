package de.tinloaf.cloudy.similarity;


/**
 * An interface describing things that can have similarity measures.
 *
 * @param T The type of objects this can be similar to. It's expected
 * that any object that implements this will be similar to at least its own
 * type.
 */
public interface Similar<T> {
    /**
     * Compute some meaurement of similarity between this object and some
     * other object. Returned similarity values should be in the range
     * [0, 1] where a similarity of 0 means that the two objects are completely
     * different, and a similarity of 1 means they are both the same.
     * Additionally, this operation should be symmetric, that is,
     * a.similarity(b) should equal b.similarity(a).
     */
    public double similarity(T other);
}
