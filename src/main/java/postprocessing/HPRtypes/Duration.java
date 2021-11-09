package postprocessing.HPRtypes;

import org.metacsp.time.TimePoint;

public class Duration {

    public TimePoint start;
    public TimePoint end;
    public float duration;

    public Duration(TimePoint start, TimePoint end) {
        this.start = start;
        this.end = end;
        this.duration = calcDuration();
    }

    public boolean equals (Object obj) {
        return (obj instanceof Duration) && (((Duration) obj).start.equals(this.start) && (((Duration) obj).end.equals(this.end)));

	}

    private float calcDuration() {
        return ((Long) this.end.getDomain().chooseValue("ET") - (Long) this.start.getDomain().chooseValue("ET")) / 1000;
    }
}
