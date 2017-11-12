package cell;

public class RemoteCell implements Cell {

	int nEmergingBeetles = 0;
	int nArrivingBeetles = 0;
	
	public void receiveBeetles(int n) {nArrivingBeetles += n;}

	public int[] censusDispersingBeetles(){return new int[] {nEmergingBeetles, nArrivingBeetles};}
	
	// TODO: eventually this will go away once remote cell neighborhoods are
	// associated with real model instances.
	public void reset(){
		nEmergingBeetles = nArrivingBeetles;
		nArrivingBeetles = 0;
	}
}
