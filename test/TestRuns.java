import java.io.IOException;


import cell.LocalCell;
import cern.jet.random.Uniform;
import cern.jet.random.engine.MersenneTwister;
import cern.jet.random.engine.RandomEngine;
import model.Calculator;
import model.Model;
import model.Parameters;
import reporters.ConsoleReporters;
import ucar.ma2.InvalidRangeException;

public class TestRuns {


	Model model;
	Model model2;




	public static void main(String args[]) throws IOException, InvalidRangeException{
		
//		fourByFour("data/fourByFour.csv", "output/fourByFour/fourByFour", 5000);
//		twoByTwo("data/twoByTwo.csv", "output/twoByTwo/twoByTwo", 10);
//		simulate("data/oneByTwo.csv", "output/oneByTwo/oneByTwo");
//		simulate("data/fourByFour.csv", "output/fourByFour/fourByFour");
		simulate("data/fourByFive.csv", "output/fourByFive/fourByFive");
	}

	public static void fourByFour(String paramFilename, String outputFilePrefix, int nSeeds){
		
		Model[][] modelGrid = buildModelGrid(4, 4, paramFilename, outputFilePrefix);
		seedCells(modelGrid, nSeeds, 1);
		
		try {simulate(modelGrid, modelGrid[0][0].parameters.nYears);
		} catch (IOException | InvalidRangeException e) {e.printStackTrace();}
		saveSimulation(modelGrid);
		
	}
	
	public static void fourByFive(String paramFilename, String outputFilePrefix){
		
	}
	
	/** Run a simulation with a 2 by 2 grid of models. */
	public static void twoByTwo(String paramFilename, String outputFilePrefix, int nSeeds){
		Model[][] modelGrid = buildModelGrid(2, 2, paramFilename, outputFilePrefix);
		
		seedCells(modelGrid, nSeeds, 1);
		
		try {simulate(modelGrid, modelGrid[0][0].parameters.nYears);
		} catch (IOException | InvalidRangeException e) {e.printStackTrace();}
		saveSimulation(modelGrid);
	}
	
	/** Create the grid of model objects. */	
	private static Model[][] buildModelGrid(int nGridRows, int nGridCols, String paramFileName, String outputFilePrefix){
		Model[][] modelGrid = new Model[nGridRows][nGridCols];
		for(int row = 0; row < nGridRows; row++)
			for(int col = 0; col < nGridCols; col++){
				modelGrid[row][col] = new Model(paramFileName);
				modelGrid[row][col].parameters.saveFileName = outputFilePrefix + "_row_" + row + "_col_" + col + ".nc";
				modelGrid[row][col].ncdfReporter.createFile(modelGrid[row][col]);
			}
		return modelGrid;
	}
	
	/** Create the grid of model objects. */
	public static Model[][] buildModelGrid( String paramFileName, String outputFilePrefix){
		Parameters params = new Parameters(paramFileName);
		int nGridRows = Integer.parseInt(params.parameterMap.get("nGridRows"));
		int nGridCols = Integer.parseInt(params.parameterMap.get("nGridCols"));
		return buildModelGrid(nGridRows, nGridCols, paramFileName, outputFilePrefix);
	}
	
	/** Place some beetles in a cell and disperse them. */
	private static void seedCells(Model[][] modelGrid, int gridRow, int gridCol, int cellRow, int cellCol, int nBeetles){
		Model model = modelGrid[gridRow][gridCol];
		LocalCell cell = model.cells[cellRow][cellCol];
		
		for(int i = 0; i < nBeetles; i ++){
			cell.addEmergingBeetles(1);
			model.disperseBeetles();
		}
		receiveRemoteBeetles(modelGrid);
		finalizeDispersal(modelGrid);
	}
	
