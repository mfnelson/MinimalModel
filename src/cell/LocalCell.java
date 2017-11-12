package cell;

import model.Model;
import neighbors.Neighborhood;
import neighbors.NeighborhoodTemplate;

public class LocalCell implements Cell{

	private int nBeetles = 0;
	
	int nEmergingBeetles = 0;
	int nArrivingBeetles = 0;

	private Neighborhood neighborhood;
	
	
	public LocalCell(int nBeetles){this.nBeetles = nBeetles;}

	public void buildNeighborhood(int row, int col, NeighborhoodTemplate template, Model model){
		neighborhood = new Neighborhood(template, row, col, model);
	}
	
	public int census(){return nBeetles;}
	
	public int[] censusDispersingBeetles(){
		return new int[] {nEmergingBeetles, nArrivingBeetles};
	}
	
	public Cell getRandomWeightedRecipient(Model model, NeighborhoodTemplate template){
		return neighborhood.getWeightedRandomCell(model.unif, template);
	}
	
	public int sendBeetles(){
		int toSend = nEmergingBeetles;
		nEmergingBeetles = 0;
		return toSend;
	}
	
	public void emerge(){
		nEmergingBeetles = nBeetles;
		nBeetles = 0;
	}
	
	public void overwinter(double nOffspring){
		nBeetles = (int)(nOffspring * nArrivingBeetles);
		nArrivingBeetles = 0;
	}
	
	@Override
	public void receiveBeetles(int n) {
		nArrivingBeetles += n;
	}

}
