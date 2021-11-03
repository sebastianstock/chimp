package postprocessing.HPRtypes;

public class TemporalEdge extends Edge {

    public int signal_type = 0;
    public double duration_lower_bound;
    public double duration_upper_bound;
    public String connection_type;

    public TemporalEdge(int edge_id, String edge_name, int source_id, int sink_id, double duration_lower_bound, double duration_upper_bound, String connection_type) {
        super(edge_id, edge_name, source_id, sink_id);
        this.edge_type = 3;
        this.duration_lower_bound = duration_lower_bound;
        this.duration_upper_bound = duration_upper_bound;
        this.connection_type = connection_type;
    }
}
