package postprocessing.estereltypes;

public class EsterelPlanEdge {


    public EsterelPlanEdge(int edge_id, String edge_name,
                           int source_id, int sink_id,
                           double duration_lower_bound, double duration_upper_bound) {
        this.edge_id = edge_id;
        this.edge_name = edge_name;
        this.source_ids[0] = source_id;
        this.sink_ids[0] = sink_id;
        this.duration_lower_bound = duration_lower_bound;
        this.duration_upper_bound = duration_upper_bound;
    }

    public int edge_id;
    public String edge_name;
    public int signal_type = 0;
    public int[] source_ids = new int[1];
    public int[] sink_ids = new int[1];
    public double duration_lower_bound;
    public double duration_upper_bound;
}
