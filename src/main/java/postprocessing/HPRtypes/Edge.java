package postprocessing.HPRtypes;

public class Edge {

    public int edge_id;
    public String edge_name;
    //EDGE = 0
    //CAUSAL_EDGE = 1
    //INTERFERENCE_EDGE = 2
    //TEMPORAL_EDGE = 3
    //CONDITION_EDGE = 4
    public int edge_type = 0;
    public int[] source_ids = new int[1];
    public int[] sink_ids = new int[1];

    public Edge(int edge_id, String edge_name, int source_id, int sink_id) {
        this.edge_id = edge_id;
        this.edge_name = edge_name;
        this.source_ids[0] = source_id;
        this.sink_ids[0] = sink_id;
    }

    public Edge(int edge_id, String edge_name, int edge_type, int source_id, int sink_id) {
        this.edge_id = edge_id;
        this.edge_name = edge_name;
        this.edge_type = edge_type;
        this.source_ids[0] = source_id;
        this.sink_ids[0] = sink_id;
    }
    
}
