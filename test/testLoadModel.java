import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

import model.Model;
import reporters.ConsoleReporters;
import ucar.ma2.InvalidRangeException;

public class testLoadModel {


	Model model;

	@Before
	public void setup(){
		model = new Model("data/params.csv");

	}

	@Test
	public void test() throws IOException, InvalidRangeException {

		ConsoleReporters.censusLocalCells(model);
		ConsoleReporters.censusLocalDispersingBeetles(model);
		ConsoleReporters.censusRemoteDispersingBeetles(model);
		model.initializeRandomEngine(222);
		model.simulate();
		
	}

}
