package knapsack.strategies;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import knapsack.core.KnapsackInstance;
import knapsack.core.KnapsackStrategy;
import knapsack.core.ValueDensity;

/**
 * This is a simple heuristic strategy - cost/weight density implementation to solve
 * knapsack problem.
 * 
 * @author vasek
 *
 */
public final class GreedyAlgorithm implements KnapsackStrategy {
	
	private KnapsackInstance instance;
	private int knapsackCost;
	private int[] knapsackConfiguration;
	private List<ValueDensity> costsWeights;
	private int expandedStates = 0;

	/**
	 * Constructor.
	 * 
	 * @param instance one particular instance of knapsack problem
	 */
	public GreedyAlgorithm(final KnapsackInstance instance) {
		this.instance = instance;
		this.knapsackConfiguration = new int[instance.getCount()]; 
		this.costsWeights = new ArrayList<ValueDensity>();
		for (int i = 0; i < instance.getCount(); i++) {
			final int cost = instance.getCosts()[i];
			final int weight = instance.getWeights()[i];
			final ValueDensity pair = new ValueDensity(cost, weight, i);
			costsWeights.add(pair);
		}
	}
	
	@Override
	public void run() {
		Collections.sort(costsWeights);
		
		int weight = 0;
		int cost = 0;
		int i = 0;
		while (weight <= instance.getCapacity()) {
			
			ValueDensity pair;
			expandedStates++;
			try {
				 pair = costsWeights.get(i);
			} catch (IndexOutOfBoundsException exception) {
				// actually do nothing - it means that all items can be stored in knapsack
				break;
			}
			if ((weight + pair.getWeight()) > instance.getCapacity()) {
				break;
			} else {
				weight += pair.getWeight();
				cost += pair.getCost();
				knapsackConfiguration[pair.getOriginalIndex()] = 1;
			}
			
			i++;
		}
		
		knapsackCost = cost;
	}
	
	@Override
	public int getKnapsackCost() {
		return knapsackCost;
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
