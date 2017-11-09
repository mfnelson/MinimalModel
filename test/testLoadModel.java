import static org.junit.Assert.fail;

import org.junit.Before;
import org.junit.Test;

import model.Model;
import reporters.ConsoleReporters;

public class testLoadModel {


	Model model;

	@Before
	public void setup(){
		model = new Model("data/params.csv");

	}

	@Test
	public void test() {

		ConsoleReporters.censusLocalCells(model);
		ConsoleReporters.censusLocalDispersingBeetles(model);
		ConsoleReporters.censusRemoteDispersingBeetles(model);

		for(int i = 0; i < 1000; i++){

			model.disperseBeetles();
			ConsoleReporters.censusLocalCells(model);
			ConsoleReporters.censusLocalDispersingBeetles(model);
			ConsoleReporters.censusRemoteDispersingBeetles(model);
		}
		fail("Not yet implemented");
	}

}
