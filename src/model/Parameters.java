package model;

import java.util.Map;

import fileIO.CSVUtils;

public class Parameters {

	public final int randomSeed;
	
	public final int nRows;
	public final int nCols;
	public final int nYears;
	
	/* Keep this mutable to make saving results from replicate runs easier. */
	public String saveFileName;
	
	public final double neighborhoodRadius;
	public final double cellWidth;
	public final double distanceSelf;
	
	public final double distanceParamA;
	public final double distanceParamB;
	public final double distanceScoreWeight;

	public final double beetleReproductionRate;
	public final int initialBeetlesPerCell;
	
	public final Map<String, String> parameterMap;
	
	public Parameters(String parameterFilename){
		
		parameterMap = CSVUtils.csvToHashMap(parameterFilename);
		
		randomSeed = Integer.parseInt(parameterMap.get("randomSeed"));
		
		nRows = Integer.parseInt(parameterMap.get("nRows"));
		nCols = Integer.parseInt(parameterMap.get("nCols"));
		nYears = Integer.parseInt(parameterMap.get("nYears"));
		
		saveFileName = parameterMap.get("saveFileName");
		
		neighborhoodRadius = Double.parseDouble(parameterMap.get("neighborhoodRadius"));
		distanceSelf = Double.parseDouble(parameterMap.get("distanceSelf"));
		cellWidth = Double.parseDouble(parameterMap.get("cellWidth"));
		
		distanceParamA = Double.parseDouble(parameterMap.get("distanceParamA"));
		distanceParamB = Double.parseDouble(parameterMap.get("distanceParamB"));
		distanceScoreWeight = Double.parseDouble(parameterMap.get("distanceScoreWeight"));
		
		beetleReproductionRate = Double.parseDouble(parameterMap.get("beetleReproductionRate"));
		initialBeetlesPerCell = Integer.parseInt(parameterMap.get("initialBeetlesPerCell"));
	}
}
