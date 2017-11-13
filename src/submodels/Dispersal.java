package submodels;

import cell.Cell;
import cell.LocalCell;
import model.Model;
import neighbors.NeighborhoodTemplate;

public class Dispersal {

	/** Reclassify overwintering beetles as emerging beetles. */
	public void initializeDispersal(Model model){
		for(int row = 0; row < model.parameters.nRows; row++)
		for(int col = 0; col < model.parameters.nCols; col++){
			model.cells[row][col].emerge();
		}
		/* TODO: For now just move the arriving beetles to the emerging
		 * beetle counts of remote cells. */
		for(int quadrant = 0; quadrant < 8; quadrant++)
		for(int row = 0; row < model.remoteCells.get(quadrant).length; row++)
		for(int col = 0; col < model.remoteCells.get(quadrant)[0].length; col++)
				model.remoteCells.get(quadrant)[row][col].reset();		
	}
	
	/** Send beetles from a specified cell to a randomly chosen neighbor
	 * of the specified cell.
	 * @param cell  The cell that beetles will disperse from
	 * @param model 
	 * @param template  */
	public void disperse(LocalCell cell, Model model, NeighborhoodTemplate template){
		Cell recipient = cell.getRandomWeightedRecipient(model, template);
		recipient.receiveBeetles(cell.sendBeetles());
	}
	
	/** Reclassify arriving beetles in the local cells as overwintering beetles. <br>
	 *  Clear the arriving beetle counts from the remote cells. */
	public void finalizeDispersal(Model model){
		for(int row = 0; row < model.parameters.nRows; row++)
		for(int col = 0; col < model.parameters.nCols; col++){
			model.cells[row][col].overwinter(model.parameters.beetleReproductionRate);
		}
		for(int quadrant = 0; quadrant < 8; quadrant++){
			for(int row = 0; row < model.remoteCells.get(quadrant).length; row++)
			for(int col = 0; col < model.remoteCells.get(quadrant)[0].length; col++){
				model.remoteCells.get(quadrant)[row][col].reset();
			}
				
		}
	}
}
