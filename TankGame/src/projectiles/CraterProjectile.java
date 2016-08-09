package projectiles;

import java.awt.Shape;

import view.Terrain;
import model.Vector;

public class CraterProjectile extends Projectile {

	public CraterProjectile(Vector position, Vector velocity, double mass) {
		super(position, velocity, mass);
	}

	@Override
	public void destroy() {
		Terrain.getTerrain().setTerrainAroundRadius(getPosition(), getMass() * 80D, false);
	}

	@Override
	public double getMagnetMultiplier() {
		return 0;
	}

	@Override
	public void onEnterTerrain(Shape outsideShape, Shape insideShape,
			Vector outsidePosition, Vector insidePosition) {
		destroyAndKill();
	}

}
