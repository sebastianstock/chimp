package dispatching;

import fluentSolver.Fluent;
import fluentSolver.FluentNetworkSolver;
import org.metacsp.framework.Constraint;
import org.metacsp.framework.ConstraintNetwork;
import org.metacsp.framework.Variable;
import org.metacsp.multi.allenInterval.AllenIntervalConstraint;
import org.metacsp.time.Bounds;
import org.metacsp.utility.logging.MetaCSPLogging;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.logging.Logger;

public class FluentDispatcher extends Thread {

	public enum ACTIVITY_STATE {PLANNED, STARTED, FINISHING, FINISHED, SKIP_BECAUSE_UNIFICATION, FAILED}

    private ConstraintNetwork cn;
	private FluentNetworkSolver fns;
	private long period;
	private HashMap<Fluent,ACTIVITY_STATE> acts;
	private HashMap<Fluent,AllenIntervalConstraint> overlapFutureConstraints;
	private HashMap<String,FluentDispatchingFunction> dfs;
	private Fluent future;
	private transient Logger logger = MetaCSPLogging.getLogger(this.getClass());

	public FluentDispatcher(FluentNetworkSolver fns, long period, Fluent future) {
		this.fns = fns;
		cn = fns.getConstraintNetwork();
		this.period = period;
		acts = new HashMap<Fluent, ACTIVITY_STATE>();
		overlapFutureConstraints = new HashMap<Fluent, AllenIntervalConstraint>();
		dfs = new HashMap<String, FluentDispatchingFunction>();
		this.future = future;
	}

	@Override
	public void run() {
		while (! isInterrupted()) {
			try { Thread.sleep(period); }  // TODO put this to the end to prevent sleeping right at the beginning?
			catch (InterruptedException e) {
				interrupt();
			}

			synchronized(fns) {
				for (String component : dfs.keySet()) {
					// go through all activity fluents
					Variable[] vars = cn.getVariables(component);
					Arrays.sort(vars, new Comparator<Variable>() {

						@Override
						public int compare(Variable o1, Variable o2) {
							if (o1 instanceof Fluent && o2 instanceof Fluent) {
								long o1_est = ((Fluent) o1).getAllenInterval().getEST();
								long o2_est = ((Fluent) o2).getAllenInterval().getEST();
								int c = Long.compare(o1_est, o2_est);
								if (c == 0) {
									c = o1.compareTo(o2);
								}
								return c;
							} else {
								return o1.compareTo(o2);
							}
						}
					});
					
					for (Variable var : vars) {
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
											logger.info("IGNORED UNIFICATION " + aic);
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
										logger.info("IGNORED: " + act);
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
			synchronized(this) {
				if (this.isFinished()) {
					logger.info("dispatching finished: notifying all waiting threads");
					this.notifyAll();
				}
				if (this.failed()) {
					logger.info("dispatching failed: notifying all waiting threads");
					this.notifyAll();
				}
			}
		}
	}
	
	/**
	 * Check if all activities have been finished.
	 * @return true if all activities have been finished, i.e., no started or planned activites exist, otherwise false
	 */
	public boolean isFinished() {
		for (ACTIVITY_STATE state : acts.values()) {
			if (state.equals(ACTIVITY_STATE.PLANNED) || state.equals(ACTIVITY_STATE.STARTED) || state.equals(ACTIVITY_STATE.FINISHING)) {
				return false;
			}
		}
		logger.fine("Acts.size: " + acts.size());
		return true;
	}
	
	/**
	 * Check if any activity has failed.
	 * @return true if any activity has failed, otherwise false
	 */
	public boolean failed() {
		for (ACTIVITY_STATE state : acts.values()) {
			if (state.equals(ACTIVITY_STATE.FAILED)) {
				return true;
			}
		}
		return false;
	}

	public void addDispatchingFunction(String component, FluentDispatchingFunction df) {
		df.registerDispatcher(this);
		this.dfs.put(component, df);
	}

	public void finish(Fluent act) { acts.put(act, ACTIVITY_STATE.FINISHING); }
	
	public void fail(Fluent act) {acts.put(act, ACTIVITY_STATE.FAILED); }
	
	public ConstraintNetwork getConstraintNetwork() {
		return fns.getConstraintNetwork();
	}

	
}
