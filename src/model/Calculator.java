package model;

import java.util.ArrayList;
import java.util.List;

import cell.RemoteCell;

public class Calculator {

	
	public static double calculateAttractiveness(int nTrees, int nBeetles, int maxBeetlesPerTree){
		int maxCapacity = nTrees * maxBeetlesPerTree;
	}
	
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

	/** Transform a 1D array of doubles into a new 1D array of doubles
	 *  with values between the input min and max values.
	 * @param input the original 1D array
	 * @param min minimum value for the output array
	 * @param max maximum value for the output array
	 * @return	 */
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
	public static List<int[]> getLocalSectorCoordsFromRemoteSector(Model model){
		List<int[]> quadrantCoordsOut = new ArrayList<int[]>(8);
		
		int nRows = model.parameters.nRows;
		int nCols = model.parameters.nCols;
		int quadWidth = model.remoteSectorWidth;
		
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
	
	
	/** Build an array of RemoteCells to populate a model's sectors. */
	public static RemoteCell[][] buildRemoteSector(int sectorWidth, int sector, int nRows, int nCols){
		int nRow = 0;
		int nCol = 0;
		/* If the sector is diagonal, it is a square based on the 
		 * neighborhood radius */
		if(sector % 2 == 0){
			nRow = nCol = sectorWidth;
		} else if(sector == 1 | sector == 5){
			/* North and south neighborhoods. */
			nRow = sectorWidth;
			nCol = nCols;
		} else {
			/* East and west neighborhoods. */
			nRow = nRows;
			nCol = sectorWidth;
		}

		RemoteCell[][] toAdd = new RemoteCell[nRow][nCol];

		for(int row = 0; row < nRow; row++) for(int col = 0; col < nCol; col++){	
			toAdd[row][col] = new RemoteCell();
		}	
		
		return toAdd;
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
		int quadWidth = model.remoteSectorWidth;
		
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
	
	
	
	/** Determine which sector an input row and column number belong to. */
	public static int determineRemoteSector(int row, int col, int nRows, int nCols){
		
		/* If the coordinate is north of the model grid: */
		if(row >= nRows){
			if(col < 0) return 0;
			else if(col >= nCols) return 2;
			else return 1;
		}
		
		/* If the coordinate is south of the model grid: */
		else if(row < 0){
			if(col < 0) return 6;
			else if(col > nCols) return 4;
			else return 5;
		}
		
		/* If the coordinate is the easter edge */
		else if(col >= nCols) return 3;
		
		/* If the coordinate is the western edge: */
		return 7;
	}

	/** Return the width, in cells, of the neighborhoods of remote cells surrounding the model's grid of local cells. */
	public static int maxLocalNeighborhoodWidth(double neighborhoodRadius, double cellWidth){
		return (int)Math.floor(neighborhoodRadius / cellWidth);
	}
	
	/** Return a coordinate transformed into the coordinate system of a remote sector.
	 * @param coord The row or column coordinate
	 * @param maxCoord The max value the coordinate can be within the main model grid.
	 * @param sectorWidth the width of the remote sector
	 * @return a coordinate in the coordinate system of the remote sector	 */
	public static int determineSectorCoordinate(int coord, int maxCoord, int sectorWidth){
		/* If the coordinate is greater than the size of main model grid: */
		if(coord >= maxCoord){
			return coord - maxCoord;
		/* If the coordinate is negative */
		} else if(coord < 0){
			return sectorWidth + coord;
		/* If the coordinate falls within the range of the main model grid. */
		} else return coord;
	}


	
	/** Calculate the cumulative sums of a 1D array of doubles.
	 * @param inputArray the doubles to sum
	 * @return an array in which each element is the sum 
	 * of all elements of the input array with lower index */
	public static double[] cumulativeSum(double[] inputArray){
		double[] outputArray = new double[inputArray.length];
		double sum = 0;
		for(int i = 0; i < inputArray.length; i++){
			sum += inputArray[i];
			outputArray[i] = sum;
		}
		return outputArray;
	}
	
	
	/** Determine which model, in a rectangular grid of models, a group of
	 * 	beetles should be sent to.
	 * 
	 * @param remoteSector the remote sector code for the sending model
	 * @param nModelRows the number of rows of models in the simulation grid
	 * @param nModelCols the number of columns of models in the simulation grid
	 * @return The coordinates of the model object to receive the beetles. */
	public static int[] getRecipientModelOffsetCoords(int remoteSector, int row, int col, int nModelRows, int nModelCols){
		int[] offset = new int[2];
		switch(remoteSector){
		case 0:		offset[0] =  1; offset[1] = -1; break;	/* NW sector */
		case 1: 	offset[0] =  1; offset[1] =  0; break;	/*  N sector */
		case 2:		offset[0] =  1; offset[1] =  1; break;	/* NE sector */
		case 3:		offset[0] =  0; offset[1] =  1; break;	/*  E sector */
		case 4:		offset[0] = -1; offset[1] =  1; break;	/* SE sector */
		case 5:		offset[0] = -1; offset[1] =  0; break;	/*  S sector */
		case 6:		offset[0] = -1; offset[1] = -1; break;	/* SW sector */
		default:	offset[0] =  0; offset[1] = -1; break;	/*  W sector */
		}
		
		int[] out = new int[2];
		
		out[0] = (offset[0] + row + nModelRows) % nModelRows;
		out[1] = (offset[1] + col + nModelCols) % nModelCols;
		
		return out;
	}
}
