package model;

public class Calculator {

	public static double exponentialDistanceScore(double distance, double dispersalDistParamA, double dispersalDistParamB){
		return(Math.pow(dispersalDistParamA * distance, -dispersalDistParamB));
	}

	/**  Vectorized wrapper for {@link Calculator#exponentialDistanceScore} <br>Calculate the attractiveness of a cell to emerging beetles, based on distance from 
	 *   the source cell using the following formula: <br>
	 *   score = (paramA * e)<sup>(paramB * distance)</sup>
	 * 
	 * @param distance distance in meters from the source cell
	 * @param dispersalDistParamA
	 * @param dispersalDistParamB
	 * @return distance score
	 */
	public static double[] exponentialDistanceScore(double distances[], double dispersalDistParamA, double dispersalDistParamB){
		double[] scores = new double[distances.length];
		for(int i = 0; i < scores.length; i++){
			scores[i] = exponentialDistanceScore(distances[i], dispersalDistParamA, dispersalDistParamB);
		}
		return scores;
	}

	public static double[] normalize(double[] input, double min, double max){
		double[] output = new double[input.length];

		double max1 = 0;
		for(double d : input) max1 = Math.max(max1,  d);

		double range = max - min;

		if(max1 > 0){
			for(int i = 0; i < input.length; i++){
				output[i] = range * (input[i] / max1) + min;
			}
		}
		return output;
	}
	
	public static int determineQuadrant(int row, int column, int nRows, int nCols){
		if(row < 0){
			if(column >= nCols) 
				return 4;
			else if(column < 0) 
				return 6;
			else 
				return 5;
		} else 
			if(row >= nRows)
		{
			if(column >= nCols) 
				return 2;
			else if(column < 0) 
				return 0;
			else 
				return 1;
		} else 
			if(column < 0) 
				return 7;
			else 
  				return 3;
	}

	public static int maxLocalNeighborhoodWidth(double neighborhoodRadius, double cellWidth){
		return (int)Math.floor(neighborhoodRadius / cellWidth);
	}
	
	/**
	 * Note:	We could use the mod operator here to do this in one line, but in another language
	 * 			it might handle negative numbers differently from Java does things.
	 * @param coord
	 * @param nCoords
	 * @param quadrant
	 * @param neighborhoodRadius
	 * @param cellWidth
	 * @return
	 */
	public static int determineQuadrantCoordinate(int coord, int nCoords, double neighborhoodRadius, double cellWidth){
		int maxWidth = maxLocalNeighborhoodWidth(neighborhoodRadius, cellWidth);
		if(coord >= nCoords){
			return coord - nCoords;
		} else if(coord < 0){
			return maxWidth + coord;
		} else return coord;
	}

	public static int map2Dto1DCoords(int row, int column, int nRows, int nColumns){
		return column + row * nColumns;
	}
	
	public static int map1DToColumn(int coord, int nColumns){
		return coord % nColumns;
	}
	
	public static int map1DToRow(int coord, int column, int nColumns){
		return coord - column * nColumns;
	}

	public static int[] map1Dto2DCoords(int coord, int nColumns){
		int column = coord % nColumns;
		int row = coord - column * nColumns;
		return new int[]{row, column};
	}
}
