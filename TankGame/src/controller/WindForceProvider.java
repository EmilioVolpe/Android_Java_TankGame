package controller;

import model.Tank;
import model.Vector;

public class WindForceProvider implements ForceProvider {

	@Override
	public Vector getForce(Moving moving) {
		return new Vector(Tank.getTanks().getWindStrength(), 0);
	}

}
