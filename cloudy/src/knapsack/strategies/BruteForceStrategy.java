package knapsack.strategies;

import knapsack.core.KnapsackInstance;
import knapsack.core.KnapsackStrategy;

import java.util.Arrays;

/**
 * Simplest strategy - it just tries every single combination of configuration
 * variable - obtained solution is then optimal.
 * 
 * @author vasek
 *
 */
public final class BruteForceStrategy implements KnapsackStrategy {

	private KnapsackInstance instance;
	private int[] x;
	private int knapsackCost = Integer.MIN_VALUE;
	private int[] knapsackConfiguration;
	private int expandedStates = 0;
	
	/**
	 * Constructor. 
	 * @param instance instance of a knapsack problem to be solved
	 */
	public BruteForceStrategy(final KnapsackInstance instance) {
		this.instance = instance;
		this.x = new int[instance.getCount()];
		this.knapsackConfiguration = new int[instance.getCount()];
		Arrays.fill(x, 0);
		Arrays.fill(knapsackConfiguration, 0);
	}
	
	@Override
	public void run() {
		while (generateState()) {
			final int weight = computeWeight();
			
			if (weight > instance.getCapacity()) {
				continue;
			}
			
			final int cost = computeCost();
			
			if (cost > knapsackCost) {
				knapsackCost = cost;
				knapsackConfiguration = x.clone();
			}
		}
	}

	@Override
	public int getKnapsackCost() {
		return knapsackCost;
	}
	
	/**
	 * Generate new state - it's stored in the x variable - and it works 
	 * as a simple counter.
	 * 
	 * @return true if whole state space hasn't been explored, false otherwise
	 */
	private boolean generateState() {
		expandedStates++;
		int zeros = 0;
		for (int i = 0; i < x.length; i++) {
						
			if (x[i] == 0) {
				x[i] = 1;
				break;
			} else {
				zeros++;
			}
		}
		
		if (zeros == x.length) {
			return false;
		}
		
		for (int i = 0; i < zeros; i++) {
			x[i] = 0;
		}	
		
		return true;
	}
	
	/**
	 * Computes cost based on configuration variable - x. 
	 * 
	 * @return cost of the current solution
	 */
	private int computeCost() {
		int cost = 0;
		
		for (int i = 0; i < x.length; i++) {
			final int c = instance.getCosts()[i]; 
			cost += x[i] * c;
		}
		
		return cost;
	}
	
	
	/**
	 * Computes weight based on configuration variable - x.
	 * 
	 * @return weight of the current solution
	 */
	private int computeWeight() {
		int weight = 0;
		
		for (int i = 0; i < x.length; i++) {
			final int w = instance.getWeights()[i];
			weight += x[i] * w;
		}
		
		return weight;
	}

	@Override
	public int[] getKnapsackConfiguration() {
		return knapsackConfiguration;
	}

	@Override
	public KnapsackInstance getInstance() {
		return instance;
	}

	@Override
	public int getNumberOfStatesExpanded() {
		return expandedStates;
	}

}
