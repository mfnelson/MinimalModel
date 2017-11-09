package reporters;

import cell.LocalCell;
import model.Model;

public class ConsoleReporters {

	
	public static void censusLocalCells(Model model){
		
		int nTrees = 0;
		int nBeetles = 0;
		
		for(LocalCell[] row : model.cells)
			for(LocalCell cell : row){
		
				int[] counts = cell.census();
				nTrees += counts[0];
				nBeetles += counts[1];
			}
		System.out.println("Total trees = " + nTrees + " Total beetles = " + nBeetles);
	}

	public static void censusRemoteDispersingBeetles(Model model){
		int[] quadrantCounts = new int[8];
		
		for(int quadrant = 0; quadrant < 8; quadrant++){
			for(int row = 0; row < model.remoteCells.get(quadrant).length; row++)
			for(int col = 0; col < model.remoteCells.get(quadrant)[0].length; col++){
				quadrantCounts[quadrant] += model.remoteCells.get(quadrant)[row][col].censusDispersingBeetles()[1];
			}
			
			System.out.println("Quadrant " + quadrant + " arriving beetles = " + quadrantCounts[quadrant]);
		}
	}
	
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
