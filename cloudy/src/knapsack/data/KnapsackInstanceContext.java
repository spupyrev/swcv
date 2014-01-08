package knapsack.data;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import knapsack.core.KnapsackInstance;

/**
 * Default implementation of {@link InstanceContext}.
 * 
 * @author vasek
 *
 */
public final class KnapsackInstanceContext implements InstanceContext {
	
	private static final String FILE_SEPARATOR = System.getProperty("file.separator");
	private static final String USER_DIR = System.getProperty("user.dir");
	private static final File INSTANCES = new File(USER_DIR + FILE_SEPARATOR + "resources" + FILE_SEPARATOR + "knapsack" + FILE_SEPARATOR + "inst");
	private static final File SOLUTIONS = new File(USER_DIR + FILE_SEPARATOR + "resources" + FILE_SEPARATOR + "knapsack" + FILE_SEPARATOR + "sol");
	private static final String DATA_SEPARATOR = " ";
	private final Map<Integer, List<KnapsackInstance>> instancesMap = new HashMap<Integer, List<KnapsackInstance>>();
	private static KnapsackInstanceContext uniqueInstance;
	
	/**
	 * Constructor. 
	 * 
	 * @param solutionAvailable if solution is in external file
	 * 
	 * @throws IOException when e.g. file not found
	 */
	private KnapsackInstanceContext(final boolean solutionAvailable) throws IOException {
		
		for (File file : INSTANCES.listFiles()) {
			if (file.isDirectory()) {
				continue;
			}
			final BufferedReader reader = new BufferedReader(new FileReader(file));
			
			String line;
			List<KnapsackInstance> instances = new ArrayList<KnapsackInstance>();
			KnapsackInstance instance = null;
			
			while ((line = reader.readLine()) != null) {
				final String[] data = line.split(DATA_SEPARATOR);
				
				final int id = Integer.parseInt(data[0]);
				final int count = Integer.parseInt(data[1]);
				final int capacity = Integer.parseInt(data[2]);
				final int[] weights = new int[count];
				final int[] costs = new int[count];
				
				for (int n = 0, w = 3, c = 4; n < count; w += 2, c += 2, n++) {
					weights[n] = Integer.parseInt(data[w]);
					costs[n] = Integer.parseInt(data[c]);
				}
				
				instance = new KnapsackInstance(id, count, capacity, weights, costs);
				instances.add(instance);
			}
			
			instancesMap.put(instance.getCount(), instances);			
			
			reader.close();
		}
		
		if (solutionAvailable) {
			for (File file : SOLUTIONS.listFiles()) {
				if (file.isDirectory()) {
					continue;
				}
				final BufferedReader reader = new BufferedReader(new FileReader(file));
				
				String line;
				int record = 0;
				List<KnapsackInstance> instances = null;
				while ((line = reader.readLine()) != null) {
					final String[] data = line.split(DATA_SEPARATOR);
					final int id = Integer.parseInt(data[0]);
					final int count = Integer.parseInt(data[1]);
					final int optimalCost = Integer.parseInt(data[2]);
					final int[] optimalConfiguration = new int[count];
					
					if (instances == null) {
						instances = instancesMap.get(count);
					}
					
					for (int n = 0, i = 4; n < count; n++, i++) {
						optimalConfiguration[n] = Integer.parseInt(data[i]);
					}
					
					final KnapsackInstance instance = instances.get(record);
	
					assert (id == instance.getId());
					
					instance.setSolutionCost(optimalCost);
					instance.setSolutionConfiguration(optimalConfiguration);				
					record++;				
				}
				
				reader.close();
		}
		}
	}
	
	/**
	 * Access method.
	 * 
	 * @param solutionAvailable if solutions are available in external file
	 * @return instances context
	 * @throws IOException when file with instances not found
	 */
	public static KnapsackInstanceContext getContext(final boolean solutionAvailable) throws IOException {
		if (uniqueInstance == null) {
			uniqueInstance = new KnapsackInstanceContext(solutionAvailable);			
		}
		
		return uniqueInstance;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public List<KnapsackInstance> getInstances(final int size) {
		return instancesMap.get(size);
	}
	
}
