package com.scripplegizm.test;

import java.util.ArrayList;
import java.util.Formatter;
import java.util.Random;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;

import com.scripplegizm.qbutils.Draw;

public class HelloWorldActivity extends Activity {
	/** Called when the activity is first created. */

	public static class GraphicsTest extends View {

		Draw draw;

		private int x = 40, y = 100;
		private int xMax = 0;
		private int yMax = 0;
		private StringBuilder statusMsg = new StringBuilder();
		private Formatter formatter = new Formatter(statusMsg);
		int mr = 0;

		// static final Point2D p = new Point2D(1, 140);

		/*
		 * REM **************** REM Walking Guy REM ****************
		 */

		public GraphicsTest(Context context) {
			super(context);

			draw = new Draw();

			// To enable touch mode
			this.setFocusableInTouchMode(true);
		}

		// Called back to draw the view. Also called after invalidate().
		@Override
		protected void onDraw(Canvas canvas) {
			// Draw the ball
			// canvas.drawOval(new RectF(x,y,20,20), paint);

			draw.getPaint().setColor(Color.CYAN);
			canvas.drawText(statusMsg.toString(), 10, 30, draw.getPaint());

			drawWiggle(canvas);

			// Update the position of the ball, including collision detection
			// and reaction.
			this.update();

			// Delay
			try {
				Thread.sleep(30);
			} catch (InterruptedException e) {
			}

			invalidate(); // Force a re-draw
		}

		private void update() {
			/*
			 * this.getX() += 10; if (x >= xMax) { x = 0; } this.getY() += 10;
			 * if (y >= yMax) { y = 0; }
			 */
			statusMsg.delete(0, statusMsg.length()); // Empty buffer
			formatter.format("%d, %d", x, y);
			updateWiggle();
		}

		int f = 1;
		int q = 1;
		int l = 3;
		final static int p = 17;
		ArrayList<Float> z = new ArrayList<Float>(p);
		ArrayList<Float> a = new ArrayList<Float>(p);
		Random rand = new Random();
		int offset = 1;

		private void initWiggle() {
			for (int i = 0; i < p; i++) {
				z.add(new Float(rand.nextInt(xMax)));
				a.add(new Float(rand.nextInt(yMax)));
			}
			z.set(0, 0.0f);
			a.set(0, 0.0f);
		}

		private void drawWiggle(Canvas canvas) {
			for (int n = 0; n < p - 1; n++) {
				draw.LINE(canvas, z.get(n), a.get(n), z.get(n + 1),
						a.get(n + 1), n);
			}
		}

		// Touch-input handler
		@Override
		public boolean onTouchEvent(MotionEvent event) {

			for (int i = 0; i < event.getPointerCount(); i++) {
				float currentX = event.getX(i);
				float currentY = event.getY(i);
				x = (int) currentX;
				y = (int) currentY;
				int n = rand.nextInt(p - 1);
				z.set(n, currentX);
				a.set(n, currentY);
			}

			return true; // Event handled
		}

		private void updateWiggle() {
			int n = rand.nextInt(p - 1);
			z.set(n, z.get(n) + (((xMax / 2) - z.get(n)) * .25f));
			a.set(n, a.get(n) + (((yMax / 2) - a.get(n)) * .25f));
			z.set(0, z.get(p - 1));

			for (int nth = p - 1; nth >= f; nth--) {
				if (z.get(nth) > (z.get(nth - offset)) + q) {
					z.set(nth, z.get(nth) - l);
				}
				if (z.get(nth) < (z.get(nth - offset)) - q) {
					z.set(nth, z.get(nth) + l);
				}
				if (a.get(nth) > (a.get(nth - offset)) + q) {
					a.set(nth, a.get(nth) - l);
				}
				if (a.get(nth) < (a.get(nth - offset)) - q) {
					a.set(nth, a.get(nth) + l);
				}
			}
		}

		// Called back when the view is first created or its size changes.
		@Override
		public void onSizeChanged(int w, int h, int oldW, int oldH) {
			// Set the movement bounds for the ball
			this.xMax = w - 1;
			this.yMax = h - 1;
			initWiggle();

		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		View testView = new GraphicsTest(this);
		setContentView(testView);
	}
}