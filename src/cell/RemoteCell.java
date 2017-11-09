package cell;

public class RemoteCell implements Cell {

	int nArrivingBeetles;
	
	
	//TODO: a stub for now...
	double occupancyScore = 1d;
	
	@Override
	public void receiveBeetles(int n) {
		nArrivingBeetles += n;
	}

	@Override
	public double getOccupancyScore() {
		return occupancyScore;
	}

	public int[] censusDispersingBeetles(){
		return new int[] {0, nArrivingBeetles};
	}
	
}
