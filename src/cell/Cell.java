package cell;

public interface Cell {

	public double attractiveness = 0;
	
	/** Increment the number of arriving beetles.
	 * @param n the number of arriving beetles */
	public void receiveBeetles(int n);
	public int[] censusDispersingBeetles();
}
