package sensing;

import java.util.Calendar;
import java.util.HashMap;
import java.util.logging.Logger;

import htn.HTNMetaConstraint;
import org.metacsp.framework.ConstraintNetwork;
import org.metacsp.multi.allenInterval.AllenIntervalConstraint;
import org.metacsp.sensing.Controllable;
import org.metacsp.sensing.InferenceCallback;
import org.metacsp.sensing.NetworkMaintenanceError;
import org.metacsp.time.Bounds;
import org.metacsp.utility.logging.MetaCSPLogging;

import dispatching.FluentDispatcher;
import dispatching.FluentDispatchingFunction;
import fluentSolver.Fluent;
import fluentSolver.FluentNetworkSolver;

public class FluentConstraintNetworkAnimator extends Thread {

	private ConstraintNetwork cn = null;
	private FluentNetworkSolver fns = null;
	private Fluent future = null;
	private long originOfTime;
	private long firstTick;
	private long period;
	private AllenIntervalConstraint currentReleaseFuture = null;
	private HashMap<FluentSensor,HashMap<Long,String>> sensorValues = new HashMap<FluentSensor, HashMap<Long,String>>();
	private InferenceCallback cb = null;
	private FluentDispatcher dis = null;
	private boolean paused = false;
	
	public static final String FUTURE_STR = "Future";
	private static final String[] NO_STRINGS = {};

	private HashMap<Controllable,HashMap<Long,String>> controllableValues = new HashMap<Controllable, HashMap<Long,String>>();

	private transient Logger logger = MetaCSPLogging.getLogger(FluentConstraintNetworkAnimator.class);

	protected long getCurrentTimeInMillis() {
		return Calendar.getInstance().getTimeInMillis();
	}

	public FluentConstraintNetworkAnimator(FluentNetworkSolver fns, long period, InferenceCallback cb, boolean startPaused) {
		this(fns, period, startPaused);
		this.cb = cb;
	}

	public FluentConstraintNetworkAnimator(FluentNetworkSolver fns, long period, InferenceCallback cb) {
		this(fns, period);
		this.cb = cb;
	}

	public FluentConstraintNetworkAnimator(FluentNetworkSolver fns, long period) {
		this(fns,period,false);
	}

	public FluentConstraintNetworkAnimator(FluentNetworkSolver fns, long period, boolean startPaused) {
		this.paused = startPaused;
		synchronized(fns) {
			this.fns = fns;
			this.period = period;
			originOfTime = fns.getOrigin();
			firstTick = getCurrentTimeInMillis();
			this.cn = fns.getConstraintNetwork();
			// Create variable representing future
			future = (Fluent)fns.createVariable("Time");
			future.setName(FUTURE_STR, NO_STRINGS);
			future.setMarking(HTNMetaConstraint.markings.JUSTIFIED);
			long timeNow = getTimeNow();
			AllenIntervalConstraint releaseFuture = new AllenIntervalConstraint(
					AllenIntervalConstraint.Type.Release, new Bounds(timeNow, timeNow));
			releaseFuture.setFrom(future);
			releaseFuture.setTo(future);
			AllenIntervalConstraint deadlineFuture = new AllenIntervalConstraint(
					AllenIntervalConstraint.Type.Deadline, new Bounds(fns.getHorizon(), fns.getHorizon()));
			deadlineFuture.setFrom(future);
			deadlineFuture.setTo(future);
			currentReleaseFuture = releaseFuture;
			if (!fns.addConstraints(currentReleaseFuture,deadlineFuture)) {
				throw new NetworkMaintenanceError(currentReleaseFuture,deadlineFuture);
			}
			this.start();
		}
	}

	public ConstraintNetwork getConstraintNetwork() { return this.cn; }

	public FluentNetworkSolver getFluentNetworkSolver() { return this.fns; }

	public void postSensorValueToDispatch(FluentSensor sensor, long time, String value) {
		synchronized(fns) {
			if (!this.sensorValues.keySet().contains(sensor))
				this.sensorValues.put(sensor, new HashMap<Long, String>());
			HashMap<Long, String> sensorVal = this.sensorValues.get(sensor);
			sensorVal.put(time, value);
		}
	}

	// TODO not used at the moment. do we need this?
	public void postControllableValueToDispatch(Controllable controllable, long time, String value) {
		synchronized(fns) {
			if (!this.controllableValues.keySet().contains(controllable))
				this.controllableValues.put(controllable, new HashMap<Long, String>());
			HashMap<Long, String> contrVal = this.controllableValues.get(controllable);
			contrVal.put(time, value);
		}
	}

	public void registerSensorValuesToDispatch(FluentSensor sensor, HashMap<Long,String> values) {
		this.sensorValues.put(sensor, values);
	}

	public void registerControllableValuesToDispatch(Controllable controllable, 
			HashMap<Long,String> values) {
		this.controllableValues.put(controllable, values);
	}

	public void addDispatchingFunctions(FluentNetworkSolver fns, FluentDispatchingFunction ... dfs) {
		boolean start = false;
		if (this.dis == null) {
			this.dis = new FluentDispatcher(fns, period, future);
			start = true;
		}
		for (FluentDispatchingFunction df : dfs) dis.addDispatchingFunction(df.getComponent(), df);
		if (start) dis.start();
	}

	public long getTimeNow() {
		return getCurrentTimeInMillis()-firstTick+originOfTime;
	}
	
	public void setPaused(boolean paused) {
		this.paused = paused;
	}

	/**
	 *  * Updates the Future variable by removing the old release
   *    constraint and creating a new one with the current time.
   *  * Animates the sensor traces.
   *  * Calls doInference() of the inferenceCallback
	 */
	@Override
	public void run() {
		int iteration = 0;
		while (! isInterrupted()) {
			
			long startSleep = getTimeNow();
			while (getTimeNow()-startSleep < period) {
				try { Thread.sleep(10); }
				catch (InterruptedException e) { 
					interrupt();
				}
			}

			if (!paused) {
				synchronized(fns) {
//					//Update release constraint of Future
					long timeNow = getTimeNow();
					AllenIntervalConstraint releaseFuture = new AllenIntervalConstraint(
							AllenIntervalConstraint.Type.Release, new Bounds(timeNow, timeNow));
					releaseFuture.setFrom(future);
					releaseFuture.setTo(future);
					if (currentReleaseFuture != null) fns.removeConstraint(currentReleaseFuture);
					if (!fns.addConstraint(releaseFuture)) {
						throw new NetworkMaintenanceError(releaseFuture);
					}
					currentReleaseFuture = releaseFuture;

//					//If there are registered sensor traces, animate them too
//					for (FluentSensor sensor : sensorValues.keySet()) {
//						Vector<Long> toRemove = new Vector<Long>();
//						HashMap<Long,String> values = sensorValues.get(sensor);
//						for (long time : values.keySet()) {
//							if (time <= timeNow) {
//								sensor.modelSensorValue(values.get(time), time);
//								toRemove.add(time);
//							}
//						}
//						for (long time : toRemove) values.remove(time);
//					}

//					//If there is a registered InferenceCallback (e.g., call a planner), run it
//					if (this.cb != null) cb.doInference(timeNow);

					//Print iteration number
					logger.info("Iteration " + iteration++ + " @" + timeNow);
				}
			}
		}
		logger.info("finished");
	}
	
	public Fluent getFuture() {
		return future;
	}
	
	public FluentDispatcher getDispatcher() {
		return dis;
	}

}
