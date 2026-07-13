package externalPathPlanning;

import chimp.model.TemporalBounds;


import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

public class LookUpTableDurationEstimatorICO implements MoveBaseDurationEstimator {

	private static Table<String, String, TemporalBounds> boundsTable = HashBasedTable.create();
	
	static {
		// "preManipulationAreaEastCounter1"
		boundsTable.put("manipulationAreaEastCounter1", "manipulationAreaEastCounter1", new TemporalBounds(1, TemporalBounds.INFINITY));
		boundsTable.put("manipulationAreaEastCounter1", "manipulationAreaNorthTable1", new TemporalBounds(4000, TemporalBounds.INFINITY));
		boundsTable.put("manipulationAreaEastCounter1", "manipulationAreaSouthTable1", new TemporalBounds(3000, TemporalBounds.INFINITY));
		boundsTable.put("manipulationAreaEastCounter1", "manipulationAreaWestTable2" , new TemporalBounds(7000, TemporalBounds.INFINITY));
		boundsTable.put("manipulationAreaEastCounter1", "manipulationAreaEastTable2" , new TemporalBounds(6000, TemporalBounds.INFINITY));
		boundsTable.put("manipulationAreaEastCounter1", "floorAreaTamsRestaurant1"      , new TemporalBounds(3000, TemporalBounds.INFINITY));
		boundsTable.put("manipulationAreaEastCounter1", "manipulationAreaEastCounterOS1", new TemporalBounds(9000000, TemporalBounds.INFINITY));
		
		boundsTable.put("manipulationAreaNorthTable1", "manipulationAreaEastCounter1", new TemporalBounds(4000, TemporalBounds.INFINITY));
		boundsTable.put("manipulationAreaNorthTable1", "manipulationAreaNorthTable1", new TemporalBounds(1, TemporalBounds.INFINITY));
		boundsTable.put("manipulationAreaNorthTable1", "manipulationAreaSouthTable1", new TemporalBounds(1000, TemporalBounds.INFINITY));
		boundsTable.put("manipulationAreaNorthTable1", "manipulationAreaWestTable2" , new TemporalBounds(5000, TemporalBounds.INFINITY));
		boundsTable.put("manipulationAreaNorthTable1", "manipulationAreaEastTable2" , new TemporalBounds(1500, TemporalBounds.INFINITY));
		boundsTable.put("manipulationAreaNorthTable1", "floorAreaTamsRestaurant1"      , new TemporalBounds(3000, TemporalBounds.INFINITY));
		boundsTable.put("manipulationAreaNorthTable1", "manipulationAreaEastCounterOS1", new TemporalBounds(9000000, TemporalBounds.INFINITY));
		
		boundsTable.put("manipulationAreaSouthTable1", "manipulationAreaEastCounter1", new TemporalBounds(3000, TemporalBounds.INFINITY));
		boundsTable.put("manipulationAreaSouthTable1", "manipulationAreaNorthTable1", new TemporalBounds(1000, TemporalBounds.INFINITY));
		boundsTable.put("manipulationAreaSouthTable1", "manipulationAreaSouthTable1", new TemporalBounds(1, TemporalBounds.INFINITY));
		boundsTable.put("manipulationAreaSouthTable1", "manipulationAreaWestTable2" , new TemporalBounds(500, TemporalBounds.INFINITY));
		boundsTable.put("manipulationAreaSouthTable1", "manipulationAreaEastTable2" , new TemporalBounds(1500, TemporalBounds.INFINITY));
		boundsTable.put("manipulationAreaSouthTable1", "floorAreaTamsRestaurant1"      , new TemporalBounds(3000, TemporalBounds.INFINITY));
		boundsTable.put("manipulationAreaSouthTable1", "manipulationAreaEastCounterOS1", new TemporalBounds(9000000, TemporalBounds.INFINITY));

		boundsTable.put("manipulationAreaWestTable2", "manipulationAreaEastCounter1", new TemporalBounds(4000, TemporalBounds.INFINITY));
		boundsTable.put("manipulationAreaWestTable2", "manipulationAreaNorthTable1", new TemporalBounds(500, TemporalBounds.INFINITY));
		boundsTable.put("manipulationAreaWestTable2", "manipulationAreaSouthTable1", new TemporalBounds(500, TemporalBounds.INFINITY));
		boundsTable.put("manipulationAreaWestTable2", "manipulationAreaWestTable2" , new TemporalBounds(1, TemporalBounds.INFINITY));
		boundsTable.put("manipulationAreaWestTable2", "manipulationAreaEastTable2" , new TemporalBounds(1000, TemporalBounds.INFINITY));
		boundsTable.put("manipulationAreaWestTable2", "floorAreaTamsRestaurant1"      , new TemporalBounds(2000, TemporalBounds.INFINITY));
		boundsTable.put("manipulationAreaWestTable2", "manipulationAreaEastCounterOS1", new TemporalBounds(9000000, TemporalBounds.INFINITY));
		
		boundsTable.put("manipulationAreaEastTable2", "manipulationAreaEastCounter1", new TemporalBounds(6000, TemporalBounds.INFINITY));
		boundsTable.put("manipulationAreaEastTable2", "manipulationAreaNorthTable1", new TemporalBounds(1500, TemporalBounds.INFINITY));
		boundsTable.put("manipulationAreaEastTable2", "manipulationAreaSouthTable1", new TemporalBounds(1500, TemporalBounds.INFINITY));
		boundsTable.put("manipulationAreaEastTable2", "manipulationAreaWestTable2" , new TemporalBounds(1000, TemporalBounds.INFINITY));
		boundsTable.put("manipulationAreaEastTable2", "manipulationAreaEastTable2" , new TemporalBounds(1, TemporalBounds.INFINITY));
		boundsTable.put("manipulationAreaEastTable2", "floorAreaTamsRestaurant1"      , new TemporalBounds(2000, TemporalBounds.INFINITY));
		boundsTable.put("manipulationAreaEastTable2", "manipulationAreaEastCounterOS1", new TemporalBounds(9000000, TemporalBounds.INFINITY));
		
		boundsTable.put("floorAreaTamsRestaurant1", "manipulationAreaEastCounter1", new TemporalBounds(10000, TemporalBounds.INFINITY));//new TemporalBounds(3000, TemporalBounds.INFINITY - 2));
		boundsTable.put("floorAreaTamsRestaurant1", "manipulationAreaNorthTable1", new TemporalBounds(3000, TemporalBounds.INFINITY));
		boundsTable.put("floorAreaTamsRestaurant1", "manipulationAreaSouthTable1", new TemporalBounds(3000, TemporalBounds.INFINITY));
		boundsTable.put("floorAreaTamsRestaurant1", "manipulationAreaWestTable2" , new TemporalBounds(2000, TemporalBounds.INFINITY));
		boundsTable.put("floorAreaTamsRestaurant1", "manipulationAreaEastTable2" , new TemporalBounds(2000, TemporalBounds.INFINITY));
		boundsTable.put("floorAreaTamsRestaurant1", "floorAreaTamsRestaurant1"      , new TemporalBounds(1, TemporalBounds.INFINITY));
		boundsTable.put("floorAreaTamsRestaurant1", "manipulationAreaEastCounterOS1", new TemporalBounds(9000000, TemporalBounds.INFINITY));
		
		boundsTable.put("manipulationAreaEastCounterOS1", "manipulationAreaEastCounter1", new TemporalBounds(9000000, TemporalBounds.INFINITY));
		boundsTable.put("manipulationAreaEastCounterOS1", "manipulationAreaNorthTable1", new TemporalBounds(9000000, TemporalBounds.INFINITY));
		boundsTable.put("manipulationAreaEastCounterOS1", "manipulationAreaSouthTable1", new TemporalBounds(9000000, TemporalBounds.INFINITY));
		boundsTable.put("manipulationAreaEastCounterOS1", "manipulationAreaWestTable2" , new TemporalBounds(9000000, TemporalBounds.INFINITY));
		boundsTable.put("manipulationAreaEastCounterOS1", "manipulationAreaEastTable2" , new TemporalBounds(9000000, TemporalBounds.INFINITY));
		boundsTable.put("manipulationAreaEastCounterOS1", "floorAreaTamsRestaurant1"      , new TemporalBounds(9000000, TemporalBounds.INFINITY));
		boundsTable.put("manipulationAreaEastCounterOS1", "manipulationAreaEastCounterOS1"      , new TemporalBounds(1, TemporalBounds.INFINITY));
		
	}
	
	@Override
	public TemporalBounds estimateDuration(String fromArea, String toArea) {
		return boundsTable.get(fromArea, toArea);
	}

}
