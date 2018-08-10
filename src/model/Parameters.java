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

	public final int nDispersalPackets;
	
	public final double beetleReproductionRate;
	public final int initialBeetlesPerCell;
	
	public final int nTreesPerCell;
	
	public final double scoreWeightAttractiveness;
	public final double scoreWeightDistance;
	
	public final Map<String, String[]> parameterMap;
	
	
	
	public Parameters(String parameterFilename){
		
		parameterMap = CSVUtils.csvToHashMap(parameterFilename);
		
		randomSeed = Integer.parseInt(parameterMap.get("randomSeed")[0]);
		
		nRows = Integer.parseInt(parameterMap.get("nRows")[0]);
		nCols = Integer.parseInt(parameterMap.get("nCols")[0]);
		nYears = Integer.parseInt(parameterMap.get("nYears")[0]);
		
		saveFileName = parameterMap.get("saveFileName")[0];
		
		neighborhoodRadius = Double.parseDouble(parameterMap.get("neighborhoodRadius")[0]);
		distanceSelf = Double.parseDouble(parameterMap.get("distanceSelf")[0]);
		cellWidth = Double.parseDouble(parameterMap.get("cellWidth")[0]);
		
		distanceParamA = Double.parseDouble(parameterMap.get("distanceParamA")[0]);
		distanceParamB = Double.parseDouble(parameterMap.get("distanceParamB")[0]);
		distanceScoreWeight = Double.parseDouble(parameterMap.get("distanceScoreWeight")[0]);
		
		nDispersalPackets = Integer.parseInt(parameterMap.get("nDispersalPackets")[0]);
		
		beetleReproductionRate = Double.parseDouble(parameterMap.get("beetleReproductionRate")[0]);
		initialBeetlesPerCell = Integer.parseInt(parameterMap.get("initialBeetlesPerCell")[0]);
		
		nTreesPerCell = Integer.parseInt(parameterMap.get("nTreesPerCell")[0]);
 
		scoreWeightAttractiveness = Double.parseDouble(parameterMap.get("scoreWeightAttractiveness")[0]);
		scoreWeightDistance = Double.parseDouble(parameterMap.get("scoreWeightDistance")[0]);
	}
}
