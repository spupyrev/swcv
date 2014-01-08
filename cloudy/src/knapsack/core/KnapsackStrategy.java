package knapsack.core;

/**
 * Common interface to various strategies that solves knapsack problem.
 * There are just following two requirements on these:
 *  
 * <ul>
 * 	<li>compute cost of the solution </li>
 *  <li>compute configuration of the solution (not necessarily to be optimal)</li>
 * </ul>
 * 
 * @author vasek
 *
 */
public interface KnapsackStrategy extends Runnable {

	/**
	 * Returns cost of the solution (not necessarily to be optimal).
	 * 
	 * @return cost of the solution
	 */
	int getKnapsackCost();
	
	/**
	 * Returns configuration of the solution (not necessarily to be optimal).
	 * @return configuration of the solution
	 */
	int[] getKnapsackConfiguration();
		
	/**
	 * @return instance which is being solved
	 */
	KnapsackInstance getInstance();
	
	/**
	 * @return number of expanded state before solution was found.
	 */
	int getNumberOfStatesExpanded();
}
