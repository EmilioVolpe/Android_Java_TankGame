package controller;

import model.Constants;
import model.Vector;

public class GravitationalForceProvider implements ForceProvider {

	public GravitationalForceProvider() {

	}

	@Override
	public Vector getForce(Moving moving) {
		return new Vector(0, Constants.G * moving.getMass());
	}

}
