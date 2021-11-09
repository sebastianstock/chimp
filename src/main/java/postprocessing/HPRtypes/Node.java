package postprocessing.HPRtypes;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import postprocessing.estereltypes.KeyValue;

public class Node {
    
    public int node_type;
    public int node_id;
    public String action_name;
    public float duration;
    public float dispatch_time;
    public List<KeyValue> parameters = new ArrayList<>();
    public List<String> preconditions = new ArrayList<>();
    public List<String> positive_effects = new ArrayList<>();
    public List<String> negative_effects = new ArrayList<>();
    public List<Integer> edges_out = new ArrayList<>();
    public List<Integer> edges_in = new ArrayList<>();

    public Node(int node_id, int node_type, String action_name, float duration, float dispatch_time) {
        this.node_id = node_id;
        this.node_type = node_type;
        this.action_name = action_name;
        this.duration = duration;
        this.dispatch_time = dispatch_time;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Node that = (Node) o;
        return node_id == that.node_id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(node_id);
    }
}
