package reporters;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import model.Calculator;
import model.Model;
import ucar.ma2.Array;
import ucar.ma2.DataType;
import ucar.ma2.Index1D;
import ucar.ma2.Index2D;
import ucar.ma2.Index3D;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Dimension;
import ucar.nc2.NetcdfFileWriter;
import ucar.nc2.Variable;
import ucar.nc2.write.Nc4Chunking;
import ucar.nc2.write.Nc4Chunking.Strategy;
import ucar.nc2.write.Nc4ChunkingStrategy;
import utils.UtilCalculations;




public class NetCDFReporters {

	NetcdfFileWriter dataFile;
	Dimension rowCoordinateDimension;
	Dimension columnCoordinateDimension;
	Dimension timeStepCoordinateDimension;
	
	Variable columnCoordinateVariable;
	Variable rowCoordinateVariable;
	Variable timeStepCoordinateVariable;
	
	Dimension remoteWidthDimension;
	Variable remoteWidthVariable;

	Array localCellsArray;
	Array[] remoteCellsArrays;
	
	Variable localBeetleVariable;
	Variable[] remoteVeetlesVariables;
	
	private int steps = 0;
	
	public NetCDFReporters(Model model){
		createArrays(model);
	}
	
	public void step(Model model){
		Array layer = Array.factory(getLocalBeetleSlice(model));
		addSliceTo3DArray(layer, localCellsArray, steps);
		steps++;
	}
	
	public void saveFile(Model model) throws IOException, InvalidRangeException{
		Array rows = Array.factory(UtilCalculations.seq(1,  model.parameters.nRows));
		Array columns = Array.factory(UtilCalculations.seq(1, model.parameters.nCols)); 
		Array timeSteps = Array.factory(UtilCalculations.seq(0, model.parameters.nYears));
		
		dataFile.write(rowCoordinateVariable, rows);
		dataFile.write(columnCoordinateVariable, columns);
		dataFile.write(timeStepCoordinateVariable, timeSteps);
		
		dataFile.write(localBeetleVariable, localCellsArray);
		dataFile.close();
	}
	
	public void createFile(Model model){
		try{
			Strategy strat = Strategy.standard;
			Nc4Chunking chunker = Nc4ChunkingStrategy.factory(strat, 9, true);
			NetcdfFileWriter.Version version = NetcdfFileWriter.Version.netcdf3;
			dataFile = NetcdfFileWriter.createNew(version, model.parameters.saveFileName, chunker);
			createVariables(model);
			dataFile.create();
		} catch(IOException e){e.printStackTrace();}
	}
	
	public void createVariables(Model model){
		rowCoordinateDimension = dataFile.addDimension(null, "row", model.parameters.nRows);
		columnCoordinateDimension = dataFile.addDimension(null, "column", model.parameters.nCols);
		timeStepCoordinateDimension = dataFile.addDimension(null, "year", model.parameters.nYears + 1);
		
		rowCoordinateVariable = dataFile.addVariable(null, "row", DataType.INT, "row");
		columnCoordinateVariable = dataFile.addVariable(null, "column", DataType.INT, "column");
		timeStepCoordinateVariable = dataFile.addVariable(null, "year", DataType.INT, "year");
		
//		remoteWidthDimension = dataFile.addDimension(null, "remoteWidth", (int)(Math.floor(model.parameters.neighborhoodRadius / model.parameters.cellWidth)));
//		remoteWidthVariable = dataFile.addVariable(null, "remoteWidth", DataType.INT, "remoteWidth");
		
		List<Dimension> dimsCells = new ArrayList<Dimension>();
		dimsCells.add(rowCoordinateDimension);
		dimsCells.add(columnCoordinateDimension);
		dimsCells.add(timeStepCoordinateDimension);
		
		List<Dimension> dimsDiagRemote =  new ArrayList<Dimension>();
		dimsDiagRemote.add(remoteWidthDimension);
		dimsDiagRemote.add(remoteWidthDimension);
		
		List<Dimension> dimsNorthSouthRemote = new ArrayList<Dimension>();
		dimsNorthSouthRemote.add(remoteWidthDimension);
		dimsNorthSouthRemote.add(columnCoordinateDimension);
		dimsNorthSouthRemote.add(timeStepCoordinateDimension);
		
		List<Dimension> dimsEastWestRemote = new ArrayList<Dimension>();
		dimsEastWestRemote.add(rowCoordinateDimension);
		dimsEastWestRemote.add(remoteWidthDimension);
		dimsEastWestRemote.add(timeStepCoordinateDimension);
		
		localBeetleVariable = dataFile.addVariable(null, "beetles", DataType.INT, dimsCells);
		
		// TODO add remote beetle variables
		
		
		
	}

