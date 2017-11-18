package reporters;

import cell.LocalCell;
import model.Model;

public class ConsoleReporters {

	/** Get the total count of overwintering beetles. */
	public static void censusLocalCells(Model model){
		
		int nBeetles = 0;
		
		for(int row = 0; row < model.parameters.nRows; row++)
		for(int col = 0; col < model.parameters.nCols; col++){
				nBeetles += model.cells[row][col].census();
			}
		System.out.println(" Total overwintering beetles = " + nBeetles);
	}

	/** Get the total count of arriving beetles for eacch remote quadrant. */
	public static void censusRemoteDispersingBeetles(Model model){
		int[] quadrantCounts = new int[8];
		
		for(int quadrant = 0; quadrant < 8; quadrant++){
			for(int row = 0; row < model.remoteSectors.get(quadrant).length; row++)
			for(int col = 0; col < model.remoteSectors.get(quadrant)[0].length; col++){
				quadrantCounts[quadrant] += model.remoteSectors.get(quadrant)[row][col].censusDispersingBeetles()[1];
			}
			
			System.out.println("Quadrant " + quadrant + " beetles arriving at remote cells = " + quadrantCounts[quadrant]);
		}
	}
	
	/** Get the total counts of beetles arriving at and dispersing from the local cells. */
	public static void censusLocalDispersingBeetles(Model model){
		int nEmerging = 0;
		int nArriving = 0;
		
		for(LocalCell[] row : model.cells)
			for(LocalCell cell : row){
		
				int[] counts = cell.censusDispersingBeetles();
				nEmerging += counts[0];
				nArriving += counts[1];
			}
		System.out.println("Total emerging beetles = " + nEmerging + " Total arriving beetles = " + nArriving);
	
	}

}
