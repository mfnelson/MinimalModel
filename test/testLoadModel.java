import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

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
	public void test() throws IOException, InvalidRangeException {

//		ConsoleReporters.censusLocalCells(model);
//		ConsoleReporters.censusLocalDispersingBeetles(model);
//		ConsoleReporters.censusRemoteDispersingBeetles(model);
		model.initializeRandomEngine(290629);
		
		
//		for(int year = 0; year < model.parameters.nYears; year++){
//			model.disperseBeetles();
////			model.dispersal.finalizeDispersal(model); 
//			System.out.println("\nAfter dispersal from main grid year:" + year);
//			ConsoleReporters.censusLocalCells(model);
//			ConsoleReporters.censusLocalDispersingBeetles(model);
//			ConsoleReporters.censusRemoteDispersingBeetles(model);
//			
//			/* Get the beetles dispersing in from surrounding remote cells. */
//			for(int remoteQuadrant = 0; remoteQuadrant < 8; remoteQuadrant++){
//				model.receiveBeetlesFromRemote(remoteQuadrant, model2.sendLocalBeetlesToRemote((remoteQuadrant + 0) % 8));
//			}
//			model.dispersal.finalizeDispersal(model);
//			model.ncdfReporter.step(model);
//			System.out.println("\nAfter dispersal from remote cells, year: " + year);
//			ConsoleReporters.censusLocalCells(model);
//			ConsoleReporters.censusLocalDispersingBeetles(model);
//			ConsoleReporters.censusRemoteDispersingBeetles(model);
//			
//		}
		
		
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
