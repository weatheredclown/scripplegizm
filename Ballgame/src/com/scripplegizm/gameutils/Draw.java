package com.scripplegizm.gameutils;

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
		canvas.drawRect(new RectF(xpos,ypos, xpos+1, ypos+1), paint);
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
