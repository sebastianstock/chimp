package postprocessing.HPRtypes;

public class TemporalEdge extends Edge {

    // Connection Types:
    // START_TO_START = 0
    // START_TO_END = 1
    // END_TO_START = 2
    // END_TO_END = 3
    public int connection_type;
    public double duration_lower_bound;
    public double duration_upper_bound;

    public TemporalEdge(int edge_id, int source_id, int sink_id, double duration_lower_bound, double duration_upper_bound, int connection_type) {
        super(edge_id, 1, source_id, sink_id);
        this.duration_lower_bound = duration_lower_bound;
        this.duration_upper_bound = duration_upper_bound;
        this.connection_type = connection_type;
    }
}