	/** Place beetles in a randomly selected cell from a randomly selected model from the grid. */
	private static void seedCells(Model[][] modelGrid, int nTrials, int nBeetlesPerTrial){
		RandomEngine re;
		re = new MersenneTwister((int)System.currentTimeMillis());
		Uniform unif = new Uniform(re);
		int nRows = modelGrid[0][0].parameters.nRows - 1;
		int nCols = modelGrid[0][0].parameters.nCols - 1;
		int nGridRows = modelGrid.length - 1;
		int nGridCols = modelGrid[0].length - 1;
		for(int i = 0; i < nTrials; i++){
			Model model = modelGrid[unif.nextIntFromTo(0, nGridRows)][unif.nextIntFromTo(0, nGridCols)];
			LocalCell cell = model.cells[unif.nextIntFromTo(0, nRows)][unif.nextIntFromTo(0, nCols)];
			cell.addBeetles(nBeetlesPerTrial);
		}
	}
	
	private static void simulate(String paramFileName, String outputFilePrefix){
		Parameters params = new Parameters(paramFileName);
		Model[][] modelGrid = buildModelGrid(paramFileName, outputFilePrefix);
		try {
			simulate(modelGrid, params.nYears);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InvalidRangeException e) {
			e.printStackTrace();
		}
		saveSimulation(modelGrid);
		
	}
	
	private static void simulate(Model[][] modelGrid, int nYears) throws IOException, InvalidRangeException{
	
		for(int year = 0; year < nYears; year++){
			initializeDispersal(modelGrid);
			/* All models disperse their beetles */
			disperseBeetles(modelGrid);
			receiveRemoteBeetles(modelGrid);
			/* All models finalize the year's dispersal and
			 * update the NetCDF data recorders. */
			finalizeDispersal(modelGrid);
		}

		censusModels(modelGrid, nYears);

	}

	private static void initializeDispersal(Model[][] modelGrid){
		int nGridRows = modelGrid.length;
		int nGridCols = modelGrid[0].length;
		for(int row = 0; row < nGridRows; row++)for(int col = 0; col < nGridCols; col++){
			modelGrid[row][col].initilizeDispersal();;
		}
	}
	
	private static void disperseBeetles(Model[][] modelGrid){
		int nGridRows = modelGrid.length;
		int nGridCols = modelGrid[0].length;
		for(int row = 0; row < nGridRows; row++)for(int col = 0; col < nGridCols; col++){
			modelGrid[row][col].disperseBeetles();
		}
	}
	
	private static void finalizeDispersal(Model[][] modelGrid){
		int nGridRows = modelGrid.length;
		int nGridCols = modelGrid[0].length;
		for(int row = 0; row < nGridRows; row++){for(int col = 0; col < nGridCols; col++){
			modelGrid[row][col].dispersal.finalizeDispersal(modelGrid[row][col]);
			modelGrid[row][col].ncdfReporter.step(modelGrid[row][col]);
		}}
	}
	
	private static void receiveRemoteBeetles(Model[][] modelGrid){
		
		int nGridRows = modelGrid.length;
		int nGridCols = modelGrid[0].length;
		
		for(int row = 0; row < nGridRows; row++){for(int col = 0; col < nGridCols; col++){

			/* Cycle through the quadrants: */
			for(int sector = 0; sector < 8; sector++){
				int[] targetCoords = Calculator.getRecipientModelOffsetCoords(sector, row, col, nGridRows, nGridCols);
				Model sourceMod = modelGrid[row][col];
				Model targetMod = modelGrid[targetCoords[0]][targetCoords[1]];
				int[][] beetles = sourceMod.stageBeetlesForRemoteSector(sector);
				targetMod.receiveBeetlesFromRemoteSector(sector, beetles);
			}}}
	}
	
	private static void saveSimulation(Model[][] modelGrid){
		int nGridRows = modelGrid.length;
		int nGridCols = modelGrid[0].length;
		for(int row = 0; row < nGridRows; row++){
			for(int col = 0; col < nGridCols; col++){
				Model mod = modelGrid[row][col];
				try {
					mod.ncdfReporter.saveFile(mod);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (InvalidRangeException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
	
	private static void censusModels(Model[][] modelGrid, int year){
		int nRows = modelGrid.length; int nCols = modelGrid[0].length;
		for(int row = 0; row < nRows; row++){for(int col = 0; col < nCols; col++){
			Model mod = modelGrid[row][col];
			System.out.println("year: " + year + " model at row " + row + " col " + col);
			ConsoleReporters.censusLocalCells(mod);
		}}
	}
}
