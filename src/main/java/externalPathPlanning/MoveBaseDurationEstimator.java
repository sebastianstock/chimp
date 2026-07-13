package externalPathPlanning;

import chimp.model.TemporalBounds;

public interface MoveBaseDurationEstimator {

	TemporalBounds estimateDuration(String fromArea, String toArea);
	
}
