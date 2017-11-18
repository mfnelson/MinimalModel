import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

import cell.LocalCell;
import model.Calculator;
import model.Model;
import reporters.ConsoleReporters;
import ucar.ma2.InvalidRangeException;

public class testLoadModel {


	Model model;
	Model model2;

	@Before
	public void setup(){
		model = new Model("data/params.csv");
		model2 = model;
	}

	private Model[][] buildGrid(int nGridRows, int nGridCols, String paramFileName, String outputFilePrefix){
		Model[][] modelGrid = new Model[nGridRows][nGridCols];
		for(int row = 0; row < nGridRows; row++)
		for(int col = 0; col < nGridCols; col++){
			modelGrid[row][col] = new Model("paramFileName");
			modelGrid[row][col].parameters.saveFileName = outputFilePrefix + "_row_" + row + "_col_" + col + ".nc";
			modelGrid[row][col].ncdfReporter.createFile(modelGrid[row][col]);
		}
		return modelGrid;
	}
	
	
	
	
	private void censusModels(Model[][] modelGrid, int year){
		int nRows = modelGrid.length; int nCols = modelGrid[0].length;
		for(int row = 0; row < nRows; row++){for(int col = 0; col < nCols; col++){
				Model mod = modelGrid[row][col];
				System.out.println("year: " + year + " model at row " + row + " col " + col);
				ConsoleReporters.censusLocalCells(mod);
		}}
	}
	
	
//	@Test
	public void twoByOne() throws IOException, InvalidRangeException {
		
		Model modelLeft = new Model("data/params.csv");
		Model modelRight = new Model("data/params.csv");
		
		modelLeft.parameters.saveFileName = "output/twoModelLeft.nc";
		modelRight.parameters.saveFileName = "output/twoModelRight.nc";
		
		modelLeft.ncdfReporter.createFile(modelLeft);
		modelRight.ncdfReporter.createFile(modelRight);
		
		/* left model quadrant 0 = right model SE
		 * left model quadrant 2 = right model SW
		 * left model quadrant 4 = right model NW
		 * left model quadrant 6 = right model NE

		 * right model quadrant 0 = left model SE
		 * right model quadrant 2 = left model SW
		 * right model quadrant 4 = left model NW
		 * right model quadrant 6 = left model NE
		 * 
		 * left model quadrant 3 = right model W
		 * left model quadrant 7 = right model E
		 * 
		 * right model quadrant 3 = left model W
		 * right model quadrant 7 = left model E
		 * 
		 * model quadrant 1 = own S
		 * model quadrant 5 = own N
		 */
		
		
		
		for(int i = 0; i < 500; i++){
			modelLeft.cells[0][0].getRandomWeightedRecipient(modelLeft.unif, modelLeft.neighborhoodTemplate).receiveBeetles(10);
		}
		
		
		
		for(int remoteQuadrant = 0; remoteQuadrant < 8; remoteQuadrant++){
			if(remoteQuadrant != 1 & remoteQuadrant != 5){
				modelLeft.receiveBeetlesFromRemoteSector(remoteQuadrant, modelRight.stageBeetlesForRemoteSector(remoteQuadrant));
				modelRight.receiveBeetlesFromRemoteSector(remoteQuadrant, modelLeft.stageBeetlesForRemoteSector(remoteQuadrant));
			} else{
				modelLeft.receiveBeetlesFromRemoteSector(remoteQuadrant, modelLeft.stageBeetlesForRemoteSector(remoteQuadrant));
				modelRight.receiveBeetlesFromRemoteSector(remoteQuadrant, modelRight.stageBeetlesForRemoteSector(remoteQuadrant));
			}
		}
		
		modelLeft.dispersal.finalizeDispersal(modelLeft);
		modelLeft.ncdfReporter.step(modelLeft);
		System.out.println("Left Model");
		ConsoleReporters.censusLocalCells(modelLeft);
		modelLeft.ncdfReporter.saveFile(modelLeft);
		
		modelRight.dispersal.finalizeDispersal(modelRight);
		modelRight.ncdfReporter.step(modelRight);
		System.out.println("Right Model");
		ConsoleReporters.censusLocalCells(modelRight);
		modelRight.ncdfReporter.saveFile(modelRight);
		
	}
	
//	@Test
	public void test() throws IOException, InvalidRangeException {

		model.initializeRandomEngine(290629);
		
		
		for(int i = 0; i < 500; i++){
			model.cells[0][0].getRandomWeightedRecipient(model.unif, model.neighborhoodTemplate).receiveBeetles(10);
		}
		for(int remoteQuadrant = 0; remoteQuadrant < 8; remoteQuadrant++){
			model.receiveBeetlesFromRemoteSector(remoteQuadrant, model2.stageBeetlesForRemoteSector((remoteQuadrant + 0) % 8));
		}
		model.dispersal.finalizeDispersal(model);
		model.ncdfReporter.step(model);
		ConsoleReporters.censusLocalCells(model);
		model.ncdfReporter.saveFile(model);
		
		
//		model.simulate();
		
	}

}
