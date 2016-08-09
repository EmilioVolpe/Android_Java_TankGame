package view;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;

import controller.Advanceable;
import controller.Blocker;
import controller.ForceProvider;
import controller.Moving;
import controller.Positioned;
import model.RenderHelper;
import model.Renderable;
import model.Vector;

public class Explosion extends Positioned implements Renderable, Advanceable, Blocker, ForceProvider {

	private Color color;
	private double elapsedTime = 0;
	private double maxRadius;
	private double totalTime;

	public Explosion(Vector position, double maxRadius, double totalTime,
			Color color) {
		super(position);
		this.color = color;
		this.position = position;
		this.maxRadius = maxRadius;
		this.totalTime = totalTime;
	}

	@Override
	public void advance(double timestep) {
		elapsedTime += timestep;
		if (elapsedTime > totalTime) {
			setDead();
			Terrain.getTerrain().setTerrainAroundRadius(getPosition(),
					getRadius(), false);
		}
	}

	public double getAdjustedTimeFraction(){
		return Math.pow(getTimeFraction(), 0.75D);
	}

	@Override
	public Shape getBoundingShape() {
		double radius = getRadius();
		return new Ellipse2D.Double(position.getX() - radius, position.getY() - radius, radius * 2D, radius * 2D);
	}

	public Color getColor() {
		return color;
	}
	
	@Override
	public Vector getForce(Moving moving){
		double shockwaveRadius = getShockwaveRadius();
		Vector displacement = moving.getPosition().subtract(getPosition());
		double normSquared = displacement.getNormSquared();
		Vector force = Vector.ZERO;
		if (normSquared > shockwaveRadius * shockwaveRadius){
			double distanceToWave = Math.max(10D, Math.sqrt(normSquared) - shockwaveRadius);
			double relativeStrength = 1E6D / (distanceToWave);
			force = force.add(displacement.setLength(relativeStrength * relativeStrength / shockwaveRadius));
		}
		double radius = getRadius();
		if (normSquared < radius * radius){
			double distanceToExplosion = Math.max(5D, radius - Math.sqrt(normSquared));
			double relativeStrength = (1D - getAdjustedTimeFraction()) * distanceToExplosion * 1E7D;
			force = force.add(displacement.setLength(relativeStrength));
		}
		
		return force;
	}

	public double getMaxRadius() {
		return maxRadius;
	}

	public double getRadius() {
		return getAdjustedTimeFraction() * getMaxRadius();
	}

	@Override
	public int getRenderLayer() {
		return 1000;
	}

	public double getTimeFraction() {
		return elapsedTime / totalTime;
	}

	@Override
	public boolean isBlocking() {
		return true;
	}
	
	public double getShockwaveRadius(){
		return 10D * Math.pow(getTimeFraction(), 1.5D) * getMaxRadius();
	}

	@Override
	public void render(Graphics2D g2) {
		RenderHelper.drawCircle(g2, getPosition(), (int)getRadius(), getColor());
		double sRadius = getShockwaveRadius();
		g2.setColor(new Color(0, 0, 0, (int)(255D * (1D - getAdjustedTimeFraction()))));
		g2.drawOval((int)(getPosition().getX() - sRadius), (int)(getPosition().getY() - sRadius), (int)(sRadius * 2D), (int)(sRadius * 2D));
	}

}