	public void createArrays(Model model){
		
		int remoteWidth = (int)(Math.ceil(model.parameters.neighborhoodRadius / model.parameters.cellWidth));
		
		localCellsArray = Array.factory(DataType.INT, new int[] {model.parameters.nRows, model.parameters.nCols, model.parameters.nYears + 1});
		remoteCellsArrays = new Array[8];

		for(int quad = 0; quad < 8; quad++){
			/* diagonal remote cell neighborhoods. */
			if(quad % 2 == 0){
				remoteCellsArrays[quad] = Array.factory(DataType.INT, new int[]{remoteWidth, remoteWidth, model.parameters.nYears + 1});
			} else if (quad == 1 & quad == 3){
				/* north/south neighbors. */
				remoteCellsArrays[quad] = Array.factory(DataType.INT, new int[]{remoteWidth, model.parameters.nCols, model.parameters.nYears + 1});
			} else {
				/* east/west neighbors */
				remoteCellsArrays[quad] = Array.factory(DataType.INT, new int[]{model.parameters.nRows, remoteWidth, model.parameters.nYears + 1});
			}
		}
	}
	
	public void addSliceTo3DArray(Array sliceToAddArray, Array masterArray, int layerIndex){
		int[] shapeToAdd = sliceToAddArray.getShape();
		int[] shapeMaster = masterArray.getShape();
		if(shapeToAdd.length == 1){
			Index1D indexSlice = new Index1D(new int[] {shapeToAdd[0]});
			Index1D indexMaster = new Index1D(new int[] {masterArray.getShape()[0]});
			indexMaster.set(layerIndex);
			indexSlice.set(0);
			long val = sliceToAddArray.getLong(indexSlice);
			masterArray.setLong(
				indexMaster.set(layerIndex), 
				val);
		} else
		if(shapeToAdd.length == 2){
			Index2D indexSliceToAdd = new Index2D(shapeToAdd);
			Index3D indexMaster = new Index3D(shapeMaster);
			
			if(masterArray.getElementType().equals(long.class)){
				for(int column = 0; column < shapeToAdd[0]; column++)
				for(int row = 0; row < shapeToAdd[1]; row++){
					masterArray.setLong(indexMaster.set(column, row, layerIndex), sliceToAddArray.getLong(indexSliceToAdd.set(column, row)));
				}
			} else 
			if(masterArray.getElementType().equals(int.class)){
				for(int column = 0; column < shapeToAdd[0]; column++)
				for(int row = 0; row < shapeToAdd[1]; row++){
					masterArray.setInt(
						indexMaster.set(
							column, 
							row, 
							layerIndex), 
						sliceToAddArray.getInt(indexSliceToAdd.set(column, row)));
				}
			} else
			if(masterArray.getElementType().equals(double.class)){
				for(int column = 0; column < sliceToAddArray.getShape()[0]; column++)
				for(int row = 0; row < sliceToAddArray.getShape()[1]; row++){
					masterArray.setDouble(indexMaster.set(column, row, layerIndex), sliceToAddArray.getDouble(indexSliceToAdd.set(column, row)));
				}
			}else
			if(masterArray.getElementType().equals(boolean.class)){
				for(int column = 0; column < sliceToAddArray.getShape()[0]; column++)
				for(int row = 0; row < sliceToAddArray.getShape()[1]; row++){
					masterArray.setBoolean(indexMaster.set(column, row, layerIndex), sliceToAddArray.getBoolean(indexSliceToAdd.set(column, row)));
				}
			}
		}
	}
	
	public int[][] getLocalBeetleSlice(Model model){
		int[][] out = new int[model.parameters.nRows][model.parameters.nCols];
		
		for(int row = 0; row < model.parameters.nRows; row++)
			for(int col = 0; col < model.parameters.nCols; col++){
				out[row][col] = model.cells[row][col].census();
			}
		
		return out;
	}
	
}