package postprocessing.estereltypes;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class EsterelPlanNode {

    public int node_type;

    public int node_id;

    public String name;

    public ActionDispatch action;

    public List<Integer> edges_out = new ArrayList<>();

    public List<Integer> edges_in = new ArrayList<>();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EsterelPlanNode that = (EsterelPlanNode) o;
        return node_id == that.node_id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(node_id);
    }
}
