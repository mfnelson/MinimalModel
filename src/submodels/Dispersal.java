package submodels;

import cell.Cell;
import cell.LocalCell;
import model.Model;

public class Dispersal {

	public void disperse(LocalCell cell, Model model){
		Cell recipient = cell.getRandomWeightedRecipient(model);
		recipient.receiveBeetles(cell.sendBeetles());
	}
	
	
}
