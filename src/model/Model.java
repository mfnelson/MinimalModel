package model;

import java.io.IOException;
import java.util.LinkedList;
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
	
	public Parameters parameters;
	public NetCDFReporters ncdfReporter;
	
	NeighborhoodTemplate neighborhoodTemplate;
	
	private RandomEngine re;
	public final Uniform unif;
	
	public Dispersal dispersal;
	
	public Model(String parameterFilename){
		parameters = new Parameters(parameterFilename);
		ncdfReporter = new NetCDFReporters(this);
		ncdfReporter.createFile(this);
		
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
		remoteCells = new LinkedList<RemoteCell[][]>();
		buildCells();
		
		dispersal = new Dispersal();
	}
	
	public void initializeRandomEngine(int rand){
		re = new MersenneTwister(rand);
	}
	
	
	public void simulate() throws IOException, InvalidRangeException{
		for(int year = 0; year < parameters.nYears; year++){
			disperseBeetles();
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
		int maxWidth = Calculator.maxLocalNeighborhoodWidth(parameters.neighborhoodRadius, parameters.cellWidth);
		for(int quadrant = 0; quadrant < 8; quadrant ++){
			
			int nRow = 0;
			int nCol = 0;
			/* If the quadrant is diagonal, it is a square based on the 
			 * neighborhood radius */
			if(quadrant % 2 == 0){
				nRow = nCol = maxWidth;
			} else if(quadrant == 1 | quadrant == 5){
				/* North and south neighborhoods. */
				nRow = maxWidth;
				nCol = parameters.nCols;
			} else {
				/* East and west neighborhoods. */
				nRow = parameters.nRows;
				nCol = maxWidth;
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
	
	public void disperseBeetles(){
		dispersal.initializeDispersal(this);
		for(int row = 0; row < parameters.nRows; row++) for(int col = 0; col < parameters.nCols; col++){
			dispersal.disperse(cells[row][col], this, neighborhoodTemplate);
		}
		dispersal.finalizeDispersal(this);
	}

	public void receiveRemoteBeetles(int quadrant, int[][] coords, int[] nBeetles){}
	
	public void receiveRemoteOccupancyScores(int quadrant, int[][] coords, double[] scores){}
	
	public Cell getCell(int[] coords){return getCell(coords[0], coords[1]);}
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
