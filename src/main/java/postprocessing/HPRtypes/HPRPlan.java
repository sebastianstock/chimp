package postprocessing.HPRtypes;

import java.util.ArrayList;
import java.util.List;

public class HPRPlan {

    public List<Node> nodes = new ArrayList<>();
    public List<Edge> edges = new ArrayList<>();

    public void addEdge(Edge edge) {
        this.edges.add(edge);
        searchNodeByID(edge.sink_id).edges_in.add(edge.edge_id);
        searchNodeByID(edge.source_id).edges_out.add(edge.edge_id);
    }

    public void addNode(Node node) {
        this.nodes.add(node);
    }

    private Node searchNodeByID(int node_id) {
        for(Node node : this.nodes) {
            if (node.node_id == node_id) {
                return node;
            }
        }
        return null;
    }
}
