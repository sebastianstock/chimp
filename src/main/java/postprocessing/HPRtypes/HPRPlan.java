package postprocessing.HPRtypes;

import java.util.ArrayList;
import java.util.List;

public class HPRPlan {

    public List<Node> nodes = new ArrayList<>();
    public List<Edge> edges = new ArrayList<>();

    public void addEdge(Edge edge) {
        this.edges.add(edge);
        this.nodes.get(edge.sink_ids[0]).edges_in.add(edge.edge_id);
        this.nodes.get(edge.source_ids[0]).edges_out.add(edge.edge_id);
    }

    public void addNode(Node node) {
        this.nodes.add(node);
    }
}
