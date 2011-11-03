package com.scripplegizm.gameutils;

import android.graphics.Matrix;

public class Point2D {
	private float xFloat;
	private float yFloat;
	private int x;
	private int y;

	public Point2D(int x, int y) {
		set(x, y);
	}
	
	public void set(int x, int y) {
		this.x = x;
		this.y = y;
		xFloat = (float) x;
		yFloat = (float) y;
	}
	
	public void set(float x, float y) {
		xFloat = x;
		yFloat = y;
		this.x = (int) x;
		this.y = (int) y;
	}

	public void setX(int x) {
		this.x = x;
		xFloat = (float) x;
	}
	
	public void setY(int y) {
		this.y = y;
		yFloat = (float) y;
	}
	
	public void setX(float x) {
		xFloat = x;
		this.x = (int) x;
	}
	
	public void setY(float y) {
		yFloat = y;
		this.y = (int) y;
	}
	
	public void addX(float x) {
		setX(xFloat + x);
	}
	
	public void addY(float y) {
		setY(yFloat + y);
	}

	public Point2D(float x2, float y2) {
		set(x2, y2);
	}

	public int dist2(Point2D p) {
		return (p.x-x) * (p.x-x) + (p.y-y) * (p.y-y);
	}

	public void add(Point2D p) {
		set(xFloat + p.xFloat, yFloat + p.yFloat);
	}

	public int mag2() {
		return x*x + y*y;
	}

	public static Point2D add(Point2D a, Point2D b) {
		Point2D ret = new Point2D(a.x, a.y);
		ret.add(b);
		return ret;
	}

	public int getX() {
		return x;
	}
	
	public float getXFloat() {
		return xFloat;
	}
	
	public int getY() {
		return y;
	}
	
	public float getYFloat() {
		return yFloat;
	}

	public void zero() {
		setY(0);
		setX(0);
	}

	public void set(Point2D point) {
		setX(point.getX());
		setY(point.getY());
	}

	public float mag() {
		return (float) Math.sqrt(xFloat*xFloat + yFloat*yFloat);
	}

	public void mult(float f) {
		setX(f * xFloat);
		setY(f * yFloat);
	}

	public void transformBy(Matrix matrix) {
		float [] pt = { xFloat, yFloat };
		matrix.mapVectors(pt);
		set(pt[0], pt[1]);
	}

	public void add(int i, int j) {
		setX(i + x);
		setY(j + y);
	}
}
