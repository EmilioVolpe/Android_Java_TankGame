package model;

import java.awt.Point;

public final class MathHelper {

	public static float atan2(float y, float x) {
		return (float) Math.atan2(y, x);
	}

	public static float distSq(float x, float y) {
		return x * x + y * y;
	}

	public static int distSq(int x, int y) {
		return x * x + y * y;
	}

	public static float dot(float x1, float y1, float x2, float y2) {
		return x1 * x2 + y1 * y2;
	}

	public static int dot(int x1, int y1, int x2, int y2) {
		return x1 * x2 + y1 * y2;
	}

	public static int dot(Point v1, Point v2) {
		return dot(v1.x, v1.y, v2.x, v2.y);
	}

	public static float hypot(float x, float y) {
		return squareRoot(distSq(x, y));
	}

	public static int hypot(int x, int y) {
		return squareRoot(distSq(x, y));
	}

	public static int hypot(Point p) {
		return hypot(p.x, p.y);
	}

	public static float inverseHypot(float x, float y) {
		return inverseSquareRoot(distSq(x, y));
	}

	public static float inverseSquareRoot(float x) {
		return 1F / squareRoot(x);
	}

	public static Point reflectionVector(Point inputVector, Point normalVector) {
		int dot2 = 2 * dot(inputVector, normalVector);
		return new Point(inputVector.x - dot2 * normalVector.x, inputVector.y
				- dot2 * normalVector.y);
	}

	public static float squareRoot(float x) {
		return (float) Math.sqrt(x);
	}

	public static int squareRoot(int num) {
		int res = 0;
		int bit = 1 << 30; // The second-to-top bit is set: 1L<<30 for long

		// "bit" starts at the highest power of four <= the argument.
		while (bit > num)
			bit >>= 2;

		while (bit != 0) {
			if (num >= res + bit) {
				num -= res + bit;
				res = (res >> 1) + bit;
			} else {
				res >>= 1;
			}
			bit >>= 2;
		}
		return res;

	}

	private MathHelper() {

	}

}
