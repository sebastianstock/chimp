package externalPathPlanning;

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
		boundsTable.put("preManipulationAreaEastCounter1", "preManipulationAreaEastCounter1", new Bounds(0, 2000));
		boundsTable.put("preManipulationAreaEastCounter1", "preManipulationAreaNorthTable1", new Bounds(20000, 30000));
		boundsTable.put("preManipulationAreaEastCounter1", "preManipulationAreaSouthTable1", new Bounds(10000, 25000));
		boundsTable.put("preManipulationAreaEastCounter1", "preManipulationAreaWestTable2" , new Bounds(30000, 40000));
		boundsTable.put("preManipulationAreaEastCounter1", "preManipulationAreaEastTable2" , new Bounds(30000, 40000));
		boundsTable.put("preManipulationAreaEastCounter1", "floorAreaTamsRestaurant1"      , new Bounds(30000, 40000));
		
		boundsTable.put("preManipulationAreaNorthTable1", "preManipulationAreaEastCounter1", new Bounds(20000, 30000));
		boundsTable.put("preManipulationAreaNorthTable1", "preManipulationAreaNorthTable1", new Bounds(0, 2000));
		boundsTable.put("preManipulationAreaNorthTable1", "preManipulationAreaSouthTable1", new Bounds(10000, 20000));
		boundsTable.put("preManipulationAreaNorthTable1", "preManipulationAreaWestTable2" , new Bounds(5000, 15000));
		boundsTable.put("preManipulationAreaNorthTable1", "preManipulationAreaEastTable2" , new Bounds(15000, 25000));
		boundsTable.put("preManipulationAreaNorthTable1", "floorAreaTamsRestaurant1"      , new Bounds(30000, 40000));
		
		boundsTable.put("preManipulationAreaSouthTable1", "preManipulationAreaEastCounter1", new Bounds(15000, 25000));
		boundsTable.put("preManipulationAreaSouthTable1", "preManipulationAreaNorthTable1", new Bounds(10000, 20000));
		boundsTable.put("preManipulationAreaSouthTable1", "preManipulationAreaSouthTable1", new Bounds(0, 2000));
		boundsTable.put("preManipulationAreaSouthTable1", "preManipulationAreaWestTable2" , new Bounds(5000, 15000));
		boundsTable.put("preManipulationAreaSouthTable1", "preManipulationAreaEastTable2" , new Bounds(15000, 25000));
		boundsTable.put("preManipulationAreaSouthTable1", "floorAreaTamsRestaurant1"      , new Bounds(30000, 40000));

		boundsTable.put("preManipulationAreaWestTable2", "preManipulationAreaEastCounter1", new Bounds(30000, 40000));
		boundsTable.put("preManipulationAreaWestTable2", "preManipulationAreaNorthTable1", new Bounds(5000, 15000));
		boundsTable.put("preManipulationAreaWestTable2", "preManipulationAreaSouthTable1", new Bounds(5000, 15000));
		boundsTable.put("preManipulationAreaWestTable2", "preManipulationAreaWestTable2" , new Bounds(0, 2000));
		boundsTable.put("preManipulationAreaWestTable2", "preManipulationAreaEastTable2" , new Bounds(10000, 20000));
		boundsTable.put("preManipulationAreaWestTable2", "floorAreaTamsRestaurant1"      , new Bounds(20000, 30000));
		
		boundsTable.put("preManipulationAreaEastTable2", "preManipulationAreaEastCounter1", new Bounds(30000, 40000));
		boundsTable.put("preManipulationAreaEastTable2", "preManipulationAreaNorthTable1", new Bounds(15000, 25000));
		boundsTable.put("preManipulationAreaEastTable2", "preManipulationAreaSouthTable1", new Bounds(15000, 25000));
		boundsTable.put("preManipulationAreaEastTable2", "preManipulationAreaWestTable2" , new Bounds(10000, 20000));
		boundsTable.put("preManipulationAreaEastTable2", "preManipulationAreaEastTable2" , new Bounds(0, 2000));
		boundsTable.put("preManipulationAreaEastTable2", "floorAreaTamsRestaurant1"      , new Bounds(20000, 30000));
		
		boundsTable.put("floorAreaTamsRestaurant1", "preManipulationAreaEastCounter1", new Bounds(30000, 40000));
		boundsTable.put("floorAreaTamsRestaurant1", "preManipulationAreaNorthTable1", new Bounds(30000, 40000));
		boundsTable.put("floorAreaTamsRestaurant1", "preManipulationAreaSouthTable1", new Bounds(30000, 40000));
		boundsTable.put("floorAreaTamsRestaurant1", "preManipulationAreaWestTable2" , new Bounds(20000, 30000));
		boundsTable.put("floorAreaTamsRestaurant1", "preManipulationAreaEastTable2" , new Bounds(20000, 30000));
		boundsTable.put("floorAreaTamsRestaurant1", "floorAreaTamsRestaurant1"      , new Bounds(0, 2000));
	}
	
	@Override
	public Bounds estimateDuration(String fromArea, String toArea) {
		return boundsTable.get(fromArea, toArea);
	}

}
