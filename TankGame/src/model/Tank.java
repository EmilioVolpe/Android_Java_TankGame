package model;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.swing.AbstractAction;
import javax.swing.Box;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.KeyStroke;

import controller.GravitationalForceProvider;
import controller.WindForceProvider;
import view.Sprite;
import view.Terrain;

public class Tank extends JPanel {

	private static final long serialVersionUID = 13914758633849548L;
	private static Tank tanks = null;
	public static final Random random = new Random();

	public static Tank getTanks() {
		return tanks;
	}

	public static void main(String[] args) {
		JFrame tanksFrame = new JFrame();
		Box superBox = Box.createHorizontalBox();
		superBox.add(Box.createHorizontalStrut(5));
		Box contentBox = Box.createVerticalBox();
		contentBox.add(Box.createVerticalStrut(5));
		contentBox.add(new Tank());
		contentBox.add(Box.createVerticalStrut(5));
		superBox.add(contentBox);
		superBox.add(Box.createHorizontalStrut(5));
		
		tanksFrame.add(superBox);
		tanksFrame.pack();
		
		tanksFrame.setResizable(false);
		tanksFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		tanksFrame.setTitle("Tanks");
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		tanksFrame.setLocation((screenSize.width - tanksFrame.getWidth()) / 2,
				(screenSize.height - tanksFrame.getHeight()) / 2);
		tanksFrame.setVisible(true);
		GamePanel.getGamePanel().requestFocusInWindow();
		tanks.resetAndInit();
		tanks.start();
	}

	private int turnNumber = 0;
	private int numArchers = 2;
	private ScheduledExecutorService service = null;
	private double wind = 0D;

	public Tank() {
		tanks = this;
		Box contentBox = Box.createVerticalBox();
		contentBox.add(ControlPanel.getControlPanel());
		contentBox.add(Box.createVerticalStrut(10));
		contentBox.add(GamePanel.getGamePanel());
		add(contentBox);
		
		getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
				KeyStroke.getKeyStroke("RIGHT"), "rightPressed");
		getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
				KeyStroke.getKeyStroke("LEFT"), "leftPressed");
		getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
				KeyStroke.getKeyStroke("released RIGHT"), "rightReleased");
		getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
				KeyStroke.getKeyStroke("released LEFT"), "leftReleased");
		getActionMap().put("rightPressed", new AbstractAction() {
			private static final long serialVersionUID = -1820935283689115640L;

			@Override
			public void actionPerformed(ActionEvent arg0) {
				ControlPanel.getControlPanel().setRightPressed(true);
			}
		});
		getActionMap().put("leftPressed", new AbstractAction() {
			private static final long serialVersionUID = 5647571204136508052L;

			@Override
			public void actionPerformed(ActionEvent arg0) {
				ControlPanel.getControlPanel().setLeftPressed(true);
			}
		});
		getActionMap().put("rightReleased", new AbstractAction() {
			private static final long serialVersionUID = -4158483471242011683L;

			@Override
			public void actionPerformed(ActionEvent arg0) {
				ControlPanel.getControlPanel().setRightPressed(false);
			}
		});
		getActionMap().put("leftReleased", new AbstractAction() {
			private static final long serialVersionUID = -726986222058392981L;

			@Override
			public void actionPerformed(ActionEvent arg0) {
				ControlPanel.getControlPanel().setLeftPressed(false);
			}
		});
	}

	public void cycleTurns() {
		turnNumber = (turnNumber + 1) % numArchers;
	}

	public Sprite getCurrentTank() {
		return World.getWorld().getAllTanks()[getTurnNumber()];
	}

	public int getNumberOfTanks() {
		return numArchers;
	}

	public int getTurnNumber() {
		return turnNumber;
	}

	public double getWind() {
		return wind;
	}

	public double getWindStrength() {
		return getWind() / 100D * Constants.G / 3D;
	}

	public void randomizeWind() {
		wind += (random.nextDouble() * 50D - 25D);
		if (wind < -100D) {
			wind = -100D;
		} else if (wind > 100D) {
			wind = 100D;
		}
	}

	public void resetAndInit() {

		if (service != null) {
			service.shutdownNow();
		}

		World world = World.getWorld();
		Terrain terrain = Terrain.getTerrain();
		world.reset();
		terrain.reset();
		world.addObject(terrain);
		world.addObject(new GravitationalForceProvider());
		world.addObject(new WindForceProvider());

		world.addObject(new Sprite(0, new Vector(240, terrain
				.getOriginalHeightValue(240) - 100D)));
		world.addObject(new Sprite(1, new Vector(720, terrain
				.getOriginalHeightValue(720) - 100D)));

		turnNumber = 0;
		wind = 0D;

		service = Executors.newSingleThreadScheduledExecutor();
	}

	public void start() {
		service.scheduleAtFixedRate(World.getWorld(), 0,
				Constants.TICK_TIME_STEP_NANOS, TimeUnit.NANOSECONDS);
	}

}
