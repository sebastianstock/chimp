package externalPathPlanning;

import org.metacsp.time.Bounds;

public interface MoveBaseDurationEstimator {

	Bounds estimateDuration(String fromArea, String toArea);
	
}
