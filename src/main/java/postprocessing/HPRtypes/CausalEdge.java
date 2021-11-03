package postprocessing.HPRtypes;

public class CausalEdge extends Edge {

    public CausalEdge(int edge_id, String edge_name, int source_id, int sink_id) {
        super(edge_id, edge_name, source_id, sink_id);
        this.edge_type = 1;
    }
    
}
