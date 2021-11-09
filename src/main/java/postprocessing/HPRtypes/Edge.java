package postprocessing.HPRtypes;

public class Edge {

    // Edge Types:
    // DECOMPOSITION_EDGE = 0
    // TEMPORAL_EDGE = 1
    public int edge_type;
    public int edge_id;
    public int source_id;
    public int sink_id;

    public Edge(int edge_id, int edge_type, int source_id, int sink_id) {
        this.edge_id = edge_id;
        this.edge_type = edge_type;
        this.source_id = source_id;
        this.sink_id = sink_id;
    }
    
}
