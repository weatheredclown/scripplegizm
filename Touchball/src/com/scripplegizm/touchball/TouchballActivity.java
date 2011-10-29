package com.scripplegizm.touchball;

import java.util.HashMap;

import android.app.Activity;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Bundle;
import android.view.MotionEvent;

import com.scripplegizm.gameutils.GameView;
import com.scripplegizm.qbutils.Draw;

public class TouchballActivity extends Activity {
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		GameView gm = new GameView(this) {

			HashMap<Integer, Draw.Point2D> balls = new HashMap<Integer, Draw.Point2D>();

			Draw.Point2D flyBall = new Draw.Point2D(0, 0);
			Draw.Point2D flyVelocity = new Draw.Point2D(0, 0);

			static final int MAX_VELOCITY = 20;

			// Touch-input handler
			@Override
			public boolean onTouchEvent(MotionEvent event) {
				final int action = event.getAction();
				switch (action & MotionEvent.ACTION_MASK) {
				case MotionEvent.ACTION_DOWN:
				case MotionEvent.ACTION_POINTER_DOWN: {
					int i = (action & MotionEvent.ACTION_POINTER_INDEX_MASK) >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;
					Integer pointerId = event.getPointerId(i);
					if (!balls.containsKey(pointerId)) {
						balls.put(pointerId, new Draw.Point2D(event.getX(i),
								event.getY(i)));
					}
					break;
				}
				case MotionEvent.ACTION_UP:
				case MotionEvent.ACTION_POINTER_UP: {
					int i = (action & MotionEvent.ACTION_POINTER_INDEX_MASK) >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;
					Integer pointerId = event.getPointerId(i);
					if (balls.containsKey(pointerId)) {
						balls.remove(pointerId);
					}
					break;
				}
				case MotionEvent.ACTION_MOVE: {
					for (int i = 0; i < event.getPointerCount(); i++) {
						Integer pointerId = event.getPointerId(i);
						if (balls.containsKey(pointerId)) {
							Draw.Point2D pt = balls.get(pointerId);
							pt.setX((int) event.getX(i));
							pt.setY((int) event.getY(i));
						}
					}
					break;
				}
				}
				return true; // Event handled
			}

			int nextColor = 0;

			@Override
			public void drawGame(Canvas canvas) {
				draw.getPaint().setColor(Color.BLACK);
				canvas.drawRect(0, 0, xMax + 1, yMax + 1, draw.getPaint());
				int lastX = -1, lastY = -1;
				int lastColor = nextColor;
				Draw.Point2D closest = null;
				float dist2 = 100000000.0f;
				for (Draw.Point2D p : balls.values()) {
					this.draw.CIRCLE(canvas, p.getX(), p.getY(), 20,
							(nextColor++) * -100, true);

					if (lastX != -1) {
						draw.LINE(canvas, p.getX(), p.getY(), lastX, lastY, 14);
					}
					lastX = p.getX();
					lastY = p.getY();
					float newDist2 = flyBall.dist2(p);
					if (newDist2 < dist2) {
						dist2 = newDist2;
						closest = p;
					}
				}
				if (lastX != -1) {
					for (Draw.Point2D p : balls.values()) {
						draw.LINE(canvas, p.getX(), p.getY(), lastX, lastY, 14);
						break;
					}
				}

				if (closest != null) {
					// draw.LINE(canvas, flyBall.x, flyBall.y, closest.x,
					// closest.y, 15);
					if (closest.getY() > flyBall.getY()) {
						flyVelocity.setY(Math.min(flyVelocity.getY() + 1,
								MAX_VELOCITY));
					} else if (closest.getY() < flyBall.getY()) {
						flyVelocity.setY(Math.max(flyVelocity.getY() - 1,
								-MAX_VELOCITY));
					}
					if (closest.getX() > flyBall.getX()) {
						flyVelocity.setX(Math.min(flyVelocity.getX() + 1,
								MAX_VELOCITY));
					} else if (closest.getX() < flyBall.getX()) {
						flyVelocity.setX(Math.max(flyVelocity.getX() - 1,
								-MAX_VELOCITY));
					}
				} else {
					flyVelocity.setY(flyVelocity.getY()
							+ (flyVelocity.getY() < 0 ? 1 : 0)
							- (flyVelocity.getY() > 0 ? 1 : 0));
					flyVelocity.setX(flyVelocity.getX()
							+ (flyVelocity.getX() < 0 ? 1 : 0)
							- (flyVelocity.getX() > 0 ? 1 : 0));
				}
				flyBall.add(flyVelocity);
				if (lastColor == nextColor) {
					nextColor = 0;
				}
				draw.CIRCLE(canvas, flyBall.getX(), flyBall.getY(), 15, 4);
			}

			@Override
			public void updateGame() {
				// TODO Auto-generated method stub

			}

			@Override
			public void reinitGame() {
				this.flyBall.setX(rand.nextInt(xMax));
				this.flyBall.setY(rand.nextInt(yMax));
			}

		};
		setContentView(gm);
	}
}