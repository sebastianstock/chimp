package dispatching;

import org.metacsp.framework.ConstraintNetwork;

import fluentSolver.Fluent;

public abstract class FluentDispatchingFunction {
	
	protected String component;
	
	protected FluentDispatcher dis;
	
	public FluentDispatchingFunction(String component) {
		this.component = component;
	}
	
	public void registerDispatcher(FluentDispatcher dis) {
		this.dis = dis;
	}
	
	public String getComponent() { return component; }
	
	public abstract void dispatch(Fluent act);
	
	public abstract boolean skip(Fluent act);
	
	public void finish(Fluent act) {
		dis.finish(act);
	}
	
	public void fail(Fluent act) {
		dis.fail(act);
	}
	
	public ConstraintNetwork getConstraintNetwork() {
		return dis.getConstraintNetwork();
	}
	

}
