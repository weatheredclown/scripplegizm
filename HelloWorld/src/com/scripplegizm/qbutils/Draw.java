package com.scripplegizm.qbutils;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
// Test
public class Draw {
	public enum QbColor {
		BLACK(Color.BLACK),
		BLUE(Color.BLUE),
		GREEN(Color.GREEN),
		CYAN(Color.CYAN),
		RED(Color.RED),
		PURPLE(Color.MAGENTA),
		ORANGE(Color.YELLOW),
		GRAY(Color.GRAY),
		DKGRAY(Color.DKGRAY),
		LTBLUE(Color.BLUE),
		LTGREEN(Color.GREEN),
		LTCYAN(Color.CYAN),
		LTORANGE(Color.YELLOW),
		LTMAGENTA(Color.MAGENTA),
		YELLOW(Color.YELLOW),
		WHITE(Color.WHITE);

		int color = 0;

		QbColor(int color) {
			this.color = color;
		}
	}

	public static class Point2D {
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
	}

	protected Paint paint;

	public Draw() {
		this.paint = new Paint();
		paint.setTypeface(Typeface.SANS_SERIF);
		paint.setTextSize(18);
	}

	public Paint getPaint() {
		return paint;
	}

	public void LINE(Canvas canvas, Float float1, Float float2,
			Float float3, Float float4, int n) {
		LINE(canvas, Math.round(float1), Math.round(float2), Math.round(float3), Math.round(float4), n);
	}

	public void PSET(Canvas canvas, int xpos, int ypos, int color) {
		PSET(canvas, xpos, ypos, color, false);
	}
	
	public void PSET(Canvas canvas, int xpos, int ypos, int k, boolean directColor) {
		if (directColor) {
			paint.setColor(k);
		} else {
			paint.setColor(QbColor.values()[k].color);
		}
		canvas.drawRect(new RectF(xpos,ypos, xpos, ypos), paint);
	}

	public void LINE(Canvas canvas, int x1, int y1, int x2, int y2, int k) {
		LINE(canvas, x1, y1, x2, y2, k, false);
	}
	
	public void LINE(Canvas canvas, int x1, int y1, int x2, int y2, int k, boolean directColor) {
		if (directColor) {
			paint.setColor(k);
		} else {
			paint.setColor(QbColor.values()[k].color);
		}
		canvas.drawLine(x1, y1, x2, y2, paint);
	}

	public void CIRCLE(Canvas canvas, int xpos, int ypos, int radius, int color) {
		CIRCLE(canvas, xpos, ypos, radius, color, false);
	}
	
	public void CIRCLE(Canvas canvas, int xpos, int ypos, int radius, int color, boolean directColor) {
		if (directColor) {
			paint.setColor(color);
		} else {
			paint.setColor(QbColor.values()[color].color);
		}
		canvas.drawOval(new RectF(xpos-radius,ypos-radius,xpos+radius,ypos+radius), paint);
	}

}
