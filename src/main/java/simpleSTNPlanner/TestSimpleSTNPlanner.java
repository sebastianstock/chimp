/*******************************************************************************
 * Copyright (c) 2010-2013 Federico Pecora <federico.pecora@oru.se>
 * 
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 * 
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 ******************************************************************************/
package simpleSTNPlanner;

import java.util.logging.Level;

import org.metacsp.multi.activity.Activity;
import org.metacsp.multi.activity.ActivityNetworkSolver;
import org.metacsp.multi.allenInterval.AllenIntervalConstraint;
import org.metacsp.time.APSPSolver;
import org.metacsp.time.Bounds;
import org.metacsp.utility.logging.MetaCSPLogging;
import org.metacsp.utility.timelinePlotting.TimelinePublisher;
import org.metacsp.utility.timelinePlotting.TimelineVisualizer;
import org.metacsp.framework.Constraint;
import org.metacsp.framework.ConstraintNetwork;

import simpleSTNPlanner.SimpleSTNDomain.markings;

public class TestSimpleSTNPlanner {	
	
	public static void main(String[] args) {

		MetaCSPLogging.setLevel(TimelinePublisher.class, Level.FINEST);

		SimpleSTNPlanner planner = new SimpleSTNPlanner(0,600,0);		
		// This is a pointer toward the ActivityNetwork solver of the Scheduler
		ActivityNetworkSolver groundSolver = (ActivityNetworkSolver)planner.getConstraintSolvers()[0];

		MetaCSPLogging.setLevel(planner.getClass(), Level.FINEST);

				
		SimpleSTNDomain rd = new SimpleSTNDomain("TestDomain");
			
		// Here I create two AllenIntervalConstraint for use in the operator I will define
		AllenIntervalConstraint durationMoveTo = new AllenIntervalConstraint(AllenIntervalConstraint.Type.Duration, new Bounds(5,APSPSolver.INF));
		//AllenIntervalConstraint moveToDuringLocalization = new AllenIntervalConstraint(AllenIntervalConstraint.Type.Overlaps, AllenIntervalConstraint.Type.Overlaps.getDefaultBounds());
		
		AllenIntervalConstraint moveFromTo = new AllenIntervalConstraint(AllenIntervalConstraint.Type.Meets, AllenIntervalConstraint.Type.Meets.getDefaultBounds());
		AllenIntervalConstraint meets = new AllenIntervalConstraint(AllenIntervalConstraint.Type.Meets, AllenIntervalConstraint.Type.Meets.getDefaultBounds());
		AllenIntervalConstraint during = new AllenIntervalConstraint(AllenIntervalConstraint.Type.During, AllenIntervalConstraint.Type.During.getDefaultBounds());
//		AllenIntervalConstraint mioi = new AllenIntervalConstraint(AllenIntervalConstraint.Type.MetByOrOverlappedBy, AllenIntervalConstraint.Type.MetByOrOverlappedBy.getDefaultBounds());
		AllenIntervalConstraint mob = new AllenIntervalConstraint(AllenIntervalConstraint.Type.MeetsOrOverlapsOrBefore, AllenIntervalConstraint.Type.MeetsOrOverlapsOrBefore.getDefaultBounds());
		
		SimpleSTNMethod methodGetObject = new SimpleSTNMethod("Robot1::GetObject(Mug1)",
				null, null,
//				new AllenIntervalConstraint[] {mob, mob}, // TODO should be mo only
//				new String[] {"Robot1::At(Table1)", "Mug1::On(Counter1)"},
				new String[] {"Robot1::MoveTo(Table1, Counter1)", "Robot1::GraspObject(RightArm1, Mug1)"});
		// We can add constraints to the operator even after it has been created
		// this is useful for adding unary constraints on the head (which has index 0)
		//methodGetObject.addConstraint(durationMoveTo, 0, 0);
		rd.addMethod(methodGetObject);
		
		
		// New operator: the first parameter is the name, the second are the constraints,
		// the third are requirement activities, fourth means
		// no usage of resources
		SimpleSTNOperator operatorMove1 = new SimpleSTNOperator("Robot1::MoveTo(Table1, Counter1)", 
				new AllenIntervalConstraint[] {moveFromTo},
				new String[] {"Robot1::At(Table1)"},
				null,
				new String[] {"Robot1::At(Table1)"},
				new AllenIntervalConstraint[] {moveFromTo},
				new String[] {"Robot1::At(Counter1)"});
		// We can add constraints to the operator even after it has been created
		// this is useful for adding unary constraints on the head (which has index 0)
		operatorMove1.addConstraint(durationMoveTo, 0, 0);
		rd.addOperator(operatorMove1);
		
//		SimpleSTNOperator operatorMove1a = new SimpleSTNOperator("Robot1::MoveTo(Table1, Counter1)", 
//				new AllenIntervalConstraint[] {moveFromTo},
//				new String[] {"Robot1::At(Table1)"},
//				null,
//				new String[] {"Robot1::At(Table1)"},
//				new AllenIntervalConstraint[] {moveFromTo},
//				new String[] {"Robot1::At(Counter1)"});
//		// We can add constraints to the operator even after it has been created
//		// this is useful for adding unary constraints on the head (which has index 0)
//		operatorMove1a.addConstraint(durationMoveTo, 0, 0);
		//rd.addOperator(operatorMove1a);

		// We give robot 2 the same capability...
//		SimpleSTNOperator operatorMove2 = new SimpleSTNOperator("Robot2::MoveTo()", 
//				null, null,
//				null, null,
//				new AllenIntervalConstraint[] {moveToDuringLocalization},
//				new String[] {"LocalizationService::Localization()"});
//		operatorMove2.addConstraint((AllenIntervalConstraint)durationMoveTo, 0, 0);
//		rd.addOperator(operatorMove2);
		
		SimpleSTNOperator operatorGrasp = new SimpleSTNOperator("Robot1::GraspObject(RightArm1, Mug1)", 
				new AllenIntervalConstraint[] {during, meets},
				new String[] {"Robot1::At(Counter1)", "Mug1::On(Counter1)"},
				null,
				new String[] {"Mug1::On(Counter1)"},
				new AllenIntervalConstraint[] {meets},
				new String[] {"Robot1::Holding(Mug1)"});
		rd.addOperator(operatorGrasp);


		//This adds the domain as a meta-constraint of the SimplePlanner
		planner.addMetaConstraint(rd);
	//		//... and we also add all its resources as separate meta-constraints
	//		for (Schedulable sch : rd.getSchedulingMetaConstraints()) planner.addMetaConstraint(sch);
		
		// INITIAL STATE
		Activity at = (Activity)groundSolver.createVariable("Robot1");
		at.setSymbolicDomain("At(Table1)");
		at.setMarking(markings.ACTIVE);
		Activity on = (Activity)groundSolver.createVariable("Mug1");
		on.setSymbolicDomain("On(Counter1)");
		on.setMarking(markings.ACTIVE);
		
		// INITIAL AND GOAL STATE DEFS
//		Activity moveActivity = (Activity)groundSolver.createVariable("Robot1");
//		moveActivity.setSymbolicDomain("MoveTo(Table1, Counter1)");
//		// ... this is a goal (i.e., an activity to justify through the meta-constraint)
//		moveActivity.setMarking(markings.NEW);
//		//.. let's also give it a minimum duration
//		AllenIntervalConstraint durationMove = new AllenIntervalConstraint(AllenIntervalConstraint.Type.Duration, new Bounds(7,APSPSolver.INF));
//		durationMove.setFrom(moveActivity);
//		durationMove.setTo(moveActivity);
//		
//		Activity graspActivity = (Activity)groundSolver.createVariable("Robot1");
//		graspActivity.setSymbolicDomain("GraspObject(RightArm1, Mug1)");
//		// ... this is a goal (i.e., an activity to justify through the meta-constraint)
//		graspActivity.setMarking(markings.NEW);
//		//.. let's also give it a minimum duration
//		AllenIntervalConstraint durationGrasp = new AllenIntervalConstraint(AllenIntervalConstraint.Type.Duration, new Bounds(7,APSPSolver.INF));
//		durationGrasp.setFrom(graspActivity);
//		durationGrasp.setTo(graspActivity);

//		Activity two = (Activity)groundSolver.createVariable("Robot2");
//		two.setSymbolicDomain("MoveTo()");
//		two.setMarking(markings.NEW);
//		AllenIntervalConstraint durationTwo = new AllenIntervalConstraint(AllenIntervalConstraint.Type.Duration, new Bounds(7,APSPSolver.INF));
//		durationTwo.setFrom(two);
//		durationTwo.setTo(two);
		
		Activity getActivity = (Activity)groundSolver.createVariable("Robot1");
		getActivity.setSymbolicDomain("GetObject(Mug1)");
		// ... this is a goal (i.e., an activity to justify through the meta-constraint)
		getActivity.setMarking(markings.NEW);
		//.. let's also give it a minimum duration
		AllenIntervalConstraint durationGet = new AllenIntervalConstraint(AllenIntervalConstraint.Type.Duration, new Bounds(7,APSPSolver.INF));
		durationGet.setFrom(getActivity);
		durationGet.setTo(getActivity);
		

		groundSolver.addConstraints(new Constraint[] {durationGet}); //{durationGrasp, durationMove});
		
		// We can also specify that goals should be related in time somehow...		
	//		AllenIntervalConstraint after = new AllenIntervalConstraint(AllenIntervalConstraint.Type.After, AllenIntervalConstraint.Type.After.getDefaultBounds());
	//		after.setFrom(two);
	//		after.setTo(one);
	//		groundSolver.addConstraint(after);

		TimelinePublisher tp = new TimelinePublisher(groundSolver, new Bounds(0,25), "Robot1", "Robot2", "Mug1", "RFIDReader1", "LaserScanner1");
		//TimelinePublisher can also be instantiated w/o bounds, in which case the bounds are calculated every time publish is called
		//	TimelinePublisher tp = new TimelinePublisher(groundSolver, "Robot1", "Robot2", "LocalizationService", "RFIDReader1", "LaserScanner1");
		TimelineVisualizer viz = new TimelineVisualizer(tp);
		tp.publish(false, false);
		//the following call is marked as "skippable" and will most likely be skipped because the previous call has not finished rendering...
		tp.publish(false, true);
		
		planner.backtrack();
		
		ConstraintNetwork.draw(groundSolver.getConstraintNetwork(), "Constraint Network");
		
		planner.draw();
		tp.publish(true, false);
	}
	
	

}
