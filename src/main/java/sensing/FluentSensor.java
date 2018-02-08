package sensing;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.logging.Logger;

import htn.HTNMetaConstraint;
import org.metacsp.framework.ConstraintNetwork;
import org.metacsp.framework.Variable;
import org.metacsp.multi.activity.Activity;
import org.metacsp.multi.allenInterval.AllenIntervalConstraint;
import org.metacsp.sensing.NetworkMaintenanceError;
import org.metacsp.time.Bounds;
import org.metacsp.utility.logging.MetaCSPLogging;

import fluentSolver.Fluent;
import fluentSolver.FluentNetworkSolver;

public class FluentSensor implements Serializable {

	private static final long serialVersionUID = -204156975991304711L;
	protected FluentNetworkSolver fns = null;
	private ConstraintNetwork cn = null;
	protected String name;
	private Fluent future = null;
	private Activity currentAct = null;
	private AllenIntervalConstraint currentMeetsFuture = null;
	protected FluentConstraintNetworkAnimator animator = null;
	
	private transient Logger logger = MetaCSPLogging.getLogger(this.getClass());

	public FluentSensor(String name, FluentConstraintNetworkAnimator animator) {
		this.animator = animator;
		this.fns = animator.getFluentNetworkSolver();
		this.cn = animator.getConstraintNetwork();
		this.name = name;
		for (Variable timeFluent : cn.getVariables("Time")) {
			if (((Fluent)timeFluent).getCompoundSymbolicVariable().getPredicateName().equals(FluentConstraintNetworkAnimator.FUTURE_STR)) {
				future = (Fluent)timeFluent;
			}
		}
	}
	
	public String getName() { return this.name; }
	
	/**
	 * Updates the sensor value in the network if it has changed.
	 * 
	 * Checks if the sensor value has changed. If yes it set the deadline to the old value 
	 * and creates a fluent for the new one.
	 * 
	 * @param value The new sensor value.
	 * @param timeNow The current time.
	 */
	public void modelSensorValue(String value, long timeNow) {
		synchronized(fns) {
			boolean makeNew = false;
			if (currentAct == null) { makeNew = true; }
			else if (currentAct != null) {
				//If it has not changed, do nothing - otherwise:
				if (this.hasChanged(value)) {
					//change value
					AllenIntervalConstraint deadline = new AllenIntervalConstraint(AllenIntervalConstraint.Type.Deadline, new Bounds(timeNow,timeNow));
					deadline.setFrom(currentAct.getVariable());
					deadline.setTo(currentAct.getVariable());
					fns.removeConstraint(currentMeetsFuture);
					boolean ret = fns.addConstraint(deadline);
					currentAct.getVariable().setMarking(HTNMetaConstraint.markings.CLOSED);
					if (!ret) throw new NetworkMaintenanceError(deadline);
					//if (!ret) throw new NetworkMaintenanceError(future.getTemporalVariable().getEST(),timeNow);
					makeNew = true;
				}
			}
			//First reading or value changed --> make new activity
			if (makeNew) {
				Activity act = this.createNewActivity(value);
				AllenIntervalConstraint rel = new AllenIntervalConstraint(AllenIntervalConstraint.Type.Release, new Bounds(timeNow,timeNow));
				rel.setFrom(act.getVariable());
				rel.setTo(act.getVariable());
				AllenIntervalConstraint meetsFuture = new AllenIntervalConstraint(AllenIntervalConstraint.Type.Meets);
				meetsFuture.setFrom(act.getVariable());
				meetsFuture.setTo(future);
				currentAct = act;
				currentMeetsFuture = meetsFuture;
				boolean ret = fns.addConstraints(rel,meetsFuture);
				//if (!ret) throw new NetworkMaintenanceError(rel,meetsFuture);
				if (!ret) throw new NetworkMaintenanceError(future.getTemporalVariable().getEST(),timeNow);
				logger.info("" + currentAct);
			}
		}
	}
	
	protected static String parseName(String everything) {
		String ret = everything.substring(everything.indexOf("Sensor")+6);
		ret = ret.substring(0,ret.indexOf(")")).trim();
		return ret;
	}
	
	protected static HashMap<Long,String> parseSensorValue(String everything, long delta) {
		HashMap<Long,String> ret = new HashMap<Long,String>();
		int lastSV = everything.lastIndexOf("SensorValue");
		while (lastSV != -1) {
			int bw = lastSV;
			int fw = lastSV;
			while (everything.charAt(--bw) != '(') { }
			int parcounter = 1;
			while (parcounter != 0) {
				if (everything.charAt(fw) == '(') parcounter++;
				else if (everything.charAt(fw) == ')') parcounter--;
				fw++;
			}
			String element = everything.substring(bw,fw);
			String value = element.substring(element.indexOf("SensorValue")+11).trim();
			long time = Long.parseLong(value.substring(value.lastIndexOf(" "),value.lastIndexOf(")")).trim());
			time += delta;
			value = value.substring(0,value.lastIndexOf(" ")).trim();
			ret.put(time,value);
			everything = everything.substring(0,bw);
			lastSV = everything.lastIndexOf("SensorValue");
		}
		return ret;
	}

	public void postSensorValue(String sensorValue, long time) {
		animator.postSensorValueToDispatch(this, time, sensorValue);
	}

	public void registerSensorTrace(String sensorTraceFile) {
		this.registerSensorTrace(sensorTraceFile, 0);
	}
	
	protected boolean hasChanged(String value) {
		return (!((Fluent)currentAct.getVariable()).getCompoundSymbolicVariable().getName().equals(value));
	}
	
	protected Activity createNewActivity(String value) {
		Fluent act = (Fluent)fns.createVariable(this.name);
		act.setName(value);
		act.setMarking(HTNMetaConstraint.markings.OPEN);
		return act;
	}
	
	public void registerSensorTrace(String sensorTraceFile, long delta) {
		String everything = null;
		try {
			BufferedReader br = new BufferedReader(new FileReader(sensorTraceFile));
			try {
				StringBuilder sb = new StringBuilder();
				String line = br.readLine();
				while (line != null) {
					if (!line.startsWith("#")) {
						sb.append(line);
						sb.append('\n');
					}
					line = br.readLine();
				}
				everything = sb.toString();
				String name = parseName(everything);
				if (name.equals(this.name)) {
					HashMap<Long,String> sensorValues = parseSensorValue(everything,delta);
					animator.registerSensorValuesToDispatch(this, sensorValues);
				}
			}
			finally { br.close(); }
		}
		catch (FileNotFoundException e) { e.printStackTrace(); }
		catch (IOException e) { e.printStackTrace(); }
	}

}
