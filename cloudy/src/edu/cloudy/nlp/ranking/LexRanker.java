package edu.cloudy.nlp.ranking;


import java.util.Collections;
import java.util.List;

import java.util.ArrayList;

/**
 * An Implementation of the LexRank algorithm described in the paper
 * "LexRank: Graph-based Centrality as Salience in Text Summarization",
 * Erkan & Radev '04, with some of our own modifications.
 */
public class LexRanker {

    /**
     * Runs the LexRank algorithm over a set of data. The data must have a
     * similarity function, and it is assumed that the similarity function
     * is symmetric. (If this is not the case, the similarity matrix will
     * not be computed correctly.)
     *
     * @param data the data to rank.
     * @param similarityThreshold how similar two items must be to be considered
     * "connected". The LexRank paper suggests a value of 0.1.
     * @param continuous whether or not to use a continuous version of the
     * LexRank algorithm, If set to false, all similarity links above the
     * similarity threshold will be considered equal; otherwise, the similarity
     * scores are used. The paper authors note that non-continuous LexRank
     * seems to perform better.
     */
    public static <T extends Similar<T>> LexRankResults<T>
                             rank(List<T> data,
                                  double similarityThreshold,
                                  boolean continuous) {
        LexRankResults<T> results = new LexRankResults<T>();
        if (data.size() == 0) {
            return results;
        }
        double[][] similarities = similarityMatrix(data);
        double[][] transitionProbabilities =
            transitionProbabilities(similarities,
                                    similarityThreshold,
                                    continuous);

        // Build the neighbor graph for the results.
        for (int i = 0; i < data.size(); ++i) {
            for (int j = 0; j < data.size(); ++j) {
                if (transitionProbabilities[i][j] > 0) {
                    List<T> neighborList = results.neighbors.get(data.get(i));
                    if (neighborList == null) {
                        neighborList = new ArrayList<T>();
                    }
                    neighborList.add(data.get(j));
                    results.neighbors.put(data.get(i), neighborList);
                }
            }
        }

        double[] rankings = powerIteration(transitionProbabilities,
                                           data.size(),
                                           0.001,
                                           1000);

        // Now that we have the LexRank scores, arrange them for the results.
        List<RankPair<T>> tempList = new ArrayList<RankPair<T>>();
        for (int i = 0; i < data.size(); ++i) {
            results.scores.put(data.get(i), rankings[i]);
            tempList.add(new RankPair<T>(data.get(i), rankings[i]));
        }
        Collections.sort(tempList);
        Collections.reverse(tempList);
        for (RankPair<T> pair: tempList) {
            results.rankedResults.add(pair.data);
        }
        return results;
    }

    /** Internal class used for sorting data by LexRank score.*/
    private static class RankPair<T> implements Comparable<RankPair<T>> {
        T data;
        double score;
        public RankPair(T d, double s) {
            data = d;
            score = s;
        }
        public int compareTo(RankPair<T> other) {
            double diff = score - other.score;
            if (diff > 0.000001) {
                return 1;
            } else if (diff < -0.000001) {
                return -1;
            } else {
                return 0;
            }
        }
    }

    /**
     * Computes a similarity matrix given a set of data.
     * Assumes that the similarity function is symmetric.
     */
    private static <T extends Similar<T>> double[][]
                              similarityMatrix(List<T> data) {
        double[][] results = new double[data.size()][data.size()];
        for (int i = 0; i < data.size(); ++i) {
            for (int j = 0; j <= i; ++j) {
                results[i][j] = results[j][i] =
                    data.get(i).similarity(data.get(j));
            }
        }
        return results;
    }

