package projectiles;

import java.awt.Shape;

import view.Terrain;
import model.Vector;

public class TimedExplosiveProjectile extends ExplosiveProjectile {

	private double time = 0;

	public TimedExplosiveProjectile(Vector position, Vector velocity,
			double mass) {
		this(position, velocity, mass, 32.5D);
	}
	
	protected TimedExplosiveProjectile(Vector position, Vector velocity, double mass, double radiusMultiplier){
		super(position, velocity, mass, radiusMultiplier);
		if (Terrain.getTerrain().doesTerrainExistAt(position)) {
			setFrozen(true);
		}
	}

	@Override
	public void advance(double timestep) {
		time += timestep;
		if (time >= 5D) {
			destroyAndKill();
		}
	}

	@Override
	public double getRadiusMultiplier(){
		return radiusMultiplier;
	}
	
	@Override
	public boolean isBlocking(){
		return true;
	}
	
	@Override
	public void onEnterTerrain(Shape outsideShape, Shape insideShape, Vector outsidePosition, Vector insidePosition) {
		if (getVelocity().getNormSquared() <= 1D){
			setFrozen(true);
			return;
		}
		Vector impactLocation = Terrain.getTerrain().getInnerImpactLocation(outsideShape, insideShape, outsidePosition, insidePosition); 
		setPosition(impactLocation);
		Vector normal = Terrain.getTerrain().getSurfaceNormalVector(impactLocation, 10);
		Vector normalComponent = getVelocity().getComponent(normal);
		setVelocity(getVelocity().subtract(normalComponent.multiply(2.0D)).multiply(0.625D));
	}

}
