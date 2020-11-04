package postprocessing.estereltypes;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ActionDispatch {

    public ActionDispatch(int action_id, String name) {
        this.action_id = action_id;
        this.name = name;
    }

    public int action_id;
    public String name;
    public List<KeyValue> parameters = new ArrayList<>();
    public float duration;
    public float dispatch_time;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ActionDispatch that = (ActionDispatch) o;
        return action_id == that.action_id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(action_id);
    }
}
