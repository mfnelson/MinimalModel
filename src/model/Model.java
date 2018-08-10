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
	
	
	/**
	 * list indices: <br>
	 * 	0:	NW sector
	 * 	1:	 N sector
	 * 	2:	NE sector
	 * 	3:	 E sector
	 *  4:	SE sector
	 *  5:	 S sector
	 *  6:	SW sector
	 *  7:	 W sector
	 * 
	 */
	public final List<RemoteCell[][]> remoteSectors;
	public final int remoteSectorWidth;

	public Parameters parameters;
	public NetCDFReporters ncdfReporter;

	public NeighborhoodTemplate neighborhoodTemplate;

	private RandomEngine re;
	public final Uniform unif;

	/** row and column min/max coords.<br>
	 * Convenience fields, so that we don't have 
	 * to re-calculate them multiple times.
	 * 
	 * list indices:<br>
	 * index 0: SE corner<br>
	 * index 1:  S edge<br>
	 * index 2:	SW corner<br>
	 * idnex 3:  W edge<br>
	 * index 4: NW corner<br>
	 * index 5:  N edge<br>
	 * index 6: NE corner<br>
	 * index 7:  E edge<br>
	 * 
	 * int[] indices:<br>
	 * index 0 = row min<br>
	 * index 1 = row max<br>
	 * index 2 = col min<br>
	 * index 3 = col max<br>*/
	private List<int[]> localSectorCellCoords;

	public Model(String parameterFilename){
		parameters = new Parameters(parameterFilename);
		ncdfReporter = new NetCDFReporters(this);
		ncdfReporter.createFile(this);

		remoteSectorWidth = Calculator.maxLocalNeighborhoodWidth(parameters.neighborhoodRadius, parameters.cellWidth);

		localSectorCellCoords = Calculator.getLocalSectorCoordsFromRemoteSector(this);

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
		remoteSectors = new ArrayList<RemoteCell[][]>();
		buildCells();
	}

	/** Reinitialize the model's random number generator with the given input int. */
	public void initializeRandomEngine(int rand){re = new MersenneTwister(rand);	}

	public void simulate() throws IOException, InvalidRangeException{
		updateNeighborScores();
		for(int year = 0; year < parameters.nYears; year++){
			disperseBeetles();
			Dispersal.finalizeDispersal(this);
			updateNeighborScores();
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

		/* Build the remote sectors. */
		for(int sector = 0; sector < 8; sector ++){
			remoteSectors.add(Calculator.buildRemoteSector(remoteSectorWidth, sector, parameters.nRows, parameters.nCols));
		}

		/* Build the neighborhoods for each local cell. 
		 * This has to happen after all the local and remote cells 
		 * are already created.*/
		for(int row = 0; row < parameters.nRows; row++) for(int col = 0; col < parameters.nCols; col++){
			cells[row][col].buildNeighborhood(row, col, neighborhoodTemplate, this);
		}
	}

	/** Initialize the dispersal season by staging overwintering beetles for emergence */
	public void initilizeDispersal(){
		Dispersal.initializeDispersal(this);
	}
	
	public void updateNeighborScores(){
		
		for(int row = 0; row < parameters.nRows; row++) for(int col = 0; col < parameters.nCols; col++){
			cells[row][col].neighborhood.updateNeighborScores(neighborhoodTemplate);
		}
	}
	
	/**  Loop through all the model's grid cells and disperse beetles to target cells. */
	public void disperseBeetles(){
		for(int row = 0; row < parameters.nRows; row++) for(int col = 0; col < parameters.nCols; col++){
			Dispersal.disperse(cells[row][col], parameters.nDispersalPackets, unif, neighborhoodTemplate);
		}
	}

	/** Receive an array of beetles being sent from a remote model.<br>
	 *  Allocate the incoming beetles to the appropriate local cells.
	 * 
	 * @param sectorCode the number of the incoming sector, in relation to the remote model.
	 * @param nBeetles array of incoming beetle counts
	 */
	public void receiveBeetlesFromRemoteSector(int sectorCode, int[][] nBeetles){
		int minRow = localSectorCellCoords.get(sectorCode)[0];
		int maxRow = localSectorCellCoords.get(sectorCode)[1];
		int minCol = localSectorCellCoords.get(sectorCode)[2];
		int maxCol = localSectorCellCoords.get(sectorCode)[3];
		
		for(int row = minRow; row <= maxRow; row++)
		for(int col = minCol; col <= maxCol; col ++){
			cells[row][col].receiveBeetles(nBeetles[row - minRow][col - minCol]);
		}
	}

	public double[][] sendAttractivenessScoresToRemote(int sectorCode){
		int minRow = localSectorCellCoords.get(sectorCode)[0];
		int maxRow = localSectorCellCoords.get(sectorCode)[1];
		int minCol = localSectorCellCoords.get(sectorCode)[2];
		int maxCol = localSectorCellCoords.get(sectorCode)[3];
		
		double[][] scores = new double[maxRow - minRow + 1][maxCol - minCol + 1];
		for(int row = minRow; row <= maxRow; row++)
		for(int col = minCol; col <= maxCol; col ++){
			scores[row - minRow][col - minCol] = cells[row][col].getAttractiveness();
		}
		return scores;
	}
	
	/** Get the attractiveness scores for the remote cells in a remote sector. */
	public void receiveAttractivenessScoresFromRemote(int sector, double[][] scores){
		int nRows = remoteSectors.get(sector).length;
		int nCols = remoteSectors.get(sector)[0].length;
		
		for(int row = 0; row < nRows; row++) for(int col = 0; col < nCols; col++){
			remoteSectors.get(sector)[row][col].attractiveness = scores[row][col];
		}
	}
	
	
	/** Return an array of beetle counts for a neighboring model to add to the
	 *  corresponding local cells in its grid.
	 * @param sector the number of the sector to which to send beetles, in reference to the local model.
	 * @param nBeetles
	 * @return	 */
	public int[][] stageBeetlesForRemoteSector(int sector){
		
		int nRows = remoteSectors.get(sector).length;
		int nCols = remoteSectors.get(sector)[0].length;
		int[][] toSend = new int[nRows][nCols];
		
		for(int row = 0; row < nRows; row++) for(int col = 0; col < nCols; col++){
			toSend[row][col] = remoteSectors.get(sector)[row][col].censusDispersingBeetles()[1];
		}
		
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
			/* Otherwise determine the sector. */
			int sector = Calculator.determineRemoteSector(row, column, parameters.nRows, parameters.nCols);
			int sectorRow = Calculator.determineSectorCoordinate(row, parameters.nRows, remoteSectorWidth);
			int sectorColumn = Calculator.determineSectorCoordinate(column, parameters.nCols, remoteSectorWidth);
			return remoteSectors.get(sector)[sectorRow][sectorColumn];
		}
	}


}
