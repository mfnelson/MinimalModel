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

	
	@Test
	public void twoByTwo() throws IOException, InvalidRangeException {
		
		int nRows = 2;
		int nCols = 2;
		
		Model[][] modelGrid = new Model[nRows][nCols];
		
		for(int row = 0; row < nRows; row++)
		for(int col = 0; col < nCols; col++){
			modelGrid[row][col] = new Model("data/twoByTwo.csv");
			modelGrid[row][col].parameters.saveFileName = "output/twoByTwo_row_" + row + "_col_" + col + ".nc";
			modelGrid[row][col].ncdfReporter.createFile(modelGrid[row][col]);
		}
		
		/* Place 500 beetles in the corner of the SW model of the grid */
//		for(int i = 0; i < 500; i++){
//			Model mod = modelGrid[0][0]; LocalCell cell = mod.cells[mod.parameters.nRows - 1][mod.parameters.nCols - 1];
//			cell.addEmergingBeetles(10);
//			mod.dispersal.disperse(cell, mod, mod.neighborhoodTemplate);
//		}
//		censusModels(modelGrid, -3);
		
		/* Other models receive the beetles from the remote quadrants */
//		for(int row = 0; row < nRows; row++){for(int col = 0; col < nCols; col++){
//				/* Cycle through the quadrants: */
//				for(int quadrant = 0; quadrant < 8; quadrant++){
//					int[] offsets = Calculator.getQuadrantOffsetCoords(quadrant);
//					Model targetMod = modelGrid[row][col];
//					Model sourceMod = modelGrid[(row + nRows + offsets[0]) % nRows][(col + nCols + offsets[1]) % nCols];
//					targetMod.receiveBeetlesFromRemote(quadrant, sourceMod.sendLocalBeetlesToRemote(quadrant));
//		}}}
////		censusModels(modelGrid, -2);
//		
//		/* All models classify their arriving beetles as overwintering. */
//		for(int row = 0; row < nRows; row++){for(int col = 0; col < nCols; col++){
//				Model mod = modelGrid[row][col];
//				mod.dispersal.finalizeDispersal(mod);
//		}}
//		censusModels(modelGrid, -1);
		
		for(int year = 0; year < modelGrid[0][0].parameters.nYears; year++){

			/* All models disperse their beetles */
			for(int row = 0; row < nRows; row++){for(int col = 0; col < nCols; col++){
				modelGrid[row][col].disperseBeetles();;
			}}
			
			/* All models reconcile their incoming beetles sent from remote models. */
			for(int row = 0; row < nRows; row++){for(int col = 0; col < nCols; col++){
					
					/* Cycle through the quadrants: */
					for(int quadrant = 0; quadrant < 8; quadrant++){
						int[] offsets = Calculator.getQuadrantOffsetCoords(quadrant);
						Model targetMod = modelGrid[row][col];
						Model sourceMod = modelGrid
								[(row + nRows + offsets[0]) % nRows]
								[(col + nCols + offsets[1]) % nCols];
						targetMod.receiveBeetlesFromRemote(quadrant, sourceMod.sendLocalBeetlesToRemote(quadrant));
			}}}
			
			/* All models finalize the year's dispersal and
			 * update the NetCDF data recorders. */
			for(int row = 0; row < nRows; row++){for(int col = 0; col < nCols; col++){
				modelGrid[row][col].dispersal.finalizeDispersal(modelGrid[row][col]);
				modelGrid[row][col].ncdfReporter.step(modelGrid[row][col]);
			}}
//			censusModels(modelGrid, year);
		}
		
		for(int row = 0; row < nRows; row++){
			for(int col = 0; col < nCols; col++){
				Model mod = modelGrid[row][col];
				mod.ncdfReporter.saveFile(mod);
			}
		}
		
		censusModels(modelGrid, 1);
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
			modelLeft.cells[0][0].getRandomWeightedRecipient(modelLeft, modelLeft.neighborhoodTemplate).receiveBeetles(10);
		}
		
		
		
		for(int remoteQuadrant = 0; remoteQuadrant < 8; remoteQuadrant++){
			if(remoteQuadrant != 1 & remoteQuadrant != 5){
				modelLeft.receiveBeetlesFromRemote(remoteQuadrant, modelRight.sendLocalBeetlesToRemote(remoteQuadrant));
				modelRight.receiveBeetlesFromRemote(remoteQuadrant, modelLeft.sendLocalBeetlesToRemote(remoteQuadrant));
			} else{
				modelLeft.receiveBeetlesFromRemote(remoteQuadrant, modelLeft.sendLocalBeetlesToRemote(remoteQuadrant));
				modelRight.receiveBeetlesFromRemote(remoteQuadrant, modelRight.sendLocalBeetlesToRemote(remoteQuadrant));
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
			model.cells[0][0].getRandomWeightedRecipient(model, model.neighborhoodTemplate).receiveBeetles(10);
		}
		for(int remoteQuadrant = 0; remoteQuadrant < 8; remoteQuadrant++){
			model.receiveBeetlesFromRemote(remoteQuadrant, model2.sendLocalBeetlesToRemote((remoteQuadrant + 0) % 8));
		}
		model.dispersal.finalizeDispersal(model);
		model.ncdfReporter.step(model);
		ConsoleReporters.censusLocalCells(model);
		model.ncdfReporter.saveFile(model);
		
		
//		model.simulate();
		
	}

}
