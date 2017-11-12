package neighbors;

import java.util.Arrays;

import cell.Cell;
import cern.jet.random.Uniform;
import model.Model;

public class Neighborhood {

	Cell[] neighbors;
	
	public Neighborhood(
			NeighborhoodTemplate template, 
			int row, 
			int col, 
			Model model){
		neighbors = new Cell[template.distances.length];
		buildNeighbors(template, row, col, model);
	}
	
	private void buildNeighbors(NeighborhoodTemplate template, int row, int col, Model model){
		
		/* Build coordinates */
		for(int i = 0; i < template.distances.length; i++){
			int offsetRow = row + template.offsetCoordinates[i][0];
			int offsetColumn = col + template.offsetCoordinates[i][1];
			neighbors[i] = model.getCell(offsetRow, offsetColumn);
		}
	}
	
	public Cell getWeightedRandomCell(Uniform unif, NeighborhoodTemplate template){
		double rand = unif.nextDoubleFromTo(0d, template.cumulativeWeightedDistanceScores[template.unweightedDistanceScores.length - 1]);
		
		/* This was confusing to me so I needed to enumerate the possibilities.
		 * Per Javadoc:
		 * 
		 * Binary search returns the index of the input number if found.
		 * If not it returns (-(insertion point) - 1).
		 * Insertion point = the index of the first element greater than the key.
		 * 
		 * Tilde is bitwise complement: ~x equals (-x)-1  
		 * 
		 * 	scores[0] = 1, scores[1] = 2, scores[2] 3
		 * 	Case 1: rand = 0.5
		 * 			search returns  x = (-(0) - 1) = -1
		 * 							~x      =  0
		 * 							~abs(x) = -2
		 * 							abs(~x) =  0
		 * 			we want			x       =  0
		 * 	Case 2: rand = 1
		 * 			search returns:	x       =  0
		 * 							~x      = -1
		 * 							~abs(x) =  1
		 * 							abs(~x) =  1
		 * 			we want:		x       =  1
		 * 	Case 3:	rand = 1.5
		 * 			search returns 	x  =  (-(1) - 1) = -2
		 * 							~x 		=  1
		 * 							~abs(x) = -3
		 * 							abs(~x) =  1
		 * 			we want			x  		=  1
		 * 	Case 4:	rand = 2		
		 * 			search returns 	x       =  1
		 *							~x 		= -2
		 * 							~abs(x) = -2
		 * 							abs(~x) =  2
		 * 			we want			x  		=  2
		 */
		int index = Math.abs(~Arrays.binarySearch(template.cumulativeWeightedDistanceScores, rand));
		return neighbors[index];
	}
}
