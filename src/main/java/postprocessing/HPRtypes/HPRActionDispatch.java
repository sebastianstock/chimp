package postprocessing.HPRtypes;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import postprocessing.estereltypes.KeyValue;

public class HPRActionDispatch {

    public HPRActionDispatch(int action_id, String name) {
        this.action_id = action_id;
        this.name = name;
    }

    public int action_id;
    public String name;
    public float duration;
    public float dispatch_time;
    public List<KeyValue> parameters = new ArrayList<>();
    public List<String> preconditions = new ArrayList<>();
    public List<String> effects = new ArrayList<>();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HPRActionDispatch that = (HPRActionDispatch) o;
        return action_id == that.action_id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(action_id);
    }
}
