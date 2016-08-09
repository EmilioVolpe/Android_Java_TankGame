package projectiles;

import java.awt.Color;
import java.awt.Shape;

import view.Explosion;
import model.Vector;
import model.World;

public class ExplosiveProjectile extends Projectile {

	protected double radiusMultiplier;

	public ExplosiveProjectile(Vector position, Vector velocity, double mass) {
		this(position, velocity, mass, 32.5D);
	}

	protected ExplosiveProjectile(Vector position, Vector velocity,
			double mass, double radiusMultiplier) {
		super(position, velocity, mass);
		this.radiusMultiplier = radiusMultiplier;
	}

	@Override
	public void destroy() {
		World.getWorld().addObject(
				new Explosion(getPosition(), getMass() * getRadiusMultiplier(), 0.75D,
						new Color(0xFF8000)));
	}
	
	protected double getRadiusMultiplier(){
		return radiusMultiplier * getVelocity().getNorm() * 4E-3D;
	}

	@Override
	public double getMagnetMultiplier() {
		return 1D;
	}

	@Override
	public void onEnterTerrain(Shape outsideShape, Shape insideShape, Vector outsidePosition, Vector insidePosition) {
		destroyAndKill();
	}

}
