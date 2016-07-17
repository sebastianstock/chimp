package examples;

public class TestRuntimes {

	public static void main(String[] args) {
		int runs = 1;
		double[] times = new double[runs];
		for (int i = 0; i < runs ; i++) {
			times[i] = TestDWRDomain.plan_dwr(null);
		}
		double sum = 0;
		System.out.println("RESULTS:");
		for (int i = 0; i < runs; i++) {
			System.out.println(i + ": " + times[i]);
			sum += times[i];
		}
		System.out.println("Average: " + sum / runs);
	}

}