    /**
     * Given a similarity matrix, computes the transition probability for a
     * random walker on a graph to go from any one node to any other node,
     * where all edges come from sufficiently high similarities between nodes.
     */
    private static double[][]
        transitionProbabilities(double[][] similarities,
                                double similarityThreshold,
                                boolean continuous) {
        double[][] probabilities =
            new double[similarities.length][similarities[0].length];
        for (int i = 0; i < similarities.length; ++i) {
            double sum = 0;
            for (int j = 0; j < similarities[i].length; ++j) {
                if (similarities[i][j] >= similarityThreshold) {
                    if (continuous) {
                        probabilities[i][j] = similarities[i][j];
                        sum += similarities[i][j];
                    } else {
                        probabilities[i][j] = 1;
                        sum += 1;
                    }
                } else {
                    probabilities[i][j] = 0;
                }
            }
            for (int j = 0; j < similarities[i].length; ++j) {
            	if (sum == 0.0) {
            		//throw new IllegalArgumentException("Division by zero.");
            		sum = 1; // does not matter, everything is zero, but must be non-zero.
            	}
            	
                probabilities[i][j] /= sum;
            }
        }
        return probabilities;
    }

    /** Multiplies two matrices. So Exciting!*/
    private static double[][] multMatrix(double[][] first,
                                         double[][] second) {
        if (first.length == 0 || second.length == 0) {
            return null;
        }
        if (first[0].length != second.length) {
            return null;
        }
        double[][] result = new double[first.length][second[0].length];
        for (int i = 0; i < first.length; ++i) {
            for (int j = 0; j < second[0].length; ++j) {
                double sum = 0;
                for (int k = 0; k < second.length; ++k) {
                    sum += first[i][k]*second[k][j];
                }
                result[i][j] = sum;
            }
        }
        return result;
    }

    /** Transposes a matrix. This is really complicated stuff. */
    private static double[][] transposeMatrix(double[][] matrix) {
        if (matrix.length == 0) {
            return null;
        }
        double[][] result = new double[matrix[0].length][matrix.length];
        for (int i = 0; i < result.length; ++i) {
            for (int j = 0; j < result[i].length; ++j) {
                result[i][j] = matrix[j][i];
            }
        }
        return result;
    }

    /**
     * Solves for an eigenvector of a stochastic matrix using the power
     * iteration algorithm.
     *
     * For future reference, when a paper writes "M^T", that does not mean "M
     * raised to the power of T," even if there is a variable called "t" right
     * there. Instead, it means "M transpose." Durrr.
     *
     * @param stochasticMatrix the matrix to get the first eigenvector of
     * @param size how many data we've got
     * @param epsilon power iteration will stop when the difference between
     * iterations is less than this.
     * @param maxIterations the maximum number of iterations for which this is
     * allowed to run. (Yeah, proper grammar right there)
     */
    private static double[] powerIteration(double[][] stochasticMatrix,
                                           int size,
                                           double epsilon,
                                           int maxIterations) {
        double[][] currentMatrix = transposeMatrix(stochasticMatrix);
        double[][] currentVector = new double[size][1];
        double[][] previousVector;
        for (int i = 0; i < size; ++i) {
            currentVector[i][0] = 1.0 / size;
        }
        //for(int i = 0; i < currentMatrix.length; i++)
        //    for(int j = 0; j < currentMatrix[0].length; j++)
        //        currentMatrix[i][j] = .15 / size + .85 * currentMatrix[i][j];
        for (int i = 0; i < maxIterations; ++i) {
            previousVector = currentVector;
            for(int a = 0; a < currentVector.length; a++) currentVector[a][0] *= .85;
            currentVector = multMatrix(currentMatrix, currentVector);
            for(int a = 0; a < currentVector.length; a++) currentVector[a][0] += .15/size;
            double error = 0;
            for (int j = 0; j < size; ++j) {
                error += Math.pow(currentVector[j][0]-previousVector[j][0], 2);
            }
            if (error < Math.pow(epsilon, 2)) {
                break;
            }
        }
        double[] result = new double[size];
        for (int i = 0; i < size; ++i) {
            result[i] = currentVector[i][0];
        }
        return result;
    }
}
