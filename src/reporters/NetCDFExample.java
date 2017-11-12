package reporters;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import ucar.ma2.Array;
import ucar.ma2.DataType;
import ucar.nc2.Attribute;
import ucar.nc2.Dimension;
import ucar.nc2.NetcdfFileWriter;
import ucar.nc2.Variable;

public class NetCDFExample {
	 public static void main(String[] args) throws IOException {
		    String location = "output/testWrite.nc";
		  NetcdfFileWriter writer = NetcdfFileWriter.createNew(NetcdfFileWriter.Version.netcdf3, location, null);

		    // add dimensions
		  Dimension latDim = writer.addDimension(null, "lat", 64);
		    Dimension lonDim = writer.addDimension(null, "lon", 128);

		    // add Variable double temperature(lat,lon)
		    List<Dimension> dims = new ArrayList<Dimension>();
		    dims.add(latDim);
		    dims.add(lonDim);
		  Variable t = writer.addVariable(null, "temperature", DataType.DOUBLE, dims);
		  t.addAttribute(new Attribute("units", "K"));   // add a 1D attribute of length 3
		  Array data = Array.factory(int.class, new int[]{3}, new int[]{1, 2, 3});
		  t.addAttribute(new Attribute("scale", data));

		    // add a string-valued variable: char svar(80)
		    Dimension svar_len = writer.addDimension(null, "svar_len", 80);
		  writer.addVariable(null, "svar", DataType.CHAR, "svar_len");

		    // add a 2D string-valued variable: char names(names, 80)
		    Dimension names = writer.addDimension(null, "names", 3);
		  writer.addVariable(null, "names", DataType.CHAR, "names svar_len");

		    // add a scalar variable
		  writer.addVariable(null, "scalar", DataType.DOUBLE, new ArrayList<Dimension>());

		    // add global attributes
		 writer.addGroupAttribute(null, new Attribute("yo", "face"));
		    writer.addGroupAttribute(null, new Attribute("versionD", 1.2));
		    writer.addGroupAttribute(null, new Attribute("versionF", (float) 1.2));
		    writer.addGroupAttribute(null, new Attribute("versionI", 1));
		    writer.addGroupAttribute(null, new Attribute("versionS", (short) 2));
		    writer.addGroupAttribute(null, new Attribute("versionB", (byte) 3));

		    // create the file
		    try {
		    writer.create();
		    } catch (IOException e) {
		      System.err.printf("ERROR creating file %s%n%s", location, e.getMessage());
		    }
		  writer.close();
		  }
}
