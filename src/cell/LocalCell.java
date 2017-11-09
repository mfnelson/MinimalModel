package cell;

import model.Model;
import neighbors.Neighborhood;
import neighbors.NeighborhoodTemplate;

public class LocalCell implements Cell{

	private int nTrees;
	private int nBeetles;
	
	int nEmergingBeetles;
	int nArrivingBeetles;

	private Neighborhood neighborhood;
	
	
	//TODO a stub for now...
	double occupancyScore = 1d;
	
	public LocalCell(int nTrees, 
			int nBeetles, 
			int row, int col, 
			NeighborhoodTemplate template)
	{
		this.nTrees = nTrees;
		this.nBeetles = nBeetles;
		
//		neighborhood = new Neighborhood(template, row, col);
	}

	public void buildNeighborhood(int row, int col, NeighborhoodTemplate template, Model model){
		neighborhood = new Neighborhood(template, row, col, model);
	}
	
	public void updateScore(NeighborhoodTemplate template){
		neighborhood.updateScores(template);
	}
	
	/** Protects the private beetle and tree count fields. */
	public int[] census(){
		return new int[] {nTrees, nBeetles};
	}
	
	public int[] censusDispersingBeetles(){
		return new int[] {nEmergingBeetles, nArrivingBeetles};
	}
	
	public Cell getRandomWeightedRecipient(Model model){
		return neighborhood.getWeightedRandomCell(model.unif);
	}
	
	public void emerge(){
		nEmergingBeetles = nBeetles;
		nBeetles = 0;
	}
	
	public int sendBeetles(){
		int toSend = nEmergingBeetles;
		nEmergingBeetles = 0;
		return toSend;
	}
	
	@Override
	public void receiveBeetles(int n) {
		nArrivingBeetles += n;
	}

	@Override
	public double getOccupancyScore() {
		return occupancyScore;
	}

	

}
