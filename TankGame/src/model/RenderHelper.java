package model;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Ellipse2D;
//RenderHelper class taken from 2-D Open Source game engine,
//will serve to be very useful and help with graphics and time-steps
public final class RenderHelper {

	private static float nextHue = -1F;
	private static final float ONE_OVER_PHI = 0.618033988749895F;

	public static void drawCircle(Graphics2D g2, Vector center, double radius,
			Color color) {
		Ellipse2D circle = new Ellipse2D.Double(center.getX() - radius, center.getY() - radius, radius * 2D, radius * 2D);
		g2.setColor(color);
		g2.fill(circle);
	}

	public static Color generateRandomColor(float saturation, float value) {
		if (nextHue == -1) {
			nextHue = Tank.random.nextFloat();
		} else {
			nextHue += ONE_OVER_PHI;
			nextHue %= 1F;
		}
		return Color.getHSBColor(nextHue, saturation, value);
	}

	public static Color getInverse(Color color) {
		return new Color(color.getBlue(), color.getGreen(), color.getRed());
	}

	public static void setAntialias(Graphics2D g2, boolean enabled) {
		if (enabled) {
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
					RenderingHints.VALUE_ANTIALIAS_ON);
		} else {
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
					RenderingHints.VALUE_ANTIALIAS_OFF);
		}
	}

	private RenderHelper() {

	}
}
