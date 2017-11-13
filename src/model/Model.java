package model;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import cell.Cell;
import cell.LocalCell;
import cell.RemoteCell;
import cern.jet.random.Uniform;
import cern.jet.random.engine.MersenneTwister;
import cern.jet.random.engine.RandomEngine;
import neighbors.NeighborhoodTemplate;
import reporters.ConsoleReporters;
import reporters.NetCDFReporters;
import submodels.Dispersal;
import ucar.ma2.InvalidRangeException;

public class Model {

	public final LocalCell[][] cells;
	public final List<RemoteCell[][]> remoteCells;
	public final int remoteQuadrantMaxWidth;

	public Parameters parameters;
	public NetCDFReporters ncdfReporter;

	public NeighborhoodTemplate neighborhoodTemplate;

	private RandomEngine re;
	public final Uniform unif;

	public Dispersal dispersal;

	/** row and column min/max coords.
	 * Convenience fields, so that we don't have 
	 * to re-calculate them multiple times.
	 * index 0 = row min
	 * index 1 = row max
	 * index 2 = col min
	 * index 3 = col max*/
	private List<int[]> quadrantCoords;

	public Model(String parameterFilename){
		parameters = new Parameters(parameterFilename);
		ncdfReporter = new NetCDFReporters(this);
		ncdfReporter.createFile(this);

		remoteQuadrantMaxWidth = Calculator.maxLocalNeighborhoodWidth(parameters.neighborhoodRadius, parameters.cellWidth);

		quadrantCoords = Calculator.getLocalCoordsFromRemoteQuadrant(this);

		/* Initialize the random number generator */
		if(parameters.randomSeed < 0){
			re = new MersenneTwister((int)System.currentTimeMillis());
		} else	re = new MersenneTwister(parameters.randomSeed);
		unif = new Uniform(re);

		neighborhoodTemplate = new NeighborhoodTemplate(
				parameters.neighborhoodRadius, 
				parameters.distanceSelf, 
				parameters.cellWidth, 
				parameters.distanceParamA, 
				parameters.distanceParamB, 
				parameters.distanceScoreWeight);

		/* Ideally these would be immutable in the sense that the references to
		 * specific cells within them shouldn't be allowed to change. */
		cells = new LocalCell[parameters.nRows][parameters.nCols];
		remoteCells = new ArrayList<RemoteCell[][]>();
		buildCells();

		dispersal = new Dispersal();
	}

	/** Reinitialize the model's random number generator with the given input int. */
	public void initializeRandomEngine(int rand){re = new MersenneTwister(rand);	}


	public void simulate() throws IOException, InvalidRangeException{
		for(int year = 0; year < parameters.nYears; year++){
			disperseBeetles();
			dispersal.finalizeDispersal(this);
			
			ConsoleReporters.censusLocalCells(this);
			ConsoleReporters.censusRemoteDispersingBeetles(this);
			ncdfReporter.step(this);
		}
		ncdfReporter.saveFile(this);
	}

	private void buildCells(){

		for(int i = 0; i < parameters.nRows; i++)
			for(int j = 0; j < parameters.nCols; j++){
				cells[i][j] = new LocalCell(parameters.initialBeetlesPerCell);
			}

		/* Build the remote cell neighborhoods. */
		for(int quadrant = 0; quadrant < 8; quadrant ++){

			int nRow = 0;
			int nCol = 0;
			/* If the quadrant is diagonal, it is a square based on the 
			 * neighborhood radius */
			if(quadrant % 2 == 0){
				nRow = nCol =remoteQuadrantMaxWidth;
			} else if(quadrant == 1 | quadrant == 5){
				/* North and south neighborhoods. */
				nRow =remoteQuadrantMaxWidth;
				nCol = parameters.nCols;
			} else {
				/* East and west neighborhoods. */
				nRow = parameters.nRows;
				nCol =remoteQuadrantMaxWidth;
			}

			RemoteCell[][] toAdd = new RemoteCell[nRow][nCol];

			for(int row = 0; row < nRow; row++) for(int col = 0; col < nCol; col++){	
				toAdd[row][col] = new RemoteCell();
			}
			remoteCells.add(toAdd);
		}

		/* Build the neighborhoods for each local cell. 
		 * This has to happen after all the local and remote cells 
		 * are already created.*/
		for(int row = 0; row < parameters.nRows; row++) for(int col = 0; col < parameters.nCols; col++){
			cells[row][col].buildNeighborhood(row, col, neighborhoodTemplate, this);
		}
	}

