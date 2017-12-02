package cell;

public class RemoteCell implements Cell {

	/** Count of beetles arriving from other cells. */
	public int nArrivingBeetles = 0;
	/** Attractiveness, based on cell capacity and current occupancy. */
	public double attractiveness = 1d;
	
	public void receiveBeetles(int n) {nArrivingBeetles += n;}

	public int[] censusDispersingBeetles(){return new int[] {0, nArrivingBeetles};}
	
	// TODO: eventually this will go away once remote cell neighborhoods are
	// associated with real model instances.
	/** Reset the arriving beetles count to zero. */ 
	public void reset(){
		nArrivingBeetles = 0;
	}
	
	
}
