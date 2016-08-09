package controller;

import model.Vector;

public interface ForceProvider {

	public Vector getForce(Moving moving);

}
