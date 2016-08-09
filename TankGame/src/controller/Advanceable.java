package controller;

public interface Advanceable {
	/**
	 * Advance the state of the object by timestep seconds. Do not recalculate
	 * the objects kinematics, only calculate specialized behavior.
	 * 
	 * @param timestep
	 *            time to advance, in seconds
	 */
	public void advance(double timestep);
}
