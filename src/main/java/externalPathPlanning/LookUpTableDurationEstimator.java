package externalPathPlanning;

import chimp.model.TemporalBounds;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

public class LookUpTableDurationEstimator implements MoveBaseDurationEstimator {

	private static Table<String, String, TemporalBounds> boundsTable = HashBasedTable.create();

	// "preManipulationAreaEastCounter1"
	// "preManipulationAreaNorthTable1"
	// "preManipulationAreaSouthTable1"
	// "preManipulationAreaWestTable2"
	// "preManipulationAreaEastTable2"
	// "floorAreaTamsRestaurant1"
	
	static {
		// "preManipulationAreaEastCounter1"
		boundsTable.put("preManipulationAreaEastCounter1", "preManipulationAreaEastCounter1", new TemporalBounds(1, TemporalBounds.INFINITY));
		boundsTable.put("preManipulationAreaEastCounter1", "preManipulationAreaNorthTable1", new TemporalBounds(4000, TemporalBounds.INFINITY));
		boundsTable.put("preManipulationAreaEastCounter1", "preManipulationAreaSouthTable1", new TemporalBounds(3000, TemporalBounds.INFINITY));
		boundsTable.put("preManipulationAreaEastCounter1", "preManipulationAreaWestTable2" , new TemporalBounds(7000, TemporalBounds.INFINITY));
		boundsTable.put("preManipulationAreaEastCounter1", "preManipulationAreaEastTable2" , new TemporalBounds(6000, TemporalBounds.INFINITY));
		boundsTable.put("preManipulationAreaEastCounter1", "floorAreaTamsRestaurant1"      , new TemporalBounds(3000, TemporalBounds.INFINITY));
		boundsTable.put("preManipulationAreaEastCounter1", "preManipulationAreaEastCounterOS1", new TemporalBounds(9000000, TemporalBounds.INFINITY));
		
		boundsTable.put("preManipulationAreaNorthTable1", "preManipulationAreaEastCounter1", new TemporalBounds(4000, TemporalBounds.INFINITY));
		boundsTable.put("preManipulationAreaNorthTable1", "preManipulationAreaNorthTable1", new TemporalBounds(1, TemporalBounds.INFINITY));
		boundsTable.put("preManipulationAreaNorthTable1", "preManipulationAreaSouthTable1", new TemporalBounds(1000, TemporalBounds.INFINITY));
		boundsTable.put("preManipulationAreaNorthTable1", "preManipulationAreaWestTable2" , new TemporalBounds(5000, TemporalBounds.INFINITY));
		boundsTable.put("preManipulationAreaNorthTable1", "preManipulationAreaEastTable2" , new TemporalBounds(1500, TemporalBounds.INFINITY));
		boundsTable.put("preManipulationAreaNorthTable1", "floorAreaTamsRestaurant1"      , new TemporalBounds(3000, TemporalBounds.INFINITY));
		boundsTable.put("preManipulationAreaNorthTable1", "preManipulationAreaEastCounterOS1", new TemporalBounds(9000000, TemporalBounds.INFINITY));
		
		boundsTable.put("preManipulationAreaSouthTable1", "preManipulationAreaEastCounter1", new TemporalBounds(3000, TemporalBounds.INFINITY));
		boundsTable.put("preManipulationAreaSouthTable1", "preManipulationAreaNorthTable1", new TemporalBounds(1000, TemporalBounds.INFINITY));
		boundsTable.put("preManipulationAreaSouthTable1", "preManipulationAreaSouthTable1", new TemporalBounds(1, TemporalBounds.INFINITY));
		boundsTable.put("preManipulationAreaSouthTable1", "preManipulationAreaWestTable2" , new TemporalBounds(500, TemporalBounds.INFINITY));
		boundsTable.put("preManipulationAreaSouthTable1", "preManipulationAreaEastTable2" , new TemporalBounds(1500, TemporalBounds.INFINITY));
		boundsTable.put("preManipulationAreaSouthTable1", "floorAreaTamsRestaurant1"      , new TemporalBounds(3000, TemporalBounds.INFINITY));
		boundsTable.put("preManipulationAreaSouthTable1", "preManipulationAreaEastCounterOS1", new TemporalBounds(9000000, TemporalBounds.INFINITY));

		boundsTable.put("preManipulationAreaWestTable2", "preManipulationAreaEastCounter1", new TemporalBounds(4000, TemporalBounds.INFINITY));
		boundsTable.put("preManipulationAreaWestTable2", "preManipulationAreaNorthTable1", new TemporalBounds(500, TemporalBounds.INFINITY));
		boundsTable.put("preManipulationAreaWestTable2", "preManipulationAreaSouthTable1", new TemporalBounds(500, TemporalBounds.INFINITY));
		boundsTable.put("preManipulationAreaWestTable2", "preManipulationAreaWestTable2" , new TemporalBounds(1, TemporalBounds.INFINITY));
		boundsTable.put("preManipulationAreaWestTable2", "preManipulationAreaEastTable2" , new TemporalBounds(1000, TemporalBounds.INFINITY));
		boundsTable.put("preManipulationAreaWestTable2", "floorAreaTamsRestaurant1"      , new TemporalBounds(2000, TemporalBounds.INFINITY));
		boundsTable.put("preManipulationAreaWestTable2", "preManipulationAreaEastCounterOS1", new TemporalBounds(9000000, TemporalBounds.INFINITY));
		
		boundsTable.put("preManipulationAreaEastTable2", "preManipulationAreaEastCounter1", new TemporalBounds(6000, TemporalBounds.INFINITY));
		boundsTable.put("preManipulationAreaEastTable2", "preManipulationAreaNorthTable1", new TemporalBounds(1500, TemporalBounds.INFINITY));
		boundsTable.put("preManipulationAreaEastTable2", "preManipulationAreaSouthTable1", new TemporalBounds(1500, TemporalBounds.INFINITY));
		boundsTable.put("preManipulationAreaEastTable2", "preManipulationAreaWestTable2" , new TemporalBounds(1000, TemporalBounds.INFINITY));
		boundsTable.put("preManipulationAreaEastTable2", "preManipulationAreaEastTable2" , new TemporalBounds(1, TemporalBounds.INFINITY));
		boundsTable.put("preManipulationAreaEastTable2", "floorAreaTamsRestaurant1"      , new TemporalBounds(2000, TemporalBounds.INFINITY));
		boundsTable.put("preManipulationAreaEastTable2", "preManipulationAreaEastCounterOS1", new TemporalBounds(9000000, TemporalBounds.INFINITY));
		
		boundsTable.put("floorAreaTamsRestaurant1", "preManipulationAreaEastCounter1", new TemporalBounds(10000, TemporalBounds.INFINITY));
		boundsTable.put("floorAreaTamsRestaurant1", "preManipulationAreaNorthTable1", new TemporalBounds(3000, TemporalBounds.INFINITY));
		boundsTable.put("floorAreaTamsRestaurant1", "preManipulationAreaSouthTable1", new TemporalBounds(3000, TemporalBounds.INFINITY));
		boundsTable.put("floorAreaTamsRestaurant1", "preManipulationAreaWestTable2" , new TemporalBounds(2000, TemporalBounds.INFINITY));
		boundsTable.put("floorAreaTamsRestaurant1", "preManipulationAreaEastTable2" , new TemporalBounds(2000, TemporalBounds.INFINITY));
		boundsTable.put("floorAreaTamsRestaurant1", "floorAreaTamsRestaurant1"      , new TemporalBounds(1, TemporalBounds.INFINITY));
		boundsTable.put("floorAreaTamsRestaurant1", "preManipulationAreaEastCounterOS1", new TemporalBounds(9000000, TemporalBounds.INFINITY));
		
		boundsTable.put("preManipulationAreaEastCounterOS1", "preManipulationAreaEastCounter1", new TemporalBounds(9000000, TemporalBounds.INFINITY));
		boundsTable.put("preManipulationAreaEastCounterOS1", "preManipulationAreaNorthTable1", new TemporalBounds(9000000, TemporalBounds.INFINITY));
		boundsTable.put("preManipulationAreaEastCounterOS1", "preManipulationAreaSouthTable1", new TemporalBounds(9000000, TemporalBounds.INFINITY));
		boundsTable.put("preManipulationAreaEastCounterOS1", "preManipulationAreaWestTable2" , new TemporalBounds(9000000, TemporalBounds.INFINITY));
		boundsTable.put("preManipulationAreaEastCounterOS1", "preManipulationAreaEastTable2" , new TemporalBounds(9000000, TemporalBounds.INFINITY));
		boundsTable.put("preManipulationAreaEastCounterOS1", "floorAreaTamsRestaurant1"      , new TemporalBounds(9000000, TemporalBounds.INFINITY));
		boundsTable.put("preManipulationAreaEastCounterOS1", "preManipulationAreaEastCounterOS1"      , new TemporalBounds(1, TemporalBounds.INFINITY));
		
	}
	
	@Override
	public TemporalBounds estimateDuration(String fromArea, String toArea) {
		return boundsTable.get(fromArea, toArea);
	}

}
