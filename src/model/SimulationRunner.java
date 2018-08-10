package model;

import java.io.IOException;

import reporters.ConsoleReporters;
import submodels.Dispersal;
import ucar.ma2.InvalidRangeException;

public class SimulationRunner {

	
	public static void run(String paramFileName, String outputFilePrefix, boolean consoleReport){
		Parameters params = new Parameters(paramFileName);
		Model[][] modelGrid = buildModelGrid(paramFileName, outputFilePrefix);
		try {simulate(modelGrid, params.nYears, consoleReport);
		} catch (IOException | InvalidRangeException e) {e.printStackTrace();}
		saveSimulation(modelGrid);
	}

	private static void simulate(Model[][] modelGrid, int nYears, boolean consoleReport) throws IOException, InvalidRangeException{
		for(int year = 0; year < nYears; year++){
			initializeDispersal(modelGrid);
			/* All models disperse their beetles */
			disperseBeetles(modelGrid);
			modelCommunication(modelGrid);
			/* All models finalize the year's dispersal and
			 * update the NetCDF data recorders. */
			finalizeDispersal(modelGrid);
		}
		if(consoleReport) censusModels(modelGrid, nYears);
	}
	
	/** Create the grid of model processes. */
	private static Model[][] buildModelGrid(String paramFileName, String outputFilePrefix){
		Parameters params = new Parameters(paramFileName);
		int nGridRows = Integer.parseInt(params.parameterMap.get("nGridRows")[0]);
		int nGridCols = Integer.parseInt(params.parameterMap.get("nGridCols")[0]);
		
		Model[][] modelGrid = new Model[nGridRows][nGridCols];
		for(int row = 0; row < nGridRows; row++)
			for(int col = 0; col < nGridCols; col++){
				modelGrid[row][col] = new Model(paramFileName);
				modelGrid[row][col].parameters.saveFileName = outputFilePrefix + "_row_" + row + "_col_" + col + ".nc";
				modelGrid[row][col].ncdfReporter.createFile(modelGrid[row][col]);
			}
		return modelGrid;
	}
	
	/** Stage each model in the model grid for the year's dispersal. */
	private static void initializeDispersal(Model[][] modelGrid){
		int nGridRows = modelGrid.length;
		int nGridCols = modelGrid[0].length;
		for(int row = 0; row < nGridRows; row++)for(int col = 0; col < nGridCols; col++){
			modelGrid[row][col].initilizeDispersal();
		}
	}

	/** Beetles disperse within each model process. */
	private static void disperseBeetles(Model[][] modelGrid){
		int nGridRows = modelGrid.length;
		int nGridCols = modelGrid[0].length;
		for(int row = 0; row < nGridRows; row++)for(int col = 0; col < nGridCols; col++){
			modelGrid[row][col].disperseBeetles();
		}
	}

	/** Each model process finishes its dispersal methods for the year. */
	private static void finalizeDispersal(Model[][] modelGrid){
		int nGridRows = modelGrid.length;
		int nGridCols = modelGrid[0].length;
		for(int row = 0; row < nGridRows; row++){for(int col = 0; col < nGridCols; col++){
			Dispersal.finalizeDispersal(modelGrid[row][col]);
			modelGrid[row][col].ncdfReporter.step(modelGrid[row][col]);
		}}
	}
	
	/** Model processes communicate with each other about the beetles
	 *  that migrated across processes.
	 * @param modelGrid */
	private static void modelCommunication(Model[][] modelGrid){

		int nGridRows = modelGrid.length;
		int nGridCols = modelGrid[0].length;

		for(int row = 0; row < nGridRows; row++){for(int col = 0; col < nGridCols; col++){

			/* Cycle through the quadrants: */
			for(int sector = 0; sector < 8; sector++){
				int[] targetCoords = Calculator.getRecipientModelOffsetCoords(sector, row, col, nGridRows, nGridCols);
				Model sourceMod = modelGrid[row][col];
				Model targetMod = modelGrid[targetCoords[0]][targetCoords[1]];
				int[][] beetles = sourceMod.stageBeetlesForRemoteSector(sector);
				targetMod.receiveBeetlesFromRemoteSector(sector, beetles);				double[][] scores = targetMod.sendAttractivenessScoresToRemote(sector);
				sourceMod.receiveAttractivenessScoresFromRemote(sector, scores);
			}}}
	}

	/** Each model process saves its output. */
	private static void saveSimulation(Model[][] modelGrid){
		int nGridRows = modelGrid.length;
		int nGridCols = modelGrid[0].length;
		for(int row = 0; row < nGridRows; row++){
			for(int col = 0; col < nGridCols; col++){
				Model mod = modelGrid[row][col];
				try {
					mod.ncdfReporter.saveFile(mod);
				} catch (IOException | InvalidRangeException e) {
					e.printStackTrace();
				}
			}
		}
	}

	/** Print a console report for each model process. */
	private static void censusModels(Model[][] modelGrid, int year){
		int nRows = modelGrid.length; int nCols = modelGrid[0].length;
		for(int row = 0; row < nRows; row++){for(int col = 0; col < nCols; col++){
			Model mod = modelGrid[row][col];
			System.out.println("year: " + year + " model at row " + row + " col " + col);
			ConsoleReporters.censusLocalCells(mod);
		}}
	}
	
}
