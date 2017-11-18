package cell;

import cern.jet.random.Uniform;
import model.Model;
import neighbors.Neighborhood;
import neighbors.NeighborhoodTemplate;

public class LocalCell implements Cell{

	/** Count of overwintering beetles. */
	private int nBeetles = 0;
	
	/** Count of beetles emerging in the spring in order to disperse to other cells. */
	int nEmergingBeetles = 0;
	/** Count of beetles arriving from other cells. */
	int nArrivingBeetles = 0;

	/** The collection of cells that this cell may disperse emerging beetles to. */
	private Neighborhood neighborhood;
	
	public LocalCell(int nBeetles){this.nBeetles = nBeetles;}

	/** Populate the list of neighboring cells with the appropriate references. */
	public void buildNeighborhood(int row, int col, NeighborhoodTemplate template, Model model){
		neighborhood = new Neighborhood(template, row, col, model);
	}
	
	/** Return the number of overwintering beetles. */
	public int census(){return nBeetles;}

	/** Return the number of emerging (index 0) and arriving beetles (index 1). */
	public int[] censusDispersingBeetles(){
		return new int[] {nEmergingBeetles, nArrivingBeetles};
	}
	
	/** Randomly choose a cell from this cell's neighborhood.  Weights are related
	 *  to the neighbors' distances from this cell.
	 * @param model
	 * @param template
	 * @return */
	public Cell getRandomWeightedRecipient(Uniform unif, NeighborhoodTemplate template){
		return neighborhood.getWeightedRandomCell(unif, template);
	}
	
	/** Return the number of emerging beetles.  Reset the count of emerging beetles to zero. */
	public int sendBeetles(){
		int toSend = nEmergingBeetles;
		nEmergingBeetles = 0;
		return toSend;
	}
	
	/** Reclassify overwintering beetles to emerging beetles. */
	public void emerge(){
		nEmergingBeetles = nBeetles;
		nBeetles = 0;
	}
	
	/** Move arriving beetles to the count of overwintering beetles. <br>
	 *  Reset the count of arriving beetles.
	 * @param nOffspring reproduction rate of beetles. */
	public void overwinter(double nOffspring){
		nBeetles = (int)(nOffspring * nArrivingBeetles);
		nArrivingBeetles = 0;
	}
	
	/** Increment the number of arriving beetles.
	 * @param n the number of arriving beetles */
	@Override
	public void receiveBeetles(int n) {
		nArrivingBeetles += n;
	}
	
	/** Increment the number of emerging beetles by n beetles. 
	 * @param n number of beetles to add	 */
	public void addEmergingBeetles(int n){
		nEmergingBeetles += n;
	}
}
