package pfd0Symbolic;

import org.metacsp.time.Bounds;

public interface MoveBaseDurationEstimator {

	public Bounds estimateDuration(String fromArea, String toArea);
	
}
