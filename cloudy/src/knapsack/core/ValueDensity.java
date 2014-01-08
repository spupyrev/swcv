package knapsack.core;

/**
 * Utility class - it helps with initial sorting in a manner of better cost/weight 
 * density, it also holds related cost and weight. 
 *  
 * @author vasek
 *
 */
public class ValueDensity implements Comparable<ValueDensity> {

		private int cost;
		private int weight;
		private int originalIndex;
		
		/**
		 * Constructor.
		 * 
		 * @param cost cost related to weight
		 * @param weight weight related to cost
		 * @param index index in original data
		 */
		public ValueDensity(final int cost, final int weight, final int index) {
			this.cost = cost;
			this.weight = weight;
			this.originalIndex = index;
		}
		
		/**
		 * @return cost 
		 */
		public int getCost() {
			return cost;
		}
		
		/**
		 * @return weight
		 */
		public int getWeight() {
			return weight;
		}
		

		/**
		 * @return index in original data
		 */
		public int getOriginalIndex() {
			return originalIndex;
		}

		@Override
		public int compareTo(ValueDensity other) {

			final double ratio1 = this.getCost() / this.getWeight();
			final double ratio2 = other.getCost() / other.getWeight();

			if (ratio1 <= ratio2) {
				return 1;
			} else {
				return -1;
			}
		}
		
		@Override
		public String toString() {
			return String.valueOf(cost / weight);
		}

	}