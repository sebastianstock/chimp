package postprocessing.estereltypes;

import java.util.ArrayList;
import java.util.List;

public class EsterelPlan {

    public List<EsterelPlanNode> nodes = new ArrayList<>();
    public List<EsterelPlanEdge> edges = new ArrayList<>();

    public void addEdge(EsterelPlanEdge edge) {
        edges.add(edge);
        nodes.get(edge.sink_ids[0]).edges_in.add(edge.edge_id);
        nodes.get(edge.source_ids[0]).edges_out.add(edge.edge_id);
    }
}
