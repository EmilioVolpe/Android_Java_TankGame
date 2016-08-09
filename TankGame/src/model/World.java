package model;

import java.awt.Shape;
import java.awt.geom.Area;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import controller.Advanceable;
import controller.Blocker;
import controller.ForceProvider;
import controller.Moving;
import controller.Positioned;
import projectiles.Projectile;
import view.Explosion;
import view.Sprite;
import view.Terrain;

public class World implements Runnable {

	private static final World world = new World();

	public static World getWorld() {
		return world;
	}

	private List<Explosion> explosions = new ArrayList<Explosion>();
	private List<Projectile> projectiles = new ArrayList<Projectile>();
	private List<Sprite> tanks = new ArrayList<Sprite>();
	private List<Advanceable> advanceables = new ArrayList<Advanceable>();
	private List<Positioned> positionedobjects = new ArrayList<Positioned>();
	private List<Moving> movingobjects = new ArrayList<Moving>();
	private List<Renderable> renderables = new ArrayList<Renderable>();
	private List<ForceProvider> forceProviders = new ArrayList<ForceProvider>();
	private List<Blocker> blockers = new ArrayList<Blocker>();
	private boolean prevBlocked = false;
	private boolean prevSecondBlocked = false;
	private Map<Moving, Boolean> prevInTerrains = new HashMap<Moving, Boolean>();
	private Map<Moving, Vector> prevPositions = new HashMap<Moving, Vector>();
	private Map<Moving, Shape> prevShapes = new HashMap<Moving, Shape>();

	private long tick = 0;

	public synchronized void addObject(Object o) {
		if (o instanceof Explosion) {
			explosions.add((Explosion) o);
			explode((Explosion) o);
		}
		if (o instanceof Projectile) {
			projectiles.add((Projectile) o);
		}
		if (o instanceof Sprite) {
			tanks.add((Sprite) o);
		}
		if (o instanceof Advanceable) {
			advanceables.add((Advanceable) o);
		}
		if (o instanceof Positioned) {
			positionedobjects.add((Positioned) o);
		}
		if (o instanceof Moving) {
			movingobjects.add((Moving) o);
			prevInTerrains.put((Moving) o, Terrain.getTerrain().isInsideTerrain(((Moving)o).getBoundingShape()));
			prevPositions.put((Moving)o, ((Moving) o).getPosition());
			prevShapes.put((Moving)o, ((Moving)o).getBoundingShape());
		}
		if (o instanceof Renderable) {
			renderables.add((Renderable) o);
		}
		if (o instanceof ForceProvider) {
			forceProviders.add((ForceProvider) o);
		}
		if (o instanceof Blocker) {
			blockers.add((Blocker) o);
		}
	}

	private void explode(Explosion explosion) {
		explosion.advance(Constants.TICK_TIME_STEP);
		double minAngle;
		double maxAngle;
		Vector normal = Terrain.getTerrain().getSurfaceNormalVector(explosion.getPosition());
		if (normal.isZero()){
			minAngle = 0D;
			maxAngle = Constants.TWO_PI;
		} else {
			double angle = normal.getAngle();
			minAngle = angle - Constants.PI_4;
			maxAngle = angle + Constants.PI_4;
		}
		for (double angle = minAngle; angle <= maxAngle; angle += (maxAngle - minAngle) * (Tank.random.nextDouble() * 0.15D + 0.125D)){
			double x = Math.cos(angle) * (Tank.random.nextDouble() * 10D + 10D);
			double y = Math.sin(angle) * (Tank.random.nextDouble() * 10D + 10D);
			double vx = Math.cos(angle) * (Tank.random.nextDouble() * 100D + 50D);
			double vy = Math.sin(angle) * (Tank.random.nextDouble() * 100D + 50D);
		}
	}

	public synchronized Advanceable[] getAllAdvanceables() {
		return advanceables.toArray(new Advanceable[0]);
	}

	public synchronized Blocker[] getAllBlockers() {
		return blockers.toArray(new Blocker[0]);
	}

	public synchronized Explosion[] getAllExplosions() {
		return explosions.toArray(new Explosion[0]);
	}

	public synchronized ForceProvider[] getAllForceProviders() {
		return forceProviders.toArray(new ForceProvider[0]);
	}

