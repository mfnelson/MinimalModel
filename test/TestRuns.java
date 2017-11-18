import java.io.IOException;

import cell.LocalCell;
import model.Calculator;
import model.Model;
import reporters.ConsoleReporters;
import ucar.ma2.InvalidRangeException;

public class TestRuns {


	Model model;
	Model model2;




	public static void main(String args[]) throws IOException, InvalidRangeException{
		
		twoByTwo("data/twoByTwo.csv", "output/twoByTwo/twoByTwo");

		//		simulate("data/oneByTwo.csv", "output/oneByTwo/oneByTwo");
//		simulate("data/fourByFour.csv", "output/fourByFour/fourByFour");
////		simulate("data/fourByFive.csv", "output/fourByFive/fourByFive");
	}

	public static void twoByTwo(String paramFilename, String outputFilePrefix){
		Model[][] modelGrid = buildModelGrid(2, 2, paramFilename, outputFilePrefix);
		seedCells(modelGrid, 0, 0, 6, 0, 200);
//		disperseBeetles(modelGrid);
		receiveRemoteBeetles(modelGrid);
		finalizeDispersal(modelGrid);
		saveSimulation(modelGrid);
		
		
	}
	
	
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
	
	private static void seedCells(Model[][] modelGrid, int gridRow, int gridCol, int cellRow, int cellCol, int nBeetles){
		Model model = modelGrid[gridRow][gridCol];
		LocalCell cell = model.cells[cellRow][cellCol];
		
		for(int i = 0; i < nBeetles; i ++){
			cell.addEmergingBeetles(1);
			model.disperseBeetles();
		}
	}
	
	
	private static void simulate(Model[][] modelGrid, int nYears) throws IOException, InvalidRangeException{
	
		for(int year = 0; year < nYears; year++){
			/* All models disperse their beetles */
			disperseBeetles(modelGrid);
			receiveRemoteBeetles(modelGrid);
			/* All models finalize the year's dispersal and
			 * update the NetCDF data recorders. */
			finalizeDispersal(modelGrid);
		}

		censusModels(modelGrid, nYears);
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
				int[] targetCoords = Calculator.getRecipientModelOffsetCoords(sector, nGridRows, nGridCols);
				Model targetMod = modelGrid[row][col];
				Model sourceMod = modelGrid[targetCoords[0]][targetCoords[1]];
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
