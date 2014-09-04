package pfd0Symbolic;

import java.util.Comparator;
public class FluentComparator implements Comparator<Fluent>{
	
	private boolean EST;
	public FluentComparator(boolean EST) {
		super();
		this.EST = EST;
	}
	
	@Override
	public int compare(Fluent o1, Fluent o2) {
		if (EST) return (int)(o1.getAllenInterval().getEST()-o2.getAllenInterval().getEST());
		return (int)(o1.getAllenInterval().getEET()-o2.getAllenInterval().getEET());
	}


}
