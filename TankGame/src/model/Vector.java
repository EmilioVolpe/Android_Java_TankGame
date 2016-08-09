package model;

import java.awt.geom.Point2D;

/**
 * Represents a two-dimensional vector in Cartesian coordinates.
 * taken from 2-D Java Game Engine
 */
public class Vector {

	public static final Vector I = new Vector(1, 0);
	public static final Vector J = new Vector(0, 1);
	public static final Vector ZERO = new Vector(0, 0);

	public static double fastInverseSquareRoot(double x) {
		long raw = Double.doubleToRawLongBits(x);
		raw = 0x5fe6eb50c7b537a9L - (raw >>> 1);
		double y = Double.longBitsToDouble(raw);
		y *= 1.5D - 0.5D * x * y * y;
		return y;
	}

	public static Vector polarToCartesian(double r, double theta) {
		return new Vector(r * Math.cos(theta), r * Math.sin(theta));
	}
	
	private final double x;
	private final double y;

	public Vector(double x, double y) {
		this.x = x;
		this.y = y;
	}
	
	public Vector(Point2D point2D){
		this(point2D.getX(), point2D.getY());
	}

	public Vector add(Vector addend) {
		return new Vector(x + addend.getX(), y + addend.getY());
	}
	
	public Point2D asPoint2D(){
		return new Point2D.Double(x, y);
	}

	/**
	 * Two-dimensional projection of (x, y, 0) * (0, 0, z) (which is always in the xy plane.)
	 */
	public Vector cross(double z) {
		return new Vector(y, -x).multiply(z);
	}

	/**
	 * z-component of the cross product of (x, y, 0) and (other.x, other.y, 0).
	 */
	public double cross(Vector vector) {
		return getX() * vector.getY() - getY() * vector.getX();
	}

	public Vector divide(double divisor) {
		return new Vector(x / divisor, y / divisor);
	}

	public double dot(Vector vector) {
		return getX() * vector.getX() + getY() * vector.getY();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Vector other = (Vector) obj;
		if (Double.doubleToLongBits(x) != Double.doubleToLongBits(other.x))
			return false;
		if (Double.doubleToLongBits(y) != Double.doubleToLongBits(other.y))
			return false;
		return true;
	}

	public double getAngle() {
		if (isZero()){
			return 0D;
		}
		return Math.atan2(getY(), getX());
	}

	public Vector getComponent(Vector direction) {
		return direction.multiply(dot(direction) / direction.getNormSquared());
	}
	
	public int getIntegerX() {
		return (int)Math.round(x);
	}

	public int getIntegerY() {
		return (int)Math.round(y);
	}

	public double getInverseNorm() {
		return Vector.fastInverseSquareRoot(getNormSquared());
	}

	public double getNorm() {
		if (isZero()){
			return 0D;
		} else {
			return 1D / getInverseNorm();
		}
	}

	public double getNormCubed() {
		double n = getNormSquared();
		return n * n * Vector.fastInverseSquareRoot(n);
	}

	public double getNormSquared() {
		return getX() * getX() + getY() * getY();
	}

	public Vector getOrthogonalComponent(Vector direction) {
		return subtract(getComponent(direction));
	}

	public double getX() {
		return x;
	}

	public double getY() {
		return y;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		long temp;
		temp = Double.doubleToLongBits(x);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(y);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		return result;
	}

	public Vector multiply(double multiplicand) {
		return new Vector(x * multiplicand, y * multiplicand);
	}

	public Vector negate() {
		return new Vector(-x, -y);
	}

	public Vector normalize() {
		if (isZero()){
			return this;
		}
		return multiply(getInverseNorm());
	}

	public Vector setLength(double length) {
		if (isZero()){
			return this;
		}
		return multiply(getInverseNorm() * length);
	}

	public Vector subtract(Vector subtrahend) {
		return new Vector(x - subtrahend.getX(), y - subtrahend.getY());
	}

	@Override
	public String toString() {
		return String.format("Vector [x=%.3f, y=%.3f]", x, y);
	}
	
	public boolean isZero(){
		return equals(Vector.ZERO);
	}

}
