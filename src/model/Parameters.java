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
	
	public Parameters(String parameterFilename){
		
		Map<String, String> pMap = CSVUtils.csvToHashMap(parameterFilename);
		
		randomSeed = Integer.parseInt(pMap.get("randomSeed"));
		
		nRows = Integer.parseInt(pMap.get("nRows"));
		nCols = Integer.parseInt(pMap.get("nCols"));
		nYears = Integer.parseInt(pMap.get("nYears"));
		
		saveFileName = pMap.get("saveFileName");
		
		neighborhoodRadius = Double.parseDouble(pMap.get("neighborhoodRadius"));
		distanceSelf = Double.parseDouble(pMap.get("distanceSelf"));
		cellWidth = Double.parseDouble(pMap.get("cellWidth"));
		
		distanceParamA = Double.parseDouble(pMap.get("distanceParamA"));
		distanceParamB = Double.parseDouble(pMap.get("distanceParamB"));
		distanceScoreWeight = Double.parseDouble(pMap.get("distanceScoreWeight"));
		
		beetleReproductionRate = Double.parseDouble(pMap.get("beetleReproductionRate"));
		initialBeetlesPerCell = Integer.parseInt(pMap.get("initialBeetlesPerCell"));
	}
}
