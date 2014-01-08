package knapsack.strategies;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import knapsack.core.KnapsackInstance;
import knapsack.core.KnapsackStrategy;

/**
 * This class represents dynamic programming approach for knapsack problem.
 * 
 * @author vasek
 *
 */
public final class DynamicProgramming implements KnapsackStrategy {
	
	private final KnapsackInstance instance;
	private final int[] weights;
	private final int[] costs;
	private final int[][] table;
	private final boolean[][] chosen;
	private int expandedStates = 0;
	
	/**
	 * Constructor.
	 * 
	 * @param instance which is being solved
	 */
	public DynamicProgramming(KnapsackInstance instance) {
		super();
		this.instance = instance;		
		this.weights = instance.getWeights();		
		this.costs = instance.getCosts();		
		this.table = new int[instance.getCount() + 1][instance.getCapacity() + 1];
		this.chosen = new boolean[instance.getCount() + 1][instance.getCapacity() + 1];
	}

	@Override
	public void run() {
		for (int w = 0; w <= instance.getCapacity(); w++) {
			table[0][w] = 0;
		}
		
		for (int i = 1; i <= instance.getCount(); i++) {
			table[i][0] = 0;
		}
		
		for (int i = 1, index = 0; i <= instance.getCount(); i++, index++) {
			for (int w = 0; w <= instance.getCapacity(); w++) {
				expandedStates++;
				if (weights[index] <= w) {
					if ((costs[index] + table[i - 1][w - weights[index]]) > table[i - 1][w]) {
						table[i][w] = costs[index] + table[i - 1][w - weights[index]];
						chosen[i][w] = true;
					} else {
						table[i][w] = table[i - 1][w];
						chosen[i][w] = false;
					}
				} else {
					table[i][w] = table[i - 1][w];
					chosen[i][w] = false;
				}
			}
		}
		
		// TODO remove consistency check
		int[] elements = this.getKnapsackConfiguration();
		int usedWeight = 0;
		for (int i = 0; i < elements.length; i++) {
			usedWeight += weights[elements[i]];
		}
		assert(usedWeight <= instance.getCapacity());
	}

	@Override
	public int getKnapsackCost() {
		final int w = table[0].length - 1;
		final int n = table.length - 1;
		
		return table[n][w];		
	}
	
	private void constructSubSolution(int i, int w, List<Integer> out) {
		if (i == 0) {
			return;
		}
		
		if (chosen[i][w]) {
			out.add(i-1);
			constructSubSolution(i-1, w - weights[i-1], out);
		} else {
			constructSubSolution(i-1, w, out);
		}
	}
	
	@Override
	public int[] getKnapsackConfiguration() {
		List<Integer> resList = new LinkedList<Integer>();
		
		this.constructSubSolution(table.length - 1, table[0].length - 1, resList);
		int[] res = new int[resList.size()];
		int i = 0;
		for (Integer x : resList) {
			res[i++] = x;
		}
		
		return res;
	}

	public int getKnapsackWeight() {
		int[] packedElements = this.getKnapsackConfiguration();
		int ret = 0;
		
		for (int i = 0; i < packedElements.length; i++) {
			ret += this.weights[packedElements[i]];
		}
		
		return ret;
	}
	
	@Override
	public KnapsackInstance getInstance() {
		return instance;
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		
		for (int row = 0; row <= instance.getCount(); row++) {
			sb.append(Arrays.toString(table[row]));
			sb.append("\n");
		}
		
		return sb.toString();
	}

	@Override
	public int getNumberOfStatesExpanded() {
		return expandedStates;
	}

}