	public synchronized Moving[] getAllMovingObjects() {
		return movingobjects.toArray(new Moving[0]);
	}

	public synchronized Positioned[] getAllPositionedObjects() {
		return positionedobjects.toArray(new Positioned[0]);
	}

	public synchronized Projectile[] getAllProjectiles() {
		return projectiles.toArray(new Projectile[0]);
	}

	public synchronized Renderable[] getAllRenderables() {
		return renderables.toArray(new Renderable[0]);
	}

	public synchronized Sprite[] getAllTanks() {
		return tanks.toArray(new Sprite[0]);
	}

	private Vector getNetForce(Moving moving) {
		ForceProvider[] forceProviders = getAllForceProviders();
		Vector force = Vector.ZERO;
		for (ForceProvider provider : forceProviders) {
			force = force.add(provider.getForce(moving));
		}
		return force;
	}

	private boolean intersects(Positioned p1, Positioned p2){
		Area a1 = new Area(p1.getBoundingShape());
		a1.intersect(new Area(p2.getBoundingShape()));
		return !a1.isEmpty();
	}

	public synchronized void markBlocked() {
		prevBlocked = true;
		prevSecondBlocked = true;
	}

	public synchronized void removeObject(Object o) {
		if (o instanceof Explosion) {
			explosions.remove(o);
		}
		if (o instanceof Projectile) {
			projectiles.remove(o);
		}
		if (o instanceof Sprite) {
			tanks.remove(o);
		}
		if (o instanceof Advanceable) {
			advanceables.remove(o);
		}
		if (o instanceof Positioned) {
			positionedobjects.remove(o);
		}
		if (o instanceof Moving) {
			movingobjects.remove(o);
			prevInTerrains.remove(o);
			prevPositions.remove(o);
			prevShapes.remove(o);
		}
		if (o instanceof Renderable) {
			renderables.remove(o);
		}
		if (o instanceof ForceProvider) {
			forceProviders.remove(o);
		}
		if (o instanceof Blocker) {
			blockers.remove(o);
		}
	}

	public synchronized void reset() {
		projectiles.clear();
		explosions.clear();
		tanks.clear();
		advanceables.clear();
		positionedobjects.clear();
		movingobjects.clear();
		renderables.clear();
		forceProviders.clear();
		blockers.clear();
		prevInTerrains.clear();
		prevPositions.clear();
		prevShapes.clear();
		prevBlocked = false;
		prevSecondBlocked = false;
		tick = 0;
	}
	
	@Override
	public synchronized void run() {
		try {
			tick(Constants.TICK_TIME_STEP);
			GamePanel.getGamePanel().render();
		} catch (Throwable t) {
			t.printStackTrace();
			throw new RuntimeException(t);
		}
	}

	private void tick(double timestep) {

		Advanceable[] advanceables = getAllAdvanceables();
		Positioned[] positioneds = getAllPositionedObjects();
		Moving[] movings = getAllMovingObjects();

		for (Advanceable adv : advanceables) {
			adv.advance(timestep);
		}

		if (tanks.size() > Tank.getTanks().getTurnNumber()){
			boolean right = ControlPanel.getControlPanel().isRightPressed();
			boolean left = ControlPanel.getControlPanel().isLeftPressed();
			if (right && !left && tick % 2 == 0) {
				Tank.getTanks().getCurrentTank().moveRight();
			} else if (left && !right && tick % 2 == 0) {
				Tank.getTanks().getCurrentTank().moveLeft();
			}
		}
		
		for (Positioned positioned : positioneds) {
			for (Moving moving : movings) {
				if (positioned == moving) {
					continue;
				}
				if (intersects(positioned, moving)){
					moving.collide(positioned);
				}
			}
		}
		
		for (Moving moving : movings){
			prevPositions.put(moving, moving.getPosition());
		}

		updateKinematics(movings, timestep);

		for (int i = 0; i < movings.length; i++) {
			Shape prevShape = prevShapes.get(movings[i]);
			boolean wasInTerrain = prevInTerrains.get(movings[i]);
			Shape currShape = movings[i].getBoundingShape();
			boolean isInTerrain = Terrain.getTerrain().isInsideTerrain(currShape);
			if (isInTerrain && !wasInTerrain){
				movings[i].onEnterTerrain(prevShape, currShape, prevPositions.get(movings[i]), movings[i].getPosition());
			} else if (isInTerrain && wasInTerrain) {
				movings[i].onTickInTerrain(prevShape, currShape, prevPositions.get(movings[i]), movings[i].getPosition());
			} else if (!isInTerrain && wasInTerrain) {
				movings[i].onLeaveTerrain(prevPositions.get(movings[i]),
						movings[i].getPosition());
			}
			prevInTerrains.put(movings[i], isInTerrain);
			prevShapes.put(movings[i], currShape);
		}

		for (Moving moving : movings) {
			if (moving.getPosition().getX() < 0 || moving.getPosition().getX() >= Constants.WIDTH || moving.getPosition().getY() >= Constants.HEIGHT){
				moving.onMoveOffScreen();
				moving.setDead();
			}
		}

		for (Positioned positioned : positioneds) {
			if (positioned.isDead()) {
				removeObject(positioned);
			}
		}

		boolean blocked = false;
		Blocker[] blockers = getAllBlockers();
		for (Blocker blocker : blockers) {
			if (blocker.isBlocking()) {
				blocked = true;
				break;
			}
		}

		if (!blocked && !prevBlocked && prevSecondBlocked) {
			if (tanks.size() >= 2) {
				ControlPanel.getControlPanel().postFire();
			}
		}

		prevSecondBlocked = prevBlocked;
		prevBlocked = blocked;

		tick++;

	}

