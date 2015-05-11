package externalPathPlanning;

import org.metacsp.time.APSPSolver;
import org.metacsp.time.Bounds;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

public class LookUpTableDurationEstimator implements MoveBaseDurationEstimator {

	private static Table<String, String, Bounds> boundsTable = HashBasedTable.create();

	// "preManipulationAreaEastCounter1"
	// "preManipulationAreaNorthTable1"
	// "preManipulationAreaSouthTable1"
	// "preManipulationAreaWestTable2"
	// "preManipulationAreaEastTable2"
	// "floorAreaTamsRestaurant1"
	
	static {
		// "preManipulationAreaEastCounter1"
		boundsTable.put("preManipulationAreaEastCounter1", "preManipulationAreaEastCounter1", new Bounds(1, APSPSolver.INF));
		boundsTable.put("preManipulationAreaEastCounter1", "preManipulationAreaNorthTable1", new Bounds(4000, APSPSolver.INF));
		boundsTable.put("preManipulationAreaEastCounter1", "preManipulationAreaSouthTable1", new Bounds(3000, APSPSolver.INF));
		boundsTable.put("preManipulationAreaEastCounter1", "preManipulationAreaWestTable2" , new Bounds(7000, APSPSolver.INF));
		boundsTable.put("preManipulationAreaEastCounter1", "preManipulationAreaEastTable2" , new Bounds(6000, APSPSolver.INF));
		boundsTable.put("preManipulationAreaEastCounter1", "floorAreaTamsRestaurant1"      , new Bounds(3000, APSPSolver.INF));
		boundsTable.put("preManipulationAreaEastCounter1", "preManipulationAreaEastCounterOS1", new Bounds(9000000, APSPSolver.INF));
		
		boundsTable.put("preManipulationAreaNorthTable1", "preManipulationAreaEastCounter1", new Bounds(4000, APSPSolver.INF));
		boundsTable.put("preManipulationAreaNorthTable1", "preManipulationAreaNorthTable1", new Bounds(1, APSPSolver.INF));
		boundsTable.put("preManipulationAreaNorthTable1", "preManipulationAreaSouthTable1", new Bounds(1000, APSPSolver.INF));
		boundsTable.put("preManipulationAreaNorthTable1", "preManipulationAreaWestTable2" , new Bounds(5000, APSPSolver.INF));
		boundsTable.put("preManipulationAreaNorthTable1", "preManipulationAreaEastTable2" , new Bounds(1500, APSPSolver.INF));
		boundsTable.put("preManipulationAreaNorthTable1", "floorAreaTamsRestaurant1"      , new Bounds(3000, APSPSolver.INF));
		boundsTable.put("preManipulationAreaNorthTable1", "preManipulationAreaEastCounterOS1", new Bounds(9000000, APSPSolver.INF));
		
		boundsTable.put("preManipulationAreaSouthTable1", "preManipulationAreaEastCounter1", new Bounds(3000, APSPSolver.INF));
		boundsTable.put("preManipulationAreaSouthTable1", "preManipulationAreaNorthTable1", new Bounds(1000, APSPSolver.INF));
		boundsTable.put("preManipulationAreaSouthTable1", "preManipulationAreaSouthTable1", new Bounds(1, APSPSolver.INF));
		boundsTable.put("preManipulationAreaSouthTable1", "preManipulationAreaWestTable2" , new Bounds(500, APSPSolver.INF));
		boundsTable.put("preManipulationAreaSouthTable1", "preManipulationAreaEastTable2" , new Bounds(1500, APSPSolver.INF));
		boundsTable.put("preManipulationAreaSouthTable1", "floorAreaTamsRestaurant1"      , new Bounds(3000, APSPSolver.INF));
		boundsTable.put("preManipulationAreaSouthTable1", "preManipulationAreaEastCounterOS1", new Bounds(9000000, APSPSolver.INF));

		boundsTable.put("preManipulationAreaWestTable2", "preManipulationAreaEastCounter1", new Bounds(4000, APSPSolver.INF));
		boundsTable.put("preManipulationAreaWestTable2", "preManipulationAreaNorthTable1", new Bounds(500, APSPSolver.INF));
		boundsTable.put("preManipulationAreaWestTable2", "preManipulationAreaSouthTable1", new Bounds(500, APSPSolver.INF));
		boundsTable.put("preManipulationAreaWestTable2", "preManipulationAreaWestTable2" , new Bounds(1, APSPSolver.INF));
		boundsTable.put("preManipulationAreaWestTable2", "preManipulationAreaEastTable2" , new Bounds(1000, APSPSolver.INF));
		boundsTable.put("preManipulationAreaWestTable2", "floorAreaTamsRestaurant1"      , new Bounds(2000, APSPSolver.INF));
		boundsTable.put("preManipulationAreaWestTable2", "preManipulationAreaEastCounterOS1", new Bounds(9000000, APSPSolver.INF));
		
		boundsTable.put("preManipulationAreaEastTable2", "preManipulationAreaEastCounter1", new Bounds(6000, APSPSolver.INF));
		boundsTable.put("preManipulationAreaEastTable2", "preManipulationAreaNorthTable1", new Bounds(1500, APSPSolver.INF));
		boundsTable.put("preManipulationAreaEastTable2", "preManipulationAreaSouthTable1", new Bounds(1500, APSPSolver.INF));
		boundsTable.put("preManipulationAreaEastTable2", "preManipulationAreaWestTable2" , new Bounds(1000, APSPSolver.INF));
		boundsTable.put("preManipulationAreaEastTable2", "preManipulationAreaEastTable2" , new Bounds(1, APSPSolver.INF));
		boundsTable.put("preManipulationAreaEastTable2", "floorAreaTamsRestaurant1"      , new Bounds(2000, APSPSolver.INF));
		boundsTable.put("preManipulationAreaEastTable2", "preManipulationAreaEastCounterOS1", new Bounds(9000000, APSPSolver.INF));
		
		boundsTable.put("floorAreaTamsRestaurant1", "preManipulationAreaEastCounter1", new Bounds(10000, APSPSolver.INF));//new Bounds(3000, APSPSolver.INF - 2));
		boundsTable.put("floorAreaTamsRestaurant1", "preManipulationAreaNorthTable1", new Bounds(3000, APSPSolver.INF));
		boundsTable.put("floorAreaTamsRestaurant1", "preManipulationAreaSouthTable1", new Bounds(3000, APSPSolver.INF));
		boundsTable.put("floorAreaTamsRestaurant1", "preManipulationAreaWestTable2" , new Bounds(2000, APSPSolver.INF));
		boundsTable.put("floorAreaTamsRestaurant1", "preManipulationAreaEastTable2" , new Bounds(2000, APSPSolver.INF));
		boundsTable.put("floorAreaTamsRestaurant1", "floorAreaTamsRestaurant1"      , new Bounds(1, APSPSolver.INF));
		boundsTable.put("floorAreaTamsRestaurant1", "preManipulationAreaEastCounterOS1", new Bounds(9000000, APSPSolver.INF));
		
		boundsTable.put("preManipulationAreaEastCounterOS1", "preManipulationAreaEastCounter1", new Bounds(9000000, APSPSolver.INF));
		boundsTable.put("preManipulationAreaEastCounterOS1", "preManipulationAreaNorthTable1", new Bounds(9000000, APSPSolver.INF));
		boundsTable.put("preManipulationAreaEastCounterOS1", "preManipulationAreaSouthTable1", new Bounds(9000000, APSPSolver.INF));
		boundsTable.put("preManipulationAreaEastCounterOS1", "preManipulationAreaWestTable2" , new Bounds(9000000, APSPSolver.INF));
		boundsTable.put("preManipulationAreaEastCounterOS1", "preManipulationAreaEastTable2" , new Bounds(9000000, APSPSolver.INF));
		boundsTable.put("preManipulationAreaEastCounterOS1", "floorAreaTamsRestaurant1"      , new Bounds(9000000, APSPSolver.INF));
		boundsTable.put("preManipulationAreaEastCounterOS1", "preManipulationAreaEastCounterOS1"      , new Bounds(1, APSPSolver.INF));
		
	}
	
	@Override
	public Bounds estimateDuration(String fromArea, String toArea) {
		return boundsTable.get(fromArea, toArea);
	}

}
