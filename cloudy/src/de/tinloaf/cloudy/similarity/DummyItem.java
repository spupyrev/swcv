package de.tinloaf.cloudy.similarity;

/**
 * Just a dumb item to test whether or not LexRank actually works.
 * It simulates having a similarity function by just reading a value out of some
 * similarity matrix. If you're using multiple of these, it's probably best to
 * make sure they all point to the same matrix, or hilarity could ensue.
 */
public class DummyItem implements Similar<DummyItem> {
    int id;
    double[][] similarityMatrix;
    public DummyItem(int id, double[][] similarityMatrix) {
        this.id = id;
        this.similarityMatrix = similarityMatrix;
    }
    public double similarity(DummyItem other) {
        return similarityMatrix[id][other.id];
    }
}