	private void updateKinematics(Moving[] movings, double timestep) {
		Vector[] positions = new Vector[movings.length];
		Vector[][] velocities = new Vector[movings.length][4];
		Vector[][] accelerations = new Vector[movings.length][4];

		// set starting conditions and k1: y0 is position and velocity and k1 is
		// velocity and acceleration
		for (int i = 0; i < movings.length; i++) {
			positions[i] = movings[i].getPosition();
			velocities[i][0] = movings[i].getVelocity();
			accelerations[i][0] = getNetForce(movings[i]).divide(
					movings[i].getMass());
		}

		// get k2
		for (int i = 0; i < movings.length; i++) {
			Vector velocity = velocities[i][0];
			Vector acceleration = accelerations[i][0];

			movings[i].setPosition(positions[i].add(velocity
					.multiply(timestep * 0.5D)));
			movings[i].setVelocity(velocities[i][0].add(acceleration
					.multiply(timestep * 0.5D)));
		}
		for (int i = 0; i < movings.length; i++) {
			velocities[i][1] = movings[i].getVelocity();
			accelerations[i][1] = getNetForce(movings[i]).divide(
					movings[i].getMass());
		}

		// get k3
		for (int i = 0; i < movings.length; i++) {
			Vector velocity = velocities[i][1];
			Vector acceleration = accelerations[i][1];

			movings[i].setPosition(positions[i].add(velocity
					.multiply(timestep * 0.5D)));
			movings[i].setVelocity(velocities[i][0].add(acceleration
					.multiply(timestep * 0.5D)));
		}
		for (int i = 0; i < movings.length; i++) {
			velocities[i][2] = movings[i].getVelocity();
			accelerations[i][2] = getNetForce(movings[i]).divide(
					movings[i].getMass());
		}

		// get k4
		for (int i = 0; i < movings.length; i++) {
			Vector velocity = velocities[i][2];
			Vector acceleration = accelerations[i][2];

			movings[i]
					.setPosition(positions[i].add(velocity.multiply(timestep)));
			movings[i].setVelocity(velocities[i][0].add(acceleration
					.multiply(timestep)));
		}
		for (int i = 0; i < movings.length; i++) {
			velocities[i][3] = movings[i].getVelocity();
			accelerations[i][3] = getNetForce(movings[i]).divide(
					movings[i].getMass());
		}

		// finalize
		for (int i = 0; i < movings.length; i++) {
			Vector position = positions[i].add(velocities[i][0]
					.add(velocities[i][1].add(velocities[i][2]).multiply(2D))
					.add(velocities[i][3]).multiply(timestep / 6D));
			Vector velocity = velocities[i][0].add(accelerations[i][0]
					.add(accelerations[i][1].add(accelerations[i][2]).multiply(
							2D)).add(accelerations[i][3])
					.multiply(timestep / 6D));
			movings[i].setPosition(position);
			movings[i].setVelocity(velocity);
		}
	}

}
