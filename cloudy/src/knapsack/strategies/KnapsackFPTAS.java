package knapsack.strategies;

import knapsack.core.KnapsackInstance;
import knapsack.core.KnapsackStrategy;

/**
 * This class represents simple approximative algorithm (FPTAS) for a knapsack problem.
 * 
 * @author vasek
 *
 */
public final class KnapsackFPTAS implements KnapsackStrategy {
	
	private final KnapsackInstance original;
	private final KnapsackInstance reduced;
	private DynamicProgramming delegate;
	private double scalingFactor = 2;
	
	/**
	 * Constructor.
	 * 
	 * @param instance which is being solved
	 * @param scalingFactor how much we want to be precise, bigger factor means coarser solution
	 * @throws CloneNotSupportedException when cloning of instances cannot be performed
	 */
	public KnapsackFPTAS(final KnapsackInstance instance, final int scalingFactor) throws CloneNotSupportedException {
		super();
		this.original = instance;
		this.reduced = (KnapsackInstance) instance.clone();
		this.scalingFactor = scalingFactor;
	}

	@Override
	public void run() {
		reduced.setCapacity((int) (original.getCapacity() / scalingFactor));
		for (int i = 0; i < original.getCount(); i++) {
			reduced.getWeights()[i] = (int) Math.ceil(original.getWeights()[i] / scalingFactor);
		}
		
		delegate = new DynamicProgramming(reduced);
		delegate.run();
	}

	@Override
	public int getKnapsackCost() {
		return delegate.getKnapsackCost();
	}

	public int getKnapsackWeight() {
		return delegate.getKnapsackWeight();
	}
	
	@Override
	public int[] getKnapsackConfiguration() {
		return delegate.getKnapsackConfiguration();
	}

	@Override
	public KnapsackInstance getInstance() {
		return original;
	}

	@Override
	public int getNumberOfStatesExpanded() {
		return delegate.getNumberOfStatesExpanded();
	}
}
