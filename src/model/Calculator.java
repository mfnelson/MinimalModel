package model;

import java.util.ArrayList;
import java.util.List;

public class Calculator {

	/** Create a sequence of integers with an interval of 1
	 * 
	 * @param start starting integer
	 * @param end ending integer
	 * @return
	 */
	public static int[] seq(int start, int end){
		int[] sequence = new int[Math.abs(end - start) + 1];
		
		int interval = 1;
		if(start > end) interval = -1;
		sequence[0] = start;
		for(int i = 1; i < sequence.length; i++){
			sequence[i] = sequence[i - 1] + interval;
		}
		return sequence;
	}
	
	
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
	
	/** Calculate the remote models' coordinate limits for sending beetles from the local model.
	 * 
	 * @param quadrant the LOCAL quadrant number
	 * @param model the local model
	 * @return coordinates, local coordinates to which to receive beetles from remote models grids.
	 * 				Indexed by the local quadrant. 
	 * 			i.e. index 0 represents the coords of the local NW corner and the remote model's quadrant 4.	 */
	public static List<int[]> getLocalCoordsFromRemoteQuadrant(Model model){
		List<int[]> quadrantCoordsOut = new ArrayList<int[]>(8);
		
		int nRows = model.parameters.nRows;
		int nCols = model.parameters.nCols;
		int quadWidth = model.remoteQuadrantMaxWidth;
		
		int rowMinSouth = 0;
		int rowMaxSouth = quadWidth - 1;
		int rowMinNorth = nRows - quadWidth;
		int rowMaxNorth = nRows - 1;
		
		int colMinWest = 0;
		int colMaxWest = quadWidth - 1;
		int colMinEast = nCols - quadWidth;
		int colMaxEast = nCols - 1;
		
		/* 0: remote model quad 0: NW corner, 	local SE corner */
		quadrantCoordsOut.add(new int[]{rowMinSouth, rowMaxSouth, colMinEast, colMaxEast});
		/* 1: remote model quad 1: N edge,		local S edge */
		quadrantCoordsOut.add(new int[]{rowMinSouth, rowMaxSouth, colMinWest, colMaxEast});
		/* 2: remote model quad 2: NE corner,	local SW corner */
		quadrantCoordsOut.add(new int[]{rowMinSouth, rowMaxSouth, colMinWest, colMaxWest});
		/* 3: remote model quad 3: E edge,		local W edge */
		quadrantCoordsOut.add(new int[]{rowMinSouth, rowMaxNorth, colMinWest, colMaxWest});		
		/* 4: remote model quad 4: SE corner,	local NW corner*/
		quadrantCoordsOut.add(new int[]{rowMinNorth, rowMaxNorth, colMinWest, colMaxWest});
		/* 5: remote model quad 5: S edge,		local N edge */
		quadrantCoordsOut.add(new int[]{rowMinNorth, rowMaxNorth, colMinWest, colMaxEast});
		/* 6: remote model quad 6: SW corner,	local NE corner */
		quadrantCoordsOut.add(new int[]{rowMinNorth, rowMaxNorth, colMinEast, colMaxEast});
		/* 7: remote model quad 7: W edge,		local E edge */ 
		quadrantCoordsOut.add(new int[]{rowMinSouth, rowMaxNorth, colMinEast, colMaxEast});
		return quadrantCoordsOut;
	}
	
	/** Calculate the local coordinate limits to receive beetles from remote models.
	 * 
	 * @param quadrant the  quadrant number
	 * @param model the local model
	 * @return coordinates, remote coordinates to which to send beetles.
	 * 				Indexed by the remote models' grid quadrant numbering system. 
	 * 			i.e. index 0 represents the remote model's NW corner (Q0) and the local model's quadrant 4.	 */
	public static List<int[]> getRemoteQuadrantCoords(Model model){
		List<int[]> quadrantCoordsOut = new ArrayList<int[]>(8);
		
		int nRows = model.parameters.nRows;
		int nCols = model.parameters.nCols;
		int quadWidth = model.remoteQuadrantMaxWidth;
		
		int rowMinSouth = 0;
		int rowMaxSouth = quadWidth;
		int rowMinNorth = nRows - quadWidth - 1 ;
		int rowMaxNorth = nRows - 1;
		
		int colMinWest = 0;
		int colMaxWest = quadWidth;
		int colMinEast = nCols - quadWidth - 1;
		int colMaxEast = nCols - 1;
		
		/* 0: ext. quad 0: NW corner, 	int. qudarant 4: SE corner*/
		quadrantCoordsOut.add(new int[]{rowMinSouth, rowMaxSouth, colMinEast, colMaxEast});
		/* 1: ext. quad 1: N edge, 		int. quad 5: S edge */
		quadrantCoordsOut.add(new int[]{rowMinSouth, rowMaxSouth, colMinWest, colMaxEast});
		/* 2: ext. quad 2: NE corner, 	int. quad 6: SW corner */
		quadrantCoordsOut.add(new int[]{rowMinSouth, rowMaxSouth, colMinWest, colMaxWest});
		/* 3: ext. quad 3: E edge,		int. quad 7: W edge */ 
		quadrantCoordsOut.add(new int[]{rowMinSouth, rowMaxNorth, colMinWest, colMaxEast});		
		/* 4: ext. quad 4: SE corner,	int. quad 0: NW corner */
		quadrantCoordsOut.add(new int[]{rowMinNorth, rowMaxNorth, colMinWest, colMaxWest});
		/* 5: ext. quad 5: S edge,		int. quad 1: N edge */
		quadrantCoordsOut.add(new int[]{rowMinNorth, rowMaxNorth, colMinWest, colMaxEast});
		/* 6: ext. quad 6: SW corner,	int. quad 2: NE corner */
		quadrantCoordsOut.add(new int[]{rowMinNorth, rowMaxNorth, colMinEast, colMaxEast});
		/* 7: ext. quad 7: W edge, 		int. quad 3: E edge */
		quadrantCoordsOut.add(new int[]{rowMinSouth, rowMaxNorth, colMinEast, colMaxEast});
		
		return quadrantCoordsOut;
	}
	
	
	
	/** Determine which quadrant of remote cells an input row and column number belong to. */
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

	/** Return the width, in cells, of the neighborhoods of remote cells surrounding the model's grid of local cells. */
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
	
	
	public static double[] cumulativeSum(double[] inputArray){
		double[] outputArray = new double[inputArray.length];
		double sum = 0;
		for(int i = 0; i < inputArray.length; i++){
			sum += inputArray[i];
			outputArray[i] = sum;
		}
		return outputArray;
	}
	
	
	public static int[] getQuadrantOffsetCoords(int quadrant){
		int[] out = new int[2];
		
		switch(quadrant){
		case 0: out[0] =  1; out[1] = -1; break;
		case 1:	out[0] =  1; out[1] =  0; break; 
		case 2: out[0] =  1; out[1] =  1; break;
		case 3: out[0] =  0; out[1] =  1; break;
		case 4: out[0] = -1; out[1] =  1; break;
		case 5: out[0] = -1; out[1] =  0; break;
		case 6: out[0] = -1; out[1] = -1; break;
		default: out[0] =  0; out[1] = -1; break;
		}
		return out;
	}
}
