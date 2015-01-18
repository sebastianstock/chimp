package dispatching;

import java.util.HashMap;

import org.metacsp.framework.Constraint;
import org.metacsp.framework.ConstraintNetwork;
import org.metacsp.framework.Variable;
import org.metacsp.multi.allenInterval.AllenIntervalConstraint;
import org.metacsp.time.Bounds;

import fluentSolver.Fluent;
import fluentSolver.FluentNetworkSolver;

public class FluentDispatcher extends Thread {

	public static enum ACTIVITY_STATE {PLANNED, STARTED, FINISHING, FINISHED, SKIP_BECAUSE_UNIFICATION};
	private ConstraintNetwork cn;
	private FluentNetworkSolver fns;
	private long period;
	private HashMap<Fluent,ACTIVITY_STATE> acts;
	private HashMap<Fluent,AllenIntervalConstraint> overlapFutureConstraints;
	private HashMap<String,FluentDispatchingFunction> dfs;
	private Fluent future;

	public FluentDispatcher(FluentNetworkSolver fns, long period, Fluent future) {
		this.fns = fns;
		cn = fns.getConstraintNetwork();
		this.period = period;
		acts = new HashMap<Fluent, ACTIVITY_STATE>();
		overlapFutureConstraints = new HashMap<Fluent, AllenIntervalConstraint>();
		dfs = new HashMap<String, FluentDispatchingFunction>();
		this.future = future;
	}

	public void run() {
		while (true) {
			try { Thread.sleep(period); }
			catch (InterruptedException e) { e.printStackTrace(); }

			synchronized(fns) {
				for (String component : dfs.keySet()) {
					// go through all activity fluents
					for (Variable var : cn.getVariables(component)) {
						if (var instanceof Fluent) {
							Fluent act = (Fluent)var;
							if (dfs.get(component).skip(act)) continue;
							
							//New act, tag as not dispatched
							if (!acts.containsKey(act)) {
								boolean skip = false;
								//... but test if activity is a unification - if so, ignore it!
								Constraint[] outgoing = fns.getConstraintNetwork().getOutgoingEdges(act);
								for (Constraint con : outgoing) {
									if (con instanceof AllenIntervalConstraint) {
										AllenIntervalConstraint aic = (AllenIntervalConstraint)con;
										Fluent to = (Fluent)aic.getTo();
										if (to.getComponent().equals(act.getComponent()) && 
												to.getCompoundSymbolicVariable().getName().equals(act.getCompoundSymbolicVariable().getName()) && 
												aic.getTypes()[0].equals(AllenIntervalConstraint.Type.Equals)) {
											skip = true;
											System.out.println("IGNORED UNIFICATION " + aic);
											break;
										}
									}
								}
								if (!skip) acts.put(act, ACTIVITY_STATE.PLANNED);
								else acts.put(act, ACTIVITY_STATE.SKIP_BECAUSE_UNIFICATION);
							}

							//Not dispatched, check if need to dispatch
							if (acts.get(act).equals(ACTIVITY_STATE.PLANNED)) {
								//time to dispatch, do it!
								if (act.getTemporalVariable().getEST() < future.getTemporalVariable().getEST()) {
									acts.put(act, ACTIVITY_STATE.STARTED);
									AllenIntervalConstraint overlapsFuture = new AllenIntervalConstraint(AllenIntervalConstraint.Type.Overlaps);
									overlapsFuture.setFrom(act);
									overlapsFuture.setTo(future);
									boolean ret = fns.addConstraint(overlapsFuture);
									if(!ret){
										System.out.println("IGNORED: " + act);
//										System.out.println("  CONSTRAINTS IN: " + Arrays.toString(fns.getConstraintNetwork().getIncidentEdges(act)));
//										System.out.println("  CONSTRAINTS OUT: " + Arrays.toString(fns.getConstraintNetwork().getOutgoingEdges(act)));
//											CopyOfTestProblemParsing.extractPlan(fns);
									}
									else {
										overlapFutureConstraints.put(act, overlapsFuture);
										this.dfs.get(component).dispatch(act);
									}

								}
							}

							//If finished, tag as finished
							else if (acts.get(act).equals(ACTIVITY_STATE.FINISHING)) {
								acts.put(act, ACTIVITY_STATE.FINISHED);
								fns.removeConstraint(overlapFutureConstraints.get(act));
								AllenIntervalConstraint deadline = new AllenIntervalConstraint(AllenIntervalConstraint.Type.Deadline, new Bounds(future.getTemporalVariable().getEST(),future.getTemporalVariable().getEST()));
								deadline.setFrom(act);
								deadline.setTo(act);
								if (!fns.addConstraint(deadline)) {
									AllenIntervalConstraint defaultDeadline = new AllenIntervalConstraint(AllenIntervalConstraint.Type.Deadline, new Bounds(act.getTemporalVariable().getEET(),act.getTemporalVariable().getEET()));
									defaultDeadline.setFrom(act);
									defaultDeadline.setTo(act);
									fns.addConstraint(defaultDeadline);
									//System.out.println("++++++++++++++++++++ SHIT: " + act + " DAEDLINE AT " + future.getTemporalVariable().getEST());
								}
							}							
						}
					}
				}
			}
		}
	}

	public void addDispatchingFunction(String component, FluentDispatchingFunction df) {
		df.registerDispatcher(this);
		this.dfs.put(component, df);
	}

	public void finish(Fluent act) { acts.put(act, ACTIVITY_STATE.FINISHING); }
	
	public ConstraintNetwork getConstraintNetwork() {
		return fns.getConstraintNetwork();
	}

	
}
