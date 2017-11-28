package submodels;

import cell.Cell;
import cell.LocalCell;
import cern.jet.random.Uniform;
import model.Model;
import neighbors.NeighborhoodTemplate;
import utils.CompactQueue;

public class Dispersal {

	/** Reclassify overwintering beetles as emerging beetles. */
	public void initializeDispersal(Model model){
		for(int row = 0; row < model.parameters.nRows; row++)
		for(int col = 0; col < model.parameters.nCols; col++){
			model.cells[row][col].emerge();
		}
	}

	/** Reset the counts of outgoing beetles in the remote cells to zero */
	public void resetOutgoingRemoteCells(Model model){
		for(int quadrant = 0; quadrant < 8; quadrant++)
		for(int row = 0; row < model.remoteSectors.get(quadrant).length; row++)
		for(int col = 0; col < model.remoteSectors.get(quadrant)[0].length; col++)
				model.remoteSectors.get(quadrant)[row][col].reset();		
	}
	
	/** Send emerging beetles from the input cell to a randomly chosen neighbor.
	 * 	The neighbor's tally of arriving beetles is incremented by the number of beetles sent.
	 * @param cell  The cell that will send beetles to a recipient. 
	 * @param model 
	 * @param template  */
	public void disperse(LocalCell cell, int nPackets, Uniform unif, NeighborhoodTemplate template){
		if(cell.censusDispersingBeetles()[0] > 0){
			CompactQueue cq = new CompactQueue(cell.sendBeetles(), nPackets);
			for(int packet = 0; packet < nPackets; packet++){
				Cell recipient = cell.getRandomWeightedRecipient(unif, template);
				recipient.receiveBeetles(cq.next());
			}
		}
	}
	
	/** Reclassify arriving beetles in the local cells as overwintering beetles. <br>
	 *  Clear the arriving beetle counts from the remote cells. */
	public void finalizeDispersal(Model model){
		for(int row = 0; row < model.parameters.nRows; row++)
		for(int col = 0; col < model.parameters.nCols; col++){
			model.cells[row][col].overwinter(model.parameters.beetleReproductionRate);
		}
		for(int quadrant = 0; quadrant < 8; quadrant++){
			for(int row = 0; row < model.remoteSectors.get(quadrant).length; row++)
			for(int col = 0; col < model.remoteSectors.get(quadrant)[0].length; col++){
				model.remoteSectors.get(quadrant)[row][col].reset();
			}
				
		}
	}
}
