package neighbors;

import model.Calculator;

public class NeighborhoodTemplate {

	public final int[][] offsetCoordinates;
	
	public final double[] distances;
	
	/** The results of applying the distance weighting function to the distance of each
	 *  cell in the block from the central cell.<br>
	 *  These are unlikely to change during a simulation.*/
	public final double[] unweightedDistanceScores;
	/** The results of applying the score weighting function to the unweighted distance scores. */
	public final double[] weightedDistanceScores;
	
	public final double[] cumulativeWeightedDistanceScores;
	
	public NeighborhoodTemplate(
			double neighborhoodRadius, 
			double distanceSelf, 
			double cellWidth, 
			double distanceParamA,
			double distanceParamB, 
			double distanceScoreWeight)
	{
		offsetCoordinates = buildOffsetCoordinates(neighborhoodRadius, cellWidth, distanceSelf, distanceParamA, distanceParamB);
		distances = buildDistances(cellWidth, distanceSelf);
		unweightedDistanceScores = Calculator.exponentialDistanceScore(distances, distanceParamA, distanceParamB);
		weightedDistanceScores = Calculator.normalize(unweightedDistanceScores,	1d - distanceScoreWeight, 1d);
		cumulativeWeightedDistanceScores = Calculator.cumulativeSum(weightedDistanceScores);
	}
	
	private int[][] buildOffsetCoordinates(double neighborhoodRadius, double cellWidth, double distanceSelf, double distanceParamA, double distanceParamB){
		
		int gridMaxDim = 2 + (int)Math.ceil(neighborhoodRadius / cellWidth);
		
		int[][] tempOffsets = new int[(1 + 2 * gridMaxDim) * (1 + 4 * gridMaxDim)][2];
		int count = 0;
		
		for(int col = -gridMaxDim; col <= gridMaxDim; col++)
		for(int row = -gridMaxDim; row <= gridMaxDim; row++){
			
//			double distance = dist(row, col) * cellWidth;
			double distance = Calculator.dist(row, col, cellWidth);
			 
			if(distance <= neighborhoodRadius){
				tempOffsets[count][0] = row;
				tempOffsets[count][1] = col;
				count++;
			}
		}
		
		int[][] outputCoordinates = new int[count][2];
		
		for(int i = 0; i < count; i++){
			outputCoordinates[i][0] = tempOffsets[i][0];
			outputCoordinates[i][1] = tempOffsets[i][1];
		}
		return outputCoordinates;
	}

	private double[] buildDistances(double cellWidth, double distanceSelf){
		double[] outputDist = new double[offsetCoordinates.length];
		for(int i = 0; i < offsetCoordinates.length; i++){
//			double distance = cellWidth * dist(offsetCoordinates[i][0], offsetCoordinates[i][1]); 
			double distance = Calculator.dist(offsetCoordinates[i][0], offsetCoordinates[i][1], cellWidth); 
			if(distance == 0)
				outputDist[i] = distanceSelf;
			else
				outputDist[i] = distance;
		}
		return outputDist;
	}
	
//	private double dist(int row, int col){
//		return Math.sqrt((double)(row * row) + (double)(col * col));
//	}
}
