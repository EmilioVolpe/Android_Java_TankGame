package model;

import java.awt.GraphicsEnvironment;

public interface Constants {
	public static final double INV_SQRT_2 = StrictMath.sqrt(0.5);
	public static final double PI = Math.PI;
	public static final double PI_2 = PI * 0.5D;
	public static final double PI_4 = PI * 0.25D;
	public static final double SQRT_2 = StrictMath.sqrt(2.0D);
	public static final double TWO_PI = PI * 2D;

	public static final double G = 215D;
	public static final int HEIGHT = 520;
	public static final int WIDTH = 960;
	public static final int REFRESH_RATE = GraphicsEnvironment
			.getLocalGraphicsEnvironment().getDefaultScreenDevice()
			.getDisplayMode().getRefreshRate();

	public static final double TICK_TIME_STEP = 1D / REFRESH_RATE;
	public static final long TICK_TIME_STEP_NANOS = (long) (1E9D * TICK_TIME_STEP);
}
