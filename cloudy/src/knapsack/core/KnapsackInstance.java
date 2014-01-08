package knapsack.core;

/**
 * This class represents one instance of knapsack problem.
 * 
 * @author vasek
 *
 */
public final class KnapsackInstance implements Cloneable {
	
	private final int id;	
	private final int count;
	private final int[] weights;
	private final int[] costs;
	private int solutionCost;
	private int[] solutionConfiguration;
	private boolean trivial = false;
	private int capacity;
	
	/**
	 * Constructor.
	 * 
	 * @param id id of the instance
	 * @param capacity knapsack's maximal capacity
	 * @param count number of items
	 * @param weights weights of items
	 * @param costs costs of items
	 */
	public KnapsackInstance(final int id, final int count, final int capacity, final int[] weights, final int[] costs) {
		this.id = id;
		this.count = count;
		this.capacity = capacity;
		this.weights = weights;
		this.costs = costs;
		
		int counter = 0;
		for (int i = 0; i < count - 1; i++) {
			if (weights[i] == costs[i]) {
				counter++;
			}
		}
		
		if (counter == (count - 1) && (weights[weights.length - 1] == capacity)) {
			this.trivial = true;
		}
	}

	/**
	 * @return id of the instance
	 */
	public int getId() {
		return id;
	}

	/**
	 * @return number of items
	 */
	public int getCount() {
		return count;
	}
	
	/**
	 * @return knapsack's maximal capacity
	 */
	public int getCapacity() {
		return capacity;
	}
	
	/**
	 * @param capacity set knapsack's maximal capacity
	 */
	public void setCapacity(final int capacity) {
		this.capacity = capacity;
	}

	/**
	 * @return weights of items
	 */
	public int[] getWeights() {
		return weights;
	}

	/**
	 * @return costs of items
	 */
	public int[] getCosts() {
		return costs;
	}

	/**
	 * @return solution (optimal cost) of this instance
	 */
	public int getSolutionCost() {
		return solutionCost;
	}
	
	/**
	 * @param solutionCost solution (optimal cost) of this instance
	 */
	public void setSolutionCost(final int solutionCost) {
		this.solutionCost = solutionCost;
	}

	/**
	 * @return solution (optimal configuration) of this instance
	 */
	public int[] getSolutionConfiguration() {
		return solutionConfiguration;
	}
	
	/**
	 * @param solutionConfiguration solution (optimal configuration) of this instance
	 */
	public void setSolutionConfiguration(final int[] solutionConfiguration) {
		this.solutionConfiguration = solutionConfiguration;
	}
	
	/**
	 * @return whether this instance is trivial to solve, for example:
	 * <ul>
	 * 	<li> Same values except last one
	 * </ul>
	 */
	public boolean isTrivial() {
		return trivial;
	}

	@Override
	public Object clone() throws CloneNotSupportedException {		
		final KnapsackInstance clone = new KnapsackInstance(id, count, capacity, weights, costs);
		return clone;
	}	
	
}
