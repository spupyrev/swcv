package knapsack.strategies;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import knapsack.core.KnapsackInstance;
import knapsack.core.KnapsackStrategy;
import knapsack.core.ValueDensity;

/**
 * Breath-first-search state space traversal with branch and bounds pruning.
 * 
 * @author vasek
 *
 */
public final class BranchAndBounds implements KnapsackStrategy {
	
	private int expandedStates = 0;
	
	/**
	 * Knapsack instance which is being solved.
	 */
	private final KnapsackInstance instance;
	
	/**
	 * Descending sorted collection of value densities.
	 */
	private final List<ValueDensity> valueDensities;
	
	/**
	 * Best cost found so far.
	 */
	private int bestCostFound = 0;
	
	/**
	 * Best configuration found so far.
	 */
	private int[] bestConfigurationFound;
	
	/**
	 * Priority queue.
	 */
	private LinkedList<KnapsackState> queue;
	
	/**
	 * Constructor.
	 * 
	 * @param instance which is being solved 
	 */
	public BranchAndBounds(KnapsackInstance instance) {
		super();
		this.instance = instance;
		this.bestConfigurationFound = new int[instance.getCount()];
		this.valueDensities = new ArrayList<ValueDensity>();
		this.queue = new LinkedList<KnapsackState>();
		
		for (int i = 0; i < instance.getCount(); i++) {
			final int cost = instance.getCosts()[i];
			final int weight = instance.getWeights()[i];
			final ValueDensity pair = new ValueDensity(cost, weight, i);
			valueDensities.add(pair);
		}
		
		Collections.sort(valueDensities);
		
		final int[] rootConfiguration = new int[instance.getCount()];
		Arrays.fill(rootConfiguration, 0);
		final KnapsackState root = new KnapsackState(rootConfiguration, 0);
		
		queue.offer(root);
	}

	@Override
	public void run() {
		if (instance.isTrivial()) {
			bestCostFound = instance.getCosts()[instance.getCount() - 1];
			return;
		}
		
		while (!queue.isEmpty()) {
			final KnapsackState current = queue.poll();
			expandedStates++;
			
			if (!current.isPromising()) {
				continue;
			}
			
			for (KnapsackState neigbor : current.expand()) {
				queue.offer(neigbor);
			}
			
			
			if (current.getValue() > bestCostFound) {
				bestCostFound = current.getValue();
				bestConfigurationFound = current.getConfiguration();
			}
		}				
	}

	@Override
	public int getKnapsackCost() {
		return bestCostFound;
	}

	@Override
	public int[] getKnapsackConfiguration() {
		return bestConfigurationFound;
	}

	@Override
	public KnapsackInstance getInstance() {
		return instance;
	}
	
	/**
	 * Utility class that represents one state in state space
	 * traversal of the knapsack problem.
	 * 
	 * @author vasek
	 *
	 */
	private class KnapsackState implements Comparable<KnapsackState> {
		
		private int[] configuration;
		private int level;
				
		/**
		 * Constructor.
		 * @param configuration of the current state
		 * @param level in the state space tree or count of fixed values
		 */
		public KnapsackState(int[] configuration, int level) {
			super();
			this.configuration = configuration;
			this.level = level;
		}		

		/**
		 * @return configuration of this state
		 */
		public int[] getConfiguration() {
			return configuration;
		}

		/**
		 * Return true when value is promising - which means potential value is better than best cost found so far
		 * and capacity wasn't reached.
		 * 
		 * @return true when node is promising, false otherwise
		 */
		public boolean isPromising() {
			return ((bestCostFound < getMaximumPotentialValue()) && (getWeight() <= instance.getCapacity())); 
		}

		/**
		 * @return maximal potential of this node
		 */
		public double getMaximumPotentialValue() {
			double maximumPotentialValue = getValue();
			double currentWeight = getWeight();
			
			if (currentWeight > instance.getCapacity()) {
				return 0;
			}
		
			for (int i = level; i < configuration.length; i++) {
				final double cost = valueDensities.get(i).getCost();
				final double weight = valueDensities.get(i).getWeight();
				
				if ((currentWeight + weight) <= instance.getCapacity()) {
					maximumPotentialValue += cost;
					currentWeight += weight;
				} else {
					final double rest = instance.getCapacity() - currentWeight;
					maximumPotentialValue += (rest * (cost / weight));
					break;
				}				
			}
			
			return maximumPotentialValue;
		}
		
		/**
		 * @return value of the current node
		 */
		public int getValue() {
			int sum = 0;
			
			for (int i = 0; i < level; i++) {
				sum += configuration[i] * valueDensities.get(i).getCost();
			}
			
			return sum;
		}
		
		/**
		 * @return weight of the current node
		 */
		public int getWeight() {
			int weight = 0;
			
			for (int i = 0; i < level; i++) {
				weight += configuration[i] * valueDensities.get(i).getWeight();
			}
			
			return weight;
		}
		
		/**
		 * Generates direct successor in state space tree.
		 *  
		 * @return successors of current node
		 */
		public List<KnapsackState> expand() {
			final List<KnapsackState> children = new ArrayList<KnapsackState>();
			
			if (level < configuration.length) {
				final KnapsackState right = new KnapsackState(configuration, level + 1);
				final int[] leftConfiguration = configuration.clone();
				leftConfiguration[level] = 1;
				final KnapsackState left = new KnapsackState(leftConfiguration, level + 1);
				
				children.add(left);
				children.add(right);
			}
			
			return children;
		}
		
		@Override
		public String toString() {
			final StringBuilder sb = new StringBuilder();
			
			sb.append("{");
			sb.append(getValue());
			sb.append(", ");
			sb.append(getWeight());
			sb.append(", ");
			sb.append(getMaximumPotentialValue());
			sb.append("}");
			
			return sb.toString();
		}

		@Override
		public int compareTo(final KnapsackState other) {
			final double max1 = this.getMaximumPotentialValue();
			final double max2 = other.getMaximumPotentialValue();
			
			return Double.compare(max2, max1);
		}	
	}

	@Override
	public int getNumberOfStatesExpanded() {
		return expandedStates;
	}
}
