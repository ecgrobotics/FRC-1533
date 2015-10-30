package org.team1533.frcvw;

class Vector {
	double x, y;
	
	public Vector(double x, double y) {
		this.x = x;
		this.y = y;
	}
	
	public double magnitude() {
		return Math.hypot(x, y);
	}
	
	public Vector normalize() {
		double mag = magnitude();
		if (mag > 0) {
			return multiply(1/mag);
		}
		return this.clone();
	}
	
	public Vector multiply(double scalar) {
		return new Vector(x * scalar, y * scalar);
	}
	
	public Vector divide(double scalar) {
		return multiply(1/scalar);
	}
	
	public Vector negate() {
		return multiply(-1);
	}
	
	public Vector clone() {
		return new Vector(x, y);
	}
	
	public static Vector zero() {
		return new Vector(0, 0);
	}
	
	public static Vector vertical() {
		return new Vector(0, 1);
	}
	
	public static Vector horizontal() {
		return new Vector(1, 0);
	}
	
	public double angle() {
		return Math.atan2(y, x);
	}
	
	public double angleDegrees() {
		return angle() * 180 / Math.PI;
	}
	
	public Vector perpendicular() {
		return new Vector(y, -x);
	}
	
	public Vector add(Vector v) {
		return new Vector(x + v.x, y + v.y);
	}
	
	public Vector subtract(Vector v) {
		return add(v.negate());
	}
	
	public int intX() {
		return (int) x;
	}
	
	public int intY() {
		return (int) y;
	}
	
	public static Vector polar(double r, double theta) {
		double x = r * Math.cos(theta),
				y = r * Math.sin(theta);
		return new Vector(x, y);
	}
	
	public double dot(Vector v) {
		return x*v.x + y*v.y;
	}
	
	public String toString() {
		return "<" + x + ", " + y + ">";
	}
	
}
