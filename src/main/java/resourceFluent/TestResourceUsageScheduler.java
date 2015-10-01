package resourceFluent;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.metacsp.framework.ConstraintNetwork;
import org.metacsp.multi.allenInterval.AllenIntervalConstraint;
import org.metacsp.time.Bounds;
import org.metacsp.utility.UI.Callback;
import org.metacsp.utility.logging.MetaCSPLogging;

import fluentSolver.Fluent;
import fluentSolver.FluentConstraint;
import fluentSolver.FluentNetworkSolver;
import htn.HTNPlanner;

public class TestResourceUsageScheduler {

	public static void main(String[] args) {
		Logger logger = MetaCSPLogging.getLogger(TestResourceUsageScheduler.class);
		String[] symbolsPredicates = {"on", "robotat", "get_mug"};
		String[] symbolsMugs = {"mug1", "mug2", "mug3", "mug4", "mug5", "mug6", "mug7", "mug8", "mug9", "mug10", "none"};
		String[] symbolsPlAreas = {"pl1", "pl2", "pl3", "pl4", "pl5", "pl6", "pl7", "pl8", "pl9", "pl10", "none"};
		String[] symbolsManAreas = {"ma1", "ma2", "ma3", "ma4", "ma5", "ma6", "ma7", "ma8", "ma9", "ma10", "none"};
		String[] symbolsPreAreas = {"pma1", "pma2", "pma3", "pma4", "pma5", "pma6", "pma7", "pma8", "pma9", "pma10", "none"};
		String[][] symbols = new String[5][];
		symbols[0] = symbolsPredicates;
		symbols[1] = symbolsMugs;
		symbols[2] = symbolsPlAreas;
		symbols[3] = symbolsManAreas;
		symbols[4] = symbolsPreAreas;
		
		final HTNPlanner planner = new HTNPlanner(0,  600000,  0, symbols, new int[] {1,1,1,1,1});
		FluentNetworkSolver fluentSolver = (FluentNetworkSolver)planner.getConstraintSolvers()[0];
		
		Fluent[] fluents = (Fluent[]) fluentSolver.createVariables(3);


		Callback cb = new Callback() {
			@Override
			public void performOperation() {
				System.out.println("SOLVED? " + planner.backtrack());				
			}
		};
		ConstraintNetwork.draw(fluentSolver.getConstraintNetwork(),cb);
		
		fluents[0].setName("get_mug(mug1 pl1 none none)");
		fluents[1].setName("get_mug(mug1 pl2 none none)");
		fluents[2].setName("robotat(none pl2 none none)");
		
		AllenIntervalConstraint con1 = new AllenIntervalConstraint(AllenIntervalConstraint.Type.Duration, new Bounds(10000,20000));
		con1.setFrom(fluents[0]);
		con1.setTo(fluents[0]);

		AllenIntervalConstraint con2 = new AllenIntervalConstraint(AllenIntervalConstraint.Type.Duration, new Bounds(10000,20000));
		con2.setFrom(fluents[1]);
		con2.setTo(fluents[1]);

		AllenIntervalConstraint con3 = new AllenIntervalConstraint(AllenIntervalConstraint.Type.Duration, new Bounds(10000,20000));
		con3.setFrom(fluents[2]);
		con3.setTo(fluents[2]);
		
		fluentSolver.addConstraints(con1,con2,con3);
		
		ResourceUsageTemplate rt = new ResourceUsageTemplate("resourceName", new int[] {1}, new String[] {"mug1"}, 1);
		FluentConstraint rcon1 = new FluentConstraint(FluentConstraint.Type.RESOURCEUSAGE, rt);
		rcon1.setFrom(fluents[0]);
		rcon1.setTo(fluents[0]);
		
		FluentConstraint rcon2 = new FluentConstraint(FluentConstraint.Type.RESOURCEUSAGE, rt);
		rcon2.setFrom(fluents[1]);
		rcon2.setTo(fluents[1]);
		
		fluentSolver.addConstraints(rcon1,rcon2);
		
		FluentResourceUsageScheduler frs = new FluentResourceUsageScheduler(null, null, "resourceName", 1);
//		FluentScheduler fs = new FluentScheduler(null, null, "get_mug", 1, "mug1");
//		fs.setUsage(fluents);		
		planner.addMetaConstraint(frs);
		MetaCSPLogging.setLevel(Level.FINEST);

	}

}
