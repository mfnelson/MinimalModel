package testRuns;

import java.io.IOException;

import model.SimulationRunner;
import ucar.ma2.InvalidRangeException;

public class TestRuns {

	public static void main(String args[]) throws IOException, InvalidRangeException{

		//		simulate("data/oneByTwo.csv", "output/oneByTwo/oneByTwo");
		//		simulate("data/fourByFour.csv", "output/fourByFour/fourByFour");
		//		simulate("data/fourByFive.csv", "output/fourByFive/fourByFive");

		SimulationRunner.run("data/fourByFive.csv", "output/fourByFive/fourByFive", true );
	}

}
