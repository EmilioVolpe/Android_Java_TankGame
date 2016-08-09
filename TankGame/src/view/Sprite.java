package view;

import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;

import controller.Advanceable;
import controller.Moving;
import projectiles.Projectile;
import model.Constants;
import model.ControlPanel;
import model.RenderHelper;
import model.Renderable;
import model.Vector;
import model.World;

public class Sprite extends Moving implements Renderable, Advanceable {

	private int playerNumber;

	private Color color;
	private double health = 100D;

	private double fireAngle;
	private int firePower = 70;
	private int selectedAmmo = 0;
	private int selectedMass = 1;

	public Sprite(int playerNumber, Vector position) {
		super(position, Vector.ZERO, 1E5D);
		this.playerNumber = playerNumber;
		if (position.getX() < Constants.WIDTH / 2) {
			fireAngle = - Constants.PI / 6D;
		} else {
			fireAngle = - Constants.PI * (5D / 6D);
		}
		if (playerNumber == 0) {
			color = Color.BLUE;
		} else {
			color = Color.RED;
		}
	}

	@Override
	public void advance(double timestep) {
		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				ControlPanel.getControlPanel().getTankHealthBars()[getPlayerNumber()]
						.setValue((int) getHealth());
			}
		});
	}

	public void damageTank(double damage) {
		if (isDead()) {
			return;
		}
		health -= damage;
		if (health <= 0) {
			health = 0;
			destroyAndKill();
		}
	}

	@Override
	protected void destroy() {
		World.getWorld().addObject(
				new Explosion(getPosition(), 150D, 1D, getColor()));
	}

	@Override
	public Shape getBoundingShape() {
		double radius = getRadius();
		return new Ellipse2D.Double(position.getX() - radius, position.getY() - radius, radius * 2D, radius * 2D);
	}

	public Color getColor() {
		return color;
	}

	public double getFireAngle() {
		return fireAngle;
	}

	public int getFirePower() {
		return firePower;
	}

	public double getHealth() {
		return health;
	}

	@Override
	public double getMagnetMultiplier() {
		return 0D;
	}

	public int getPlayerNumber() {
		return playerNumber;
	}

	public double getRadius() {
		return 10D;
	}

	@Override
	public int getRenderLayer() {
		return 0;
	}

	public int getSelectedAmmo() {
		return selectedAmmo;
	}

	public int getSelectedMass() {
		return selectedMass;
	}

	public boolean isBlocking() {
		return false;
	}

	private void move(double deltaX) { // either -1 or 1
		Vector prevPosition = getPosition();
		Shape prevShape = getBoundingShape();
		Vector normal = Terrain.getTerrain().getNormalVectorWith(prevShape);
		if (normal.getY() == 0){
			return;
		}
		normal = normal.multiply(Math.abs(deltaX) / Math.abs(normal.getY()));
		double deltaY = Math.abs(normal.getX());
		if (Math.signum(deltaX) == Math.signum(normal.getX())){
			deltaY = 0;
		}
		if (deltaY > 3){
			return;
		}
		Vector newPosition = getPosition().add(new Vector(deltaX, -deltaY));
		setPosition(newPosition);
		Shape newShape = getBoundingShape();
		Vector finalPosition = Terrain.getTerrain().getInnerImpactLocation(prevShape, newShape, prevPosition, newPosition); 
		setPosition(finalPosition);
	}

	public void moveLeft() {
		if (getPosition().getX() <= 0) {
			return;
		}
		move(-1);
	}

	public void moveRight() {
		if (getPosition().getX() >= Constants.WIDTH - 1) {
			return;
		}
		move(1);
	}
	
	@Override
	public void onEnterTerrain(Shape outsideShape, Shape insideShape, Vector outsidePosition, Vector insidePosition) {
		
	}

	@Override
	protected void onImpactWithExplosion(Explosion ex) {
		damageTank(Constants.TICK_TIME_STEP * 20D);
	}

	@Override
	protected void onImpactWithProjectile(Projectile projectile) {

	}

	@Override
	protected void onImpactWithTank(Sprite tank) {

	}

	@Override
	public void onLeaveTerrain(Vector oldPosition, Vector newPosition) {
		setFrozen(false);
	}

	@Override
	public void onMoveOffScreen() {
		destroy();
	}

	@Override
	public void onTickInTerrain(Shape outsideShape, Shape insideShape, Vector outsidePosition, Vector insidePosition) {
		setVelocity(Vector.ZERO);
		Vector outerLocation = Terrain.getTerrain().getOuterImpactLocation(outsideShape, insideShape, outsidePosition, insidePosition);
		setPosition(outerLocation);
	}

	@Override
	public void render(Graphics2D g2) {
		double radius = getRadius();
		RenderHelper.drawCircle(g2, getPosition(), (int) radius, getColor());
		Vector barrelVector = Vector.polarToCartesian(radius * 2.5D,
				getFireAngle());
		Vector barrelEnd = getPosition().add(barrelVector);
		g2.setColor(RenderHelper.getInverse(getColor()));
		g2.drawLine(getPosition().getIntegerX(), getPosition().getIntegerY(),
				barrelEnd.getIntegerX(), barrelEnd.getIntegerY());
	}

	@Override
	public void setDead() {
		super.setDead();
		ControlPanel.getControlPanel().playerWon(1 - getPlayerNumber());
	}

	public void setFireAngle(double angle) {
		fireAngle = angle;
	}

	public void setFirePower(int firePower) {
		this.firePower = firePower;
	}

	public void setSelectedAmmo(int selectedAmmo) {
		this.selectedAmmo = selectedAmmo;
	}

	public void setSelectedMass(int selectedMass) {
		this.selectedMass = selectedMass;
	}

}
