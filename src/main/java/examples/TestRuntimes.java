package examples;

public class TestRuntimes {
	
	static final int RUNS = 2;

	public static void main(String[] args) {
		double[] times = new double[RUNS];
		for (int i = 0; i < RUNS ; i++) {
			times[i] = TestDWRDomain.plan_dwr(null);
		}
		double sum = 0;
		System.out.println("RESULTS:");
		for (int i = 0; i < RUNS; i++) {
			System.out.println(i + ": " + times[i]);
			sum += times[i];
		}
		System.out.println("Average: " + sum / RUNS);
	}

}
