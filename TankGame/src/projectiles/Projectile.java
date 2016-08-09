package projectiles;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;

import view.Explosion;
import view.Sprite;
import controller.Advanceable;
import controller.Blocker;
import controller.Moving;
import model.Renderable;
import model.Vector;

public abstract class Projectile extends Moving implements Renderable,
		Advanceable, Blocker {

	protected Projectile(Vector position, Vector velocity, double mass) {
		super(position, velocity, mass);
	}
	
	@Override
	public void advance(double timestep){
		
	}

	@Override
	public Shape getBoundingShape(){
		double radius = getRadius();
		return new Ellipse2D.Double(position.getX() - radius, position.getY() - radius, radius * 2D, radius * 2D);
	}
	
	public Color getColor() {
		return new Color(0x00A000);
	}

	public double getRadius() {
		return 5D * getMass();
	}

	@Override
	public int getRenderLayer() {
		return 500;
	}

	@Override
	public boolean isBlocking() {
		return !isFrozen();
	}

	@Override
	protected void onImpactWithExplosion(Explosion explosion) {
		destroyAndKill();
	}

	@Override
	protected void onImpactWithProjectile(Projectile projectile) {
		destroyAndKill();
	}

	@Override
	protected void onImpactWithTank(Sprite tank) {
		destroyAndKill();
	}
	
	@Override
	public void onMoveOffScreen(){
		
	}
	
	@Override
	public void render(Graphics2D g2) {
		g2.setColor(getColor());
		g2.fill(getBoundingShape());
	}
	
}
