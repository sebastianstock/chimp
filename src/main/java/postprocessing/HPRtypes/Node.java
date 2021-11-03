package postprocessing.HPRtypes;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Node {
    
    public int node_type;
    public int node_id;
    public String name;
    public HPRActionDispatch action;
    public List<Integer> edges_out = new ArrayList<>();
    public List<Integer> edges_in = new ArrayList<>();

    public Node(int node_id, int node_type, String name, HPRActionDispatch action) {
        this.node_id = node_id;
        this.node_type = node_type;
        this.name = name;
        this.action = action;
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
