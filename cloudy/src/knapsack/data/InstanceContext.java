package knapsack.data;

import java.util.List;

import knapsack.core.KnapsackInstance;

/**
 * This class represents the utility tier between raw data and {@link KnapsackInstance} 
 * objects. It's main purpose is to make possible to obtain instances of one 
 * particular size.
 * 
 * @see #getInstances(int)
 * 
 * @author vasek
 *
 */
public interface InstanceContext {

	/**
	 * Returns instances of particular size.
	 * 
	 * @param size size of the instances
	 * @return list of the instances which particular size
	 */
	List<KnapsackInstance> getInstances(int size);	
}