	/** Initialize the dispersal season by staging overwintering beetles for emergence and dispersal. <br?
	 *  Loop through all the model's grid cells and disperse beetles to target cells. */
	public void disperseBeetles(){
		dispersal.initializeDispersal(this);
		for(int row = 0; row < parameters.nRows; row++) for(int col = 0; col < parameters.nCols; col++){
			dispersal.disperse(cells[row][col], this, neighborhoodTemplate);
		}
	}

	/** Allocate arriving beetles from a neighboring model's set of remote 
	 *  cells to the appropriate local cells in this model's grid. */
	public void receiveBeetlesFromRemote(int remoteQuadrant, int[][] nBeetles){
		int minRow = quadrantCoords.get(remoteQuadrant)[0];
		int maxRow = quadrantCoords.get(remoteQuadrant)[1];
		int minCol = quadrantCoords.get(remoteQuadrant)[2];
		int maxCol = quadrantCoords.get(remoteQuadrant)[3];
		
		int sum = 0;
		for(int row = minRow; row <= maxRow; row++)
		for(int col = minCol; col <= maxCol; col ++){
			cells[row][col].receiveBeetles(nBeetles[row - minRow][col - minCol]);
			sum += nBeetles[row - minRow][col - minCol];
		}
		
		
//		System.out.println(sum + " remote beetles received from remote model's quadrant " + remoteQuadrant);
//		System.out.print("");
	}

	/** Return an array of beetle counts for a neighboring model to add to the
	 *  corresponding local cells in its grid.
	 * @param quadrant
	 * @param nBeetles
	 * @return	 */
	public int[][] sendLocalBeetlesToRemote(int quadrant){
		
		int nRows = remoteCells.get(quadrant).length;
		int nCols = remoteCells.get(quadrant)[0].length;
		int[][] toSend = new int[nRows][nCols];
		
		int sum = 0;
		
		for(int row = 0; row < nRows; row++) for(int col = 0; col < nCols; col++){
			toSend[row][col] = remoteCells.get(quadrant)[row][col].censusDispersingBeetles()[1];
			sum += remoteCells.get(quadrant)[row][col].censusDispersingBeetles()[1];
		}
//		System.out.println(sum + " beetles sent to remote model from quadrant " + quadrant);
		
		return toSend;
	}

	/** Return the cell (local or remote) located at the specified coordinates. */
	public Cell getCell(int[] coords){return getCell(coords[0], coords[1]);}
	/** Return the cell (local or remote) located at the specified row and column coordinates. */
	public Cell getCell(int row, int column){

		/* If the cell is local */
		if(row >= 0 & row < parameters.nRows & column >= 0 & column < parameters.nCols){
			return cells[row][column];
		} else
		{
			/* Otherwise determine the quadrant. */
			int quadrant = Calculator.determineQuadrant(row, column, parameters.nRows, parameters.nCols);
			int quadRow = Calculator.determineQuadrantCoordinate(row, parameters.nRows, parameters.neighborhoodRadius, parameters.cellWidth);
			int quadColumn = Calculator.determineQuadrantCoordinate(column, parameters.nCols, parameters.neighborhoodRadius, parameters.cellWidth);
			return remoteCells.get(quadrant)[quadRow][quadColumn];
		}
	}


}
