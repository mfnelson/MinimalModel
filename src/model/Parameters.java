package model;

import java.util.Map;

import fileIO.CSVUtils;

public class Parameters {

	public final int randomSeed;
	
	public final int nRows;
	public final int nCols;
	
	public final int nTreesPerCell;
	public final int nInitialBeetlesPerCell;
	
	public final double neighborhoodRadius;
	public final double distanceSelf;
	public final double cellWidth;
	
	public final double distanceParamA;
	public final double distanceParamB;
	public final double distanceScoreWeight;

	
	public final int treeRegenerationRate;
	public final int treeAttackTreshold;
	public final int minTreesForAttack;
	public final int maxBeetlesPerTree;
	public final double beetleReproductionRate;

	public Parameters(String parameterFilename){
		
		Map<String, String> pMap = CSVUtils.csvToHashMap(parameterFilename);
		
		randomSeed = Integer.parseInt(pMap.get("randomSeed"));
		
		nRows = Integer.parseInt(pMap.get("nRows"));
		nCols = Integer.parseInt(pMap.get("nCols"));
		
		nTreesPerCell = Integer.parseInt(pMap.get("nTreesPerCell"));
		nInitialBeetlesPerCell = Integer.parseInt(pMap.get("nInitialBeetlesPerCell"));
		
		neighborhoodRadius = Double.parseDouble(pMap.get("neighborhoodRadius"));
		distanceSelf = Double.parseDouble(pMap.get("distanceSelf"));
		cellWidth = Double.parseDouble(pMap.get("cellWidth"));
		
		distanceParamA = Double.parseDouble(pMap.get("distanceParamA"));
		distanceParamB = Double.parseDouble(pMap.get("distanceParamB"));
		distanceScoreWeight = Double.parseDouble(pMap.get("distanceScoreWeight"));
		
		treeRegenerationRate = Integer.parseInt(pMap.get("treeRegenerationRate"));
		treeAttackTreshold = Integer.parseInt(pMap.get("treeAttackTreshold"));
		minTreesForAttack = Integer.parseInt(pMap.get("minTreesForAttack"));
		maxBeetlesPerTree = Integer.parseInt(pMap.get("maxBeetlesPerTree"));
		beetleReproductionRate = Double.parseDouble(pMap.get("beetleReproductionRate"));


		
	}
	
	
	
	
}
