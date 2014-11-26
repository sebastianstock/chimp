package pfd0Symbolic;

import org.metacsp.multi.allenInterval.AllenIntervalConstraint;

public class AdditionalConstraintTemplate {

	private final AllenIntervalConstraint constraint;
	private final String fromKey;
	private final String toKey;
	
	public AdditionalConstraintTemplate(AllenIntervalConstraint constraint, String fromKey, String toKey) {
		this.constraint = constraint;
		this.fromKey = fromKey;
		this.toKey = toKey;
	}

	public AllenIntervalConstraint getConstraint() {
		return constraint;
	}

	public String getFromKey() {
		return fromKey;
	}

	public String getToKey() {
		return toKey;
	}

	public boolean involvesHeadAndKey(String key) {
		if ((fromKey.equals(PlanReportroryItem.HEAD_KEYWORD_STRING) && (toKey.equals(key))) ||
				(toKey.equals(PlanReportroryItem.HEAD_KEYWORD_STRING) && (fromKey.equals(key))) ) {
			return true;
		} else {
			return false;
		}
	}
	
	public boolean startsFromHead() {
		return fromKey.equals(PlanReportroryItem.HEAD_KEYWORD_STRING);
	}
}
