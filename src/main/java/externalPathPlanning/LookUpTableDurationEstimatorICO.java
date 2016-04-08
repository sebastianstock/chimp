package externalPathPlanning;

import org.metacsp.time.APSPSolver;
import org.metacsp.time.Bounds;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

public class LookUpTableDurationEstimatorICO implements MoveBaseDurationEstimator {

	private static Table<String, String, Bounds> boundsTable = HashBasedTable.create();
	
	static {
		// "preManipulationAreaEastCounter1"
		boundsTable.put("manipulationAreaEastCounter1", "manipulationAreaEastCounter1", new Bounds(1, APSPSolver.INF));
		boundsTable.put("manipulationAreaEastCounter1", "manipulationAreaNorthTable1", new Bounds(4000, APSPSolver.INF));
		boundsTable.put("manipulationAreaEastCounter1", "manipulationAreaSouthTable1", new Bounds(3000, APSPSolver.INF));
		boundsTable.put("manipulationAreaEastCounter1", "manipulationAreaWestTable2" , new Bounds(7000, APSPSolver.INF));
		boundsTable.put("manipulationAreaEastCounter1", "manipulationAreaEastTable2" , new Bounds(6000, APSPSolver.INF));
		boundsTable.put("manipulationAreaEastCounter1", "floorAreaTamsRestaurant1"      , new Bounds(3000, APSPSolver.INF));
		boundsTable.put("manipulationAreaEastCounter1", "manipulationAreaEastCounterOS1", new Bounds(9000000, APSPSolver.INF));
		
		boundsTable.put("manipulationAreaNorthTable1", "manipulationAreaEastCounter1", new Bounds(4000, APSPSolver.INF));
		boundsTable.put("manipulationAreaNorthTable1", "manipulationAreaNorthTable1", new Bounds(1, APSPSolver.INF));
		boundsTable.put("manipulationAreaNorthTable1", "manipulationAreaSouthTable1", new Bounds(1000, APSPSolver.INF));
		boundsTable.put("manipulationAreaNorthTable1", "manipulationAreaWestTable2" , new Bounds(5000, APSPSolver.INF));
		boundsTable.put("manipulationAreaNorthTable1", "manipulationAreaEastTable2" , new Bounds(1500, APSPSolver.INF));
		boundsTable.put("manipulationAreaNorthTable1", "floorAreaTamsRestaurant1"      , new Bounds(3000, APSPSolver.INF));
		boundsTable.put("manipulationAreaNorthTable1", "manipulationAreaEastCounterOS1", new Bounds(9000000, APSPSolver.INF));
		
		boundsTable.put("manipulationAreaSouthTable1", "manipulationAreaEastCounter1", new Bounds(3000, APSPSolver.INF));
		boundsTable.put("manipulationAreaSouthTable1", "manipulationAreaNorthTable1", new Bounds(1000, APSPSolver.INF));
		boundsTable.put("manipulationAreaSouthTable1", "manipulationAreaSouthTable1", new Bounds(1, APSPSolver.INF));
		boundsTable.put("manipulationAreaSouthTable1", "manipulationAreaWestTable2" , new Bounds(500, APSPSolver.INF));
		boundsTable.put("manipulationAreaSouthTable1", "manipulationAreaEastTable2" , new Bounds(1500, APSPSolver.INF));
		boundsTable.put("manipulationAreaSouthTable1", "floorAreaTamsRestaurant1"      , new Bounds(3000, APSPSolver.INF));
		boundsTable.put("manipulationAreaSouthTable1", "manipulationAreaEastCounterOS1", new Bounds(9000000, APSPSolver.INF));

		boundsTable.put("manipulationAreaWestTable2", "manipulationAreaEastCounter1", new Bounds(4000, APSPSolver.INF));
		boundsTable.put("manipulationAreaWestTable2", "manipulationAreaNorthTable1", new Bounds(500, APSPSolver.INF));
		boundsTable.put("manipulationAreaWestTable2", "manipulationAreaSouthTable1", new Bounds(500, APSPSolver.INF));
		boundsTable.put("manipulationAreaWestTable2", "manipulationAreaWestTable2" , new Bounds(1, APSPSolver.INF));
		boundsTable.put("manipulationAreaWestTable2", "manipulationAreaEastTable2" , new Bounds(1000, APSPSolver.INF));
		boundsTable.put("manipulationAreaWestTable2", "floorAreaTamsRestaurant1"      , new Bounds(2000, APSPSolver.INF));
		boundsTable.put("manipulationAreaWestTable2", "manipulationAreaEastCounterOS1", new Bounds(9000000, APSPSolver.INF));
		
		boundsTable.put("manipulationAreaEastTable2", "manipulationAreaEastCounter1", new Bounds(6000, APSPSolver.INF));
		boundsTable.put("manipulationAreaEastTable2", "manipulationAreaNorthTable1", new Bounds(1500, APSPSolver.INF));
		boundsTable.put("manipulationAreaEastTable2", "manipulationAreaSouthTable1", new Bounds(1500, APSPSolver.INF));
		boundsTable.put("manipulationAreaEastTable2", "manipulationAreaWestTable2" , new Bounds(1000, APSPSolver.INF));
		boundsTable.put("manipulationAreaEastTable2", "manipulationAreaEastTable2" , new Bounds(1, APSPSolver.INF));
		boundsTable.put("manipulationAreaEastTable2", "floorAreaTamsRestaurant1"      , new Bounds(2000, APSPSolver.INF));
		boundsTable.put("manipulationAreaEastTable2", "manipulationAreaEastCounterOS1", new Bounds(9000000, APSPSolver.INF));
		
		boundsTable.put("floorAreaTamsRestaurant1", "manipulationAreaEastCounter1", new Bounds(10000, APSPSolver.INF));//new Bounds(3000, APSPSolver.INF - 2));
		boundsTable.put("floorAreaTamsRestaurant1", "manipulationAreaNorthTable1", new Bounds(3000, APSPSolver.INF));
		boundsTable.put("floorAreaTamsRestaurant1", "manipulationAreaSouthTable1", new Bounds(3000, APSPSolver.INF));
		boundsTable.put("floorAreaTamsRestaurant1", "manipulationAreaWestTable2" , new Bounds(2000, APSPSolver.INF));
		boundsTable.put("floorAreaTamsRestaurant1", "manipulationAreaEastTable2" , new Bounds(2000, APSPSolver.INF));
		boundsTable.put("floorAreaTamsRestaurant1", "floorAreaTamsRestaurant1"      , new Bounds(1, APSPSolver.INF));
		boundsTable.put("floorAreaTamsRestaurant1", "manipulationAreaEastCounterOS1", new Bounds(9000000, APSPSolver.INF));
		
		boundsTable.put("manipulationAreaEastCounterOS1", "manipulationAreaEastCounter1", new Bounds(9000000, APSPSolver.INF));
		boundsTable.put("manipulationAreaEastCounterOS1", "manipulationAreaNorthTable1", new Bounds(9000000, APSPSolver.INF));
		boundsTable.put("manipulationAreaEastCounterOS1", "manipulationAreaSouthTable1", new Bounds(9000000, APSPSolver.INF));
		boundsTable.put("manipulationAreaEastCounterOS1", "manipulationAreaWestTable2" , new Bounds(9000000, APSPSolver.INF));
		boundsTable.put("manipulationAreaEastCounterOS1", "manipulationAreaEastTable2" , new Bounds(9000000, APSPSolver.INF));
		boundsTable.put("manipulationAreaEastCounterOS1", "floorAreaTamsRestaurant1"      , new Bounds(9000000, APSPSolver.INF));
		boundsTable.put("manipulationAreaEastCounterOS1", "manipulationAreaEastCounterOS1"      , new Bounds(1, APSPSolver.INF));
		
	}
	
	@Override
	public Bounds estimateDuration(String fromArea, String toArea) {
		return boundsTable.get(fromArea, toArea);
	}

}
