package com.scripplegizm.spacegame;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.view.KeyEvent;

import com.scripplegizm.gameutils.ClickableRect;
import com.scripplegizm.gameutils.GameActivity;
import com.scripplegizm.gameutils.GameView;
import com.scripplegizm.gameutils.Point2D;

public class SpaceGameActivity extends GameActivity {

	@Override
	public GameView createGameView() {
		return new GameView(this) {

			Bitmap shipBitmap = decodeBitmap(R.drawable.ship01);
			Bitmap bulletBitmap = decodeBitmap(R.drawable.bullet);

			Set<Direction> dir = new HashSet<Direction>();

			ClickableRect up = new ClickableRect(0, 0, 10, 10,
					ClickableMode.HOLDABLE, GameState.RUNNING,
					KeyEvent.KEYCODE_DPAD_UP) {

				@Override
				public void click() {
					dir.add(Direction.UP);
				}

				@Override
				public void up() {
					dir.remove(Direction.UP);
				}
			};

			ClickableRect right = new ClickableRect(0, 0, 10, 10,
					ClickableMode.HOLDABLE, GameState.RUNNING,
					KeyEvent.KEYCODE_DPAD_RIGHT) {

				@Override
				public void click() {
					dir.add(Direction.RIGHT);
				}

				@Override
				public void up() {
					dir.remove(Direction.RIGHT);
				}
			};

			boolean fire = false;

			ClickableRect space = new ClickableRect(0, 0, 10, 10,
					ClickableMode.SINGLE_CLICK, GameState.RUNNING,
					KeyEvent.KEYCODE_SPACE) {
				@Override
				public void click() {
					fire = true;
				}
			};

			ClickableRect left = new ClickableRect(0, 0, 10, 10,
					ClickableMode.HOLDABLE, GameState.RUNNING,
					KeyEvent.KEYCODE_DPAD_LEFT) {

				@Override
				public void click() {
					dir.add(Direction.LEFT);
				}

				@Override
				public void up() {
					dir.remove(Direction.LEFT);
				}
			};

			@Override
			public void drawGame(Canvas canvas) {
				blank(canvas, Color.BLACK);
				ship.draw(canvas);
				bulletManager.draw(canvas);
			}

			class Ship {
				Point2D pos = new Point2D(100, 100);
				Point2D vel = new Point2D(0, 0);
				float rotation = 0.0f;
				Matrix matrix = new Matrix();

				void fire() {
					Bullet bullet = new Bullet();
					int yOffset = -bulletBitmap.getHeight()
							- shipBitmap.getHeight() / 2;
					bullet.pos.set(0, yOffset);
					int y2 = -bulletBitmap.getHeight() / 2
							+ shipBitmap.getHeight() / 2;
					int x2 = -bulletBitmap.getWidth() / 2
							+ shipBitmap.getWidth() / 2;
					bullet.pos.transformBy(matrix);
					bullet.pos.add(pos);
					bullet.pos.add(x2, y2);
					bullet.vel.set(0.0f, -300.0f);
					bullet.vel.transformBy(matrix);
					bullet.vel.add(vel);
					bulletManager.bullets.add(bullet);
				}

				void update() {
					if (fire) {
						fire = false;
						fire();
					}
					pos.addX(vel.getXFloat() * delta);
					pos.addY(vel.getYFloat() * delta);
					if (pos.getX() > xMax - shipBitmap.getWidth()) {
						pos.setX(xMax - shipBitmap.getWidth());
						vel.setX(-vel.getXFloat());
					} else if (pos.getX() < 0) {
						pos.setX(0);
						vel.setX(-vel.getXFloat());
					}
					if (pos.getY() > yMax - shipBitmap.getHeight()) {
						pos.setY(yMax - shipBitmap.getHeight());
						vel.setY(-vel.getYFloat());
					} else if (pos.getY() < 0) {
						pos.setY(0);
						vel.setY(-vel.getYFloat());
					}
					if (dir.contains(Direction.UP)) {
						float[] pt = { 0.0f, -25.0f };
						matrix.mapVectors(pt);
						vel.mult(0.95f);
						vel.addX(pt[0]);
						vel.addY(pt[1]);
					}
					if (dir.contains(Direction.LEFT)) {
						rotateShip(-10);
					}
					if (dir.contains(Direction.RIGHT)) {
						rotateShip(10);
					}
				}

				public void rotateShip(float degrees) {
					rotation += degrees;
					rotation = rotation % 360;
					matrix.setRotate(rotation, shipBitmap.getWidth() / 2,
							shipBitmap.getHeight() / 2);
				}

				public void draw(Canvas canvas) {
					Matrix composite = new Matrix(matrix);
					composite.postTranslate(pos.getX(), pos.getY());
					canvas.drawBitmap(shipBitmap, composite, null);
				}
			}

			Ship ship = new Ship();

			class BulletManager {
				List<Bullet> bullets = new ArrayList<Bullet>();
				List<Bullet> removeList = new ArrayList<Bullet>();

				void update() {
					for (Bullet bullet : bullets) {
						bullet.update();
					}
					for (Bullet bullet : removeList) {
						bullets.remove(bullet);
					}
					removeList.clear();
				}

				void draw(Canvas canvas) {
					for (Bullet bullet : bullets) {
						bullet.draw(canvas);
					}
				}
			}

			BulletManager bulletManager = new BulletManager();

			class Bullet {
				Point2D pos = new Point2D(0, 0);
				Point2D vel = new Point2D(0, 0);

				void draw(Canvas canvas) {
					canvas.drawBitmap(bulletBitmap, pos.getX(), pos.getY(),
							null);
				}

				void update() {
					pos.addX(vel.getX() * delta);
					pos.addY(vel.getY() * delta);
					if (pos.getX() > xMax || pos.getY() > yMax
							|| pos.getY() < -bulletBitmap.getHeight()
							|| pos.getX() < -bulletBitmap.getWidth()) {
						bulletManager.removeList.remove(this);
					}
				}
			}

			@Override
			public void updateGame() {
				this.updateDelta();
				ship.update();
				bulletManager.update();
			}

			@Override
			public void initGame() {
				buttonManager.addButton(up);
				buttonManager.addButton(right);
				buttonManager.addButton(left);
				buttonManager.addButton(space);
				setGameState(GameState.RUNNING);
			}

			@Override
			public void reinitGame() {
				// TODO Auto-generated method stub

			}

		};
	}
}