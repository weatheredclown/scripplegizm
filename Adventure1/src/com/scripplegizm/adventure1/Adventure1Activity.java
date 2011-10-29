package com.scripplegizm.adventure1;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Path.Direction;
import android.graphics.Rect;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.scripplegizm.gameutils.AnimatedSprite;
import com.scripplegizm.gameutils.AnimatedSprite.AnimMode;
import com.scripplegizm.gameutils.Clickables.BitmapButton;
import com.scripplegizm.gameutils.GameActivity;
import com.scripplegizm.gameutils.GameView;
import com.scripplegizm.gameutils.Histogram;
import com.scripplegizm.gameutils.Point2D;

public class Adventure1Activity extends GameActivity {
	@Override
	public GameView createGameView() {
		return new GameView(this) {

			class InputMechanism {
				Set<Direction> directions = new HashSet<Direction>();
			}

			class Character {
				InputMechanism input = new InputMechanism();
				AnimatedSprite sprite;
				Point2D pos = new Point2D(100, 100);

				public Character(int resource) {
					sprite = new AnimatedSprite(decodeBitmap(resource), 10, 3,
							4, AnimMode.LOOPED);
				}

				public void draw(Canvas canvas) {
					sprite.draw(canvas);
				}

				public void update() {
					boolean moved = false;
					if (input.directions.contains(Direction.LEFT)) {
						pos.addX(-SPEED * delta);
						sprite.setTrack(3);
						moved = true;
					}
					if (input.directions.contains(Direction.RIGHT)) {
						pos.addX(SPEED * delta);
						sprite.setTrack(1);
						moved = true;
					}
					if (input.directions.contains(Direction.UP)) {
						pos.addY(-SPEED * delta);
						sprite.setTrack(0);
						moved = true;
					}
					if (input.directions.contains(Direction.DOWN)) {
						pos.addY(SPEED * delta);
						sprite.setTrack(2);
						moved = true;
					}
					sprite.pos.set(pos);
					sprite.Update(moved ? delta : 0.0f);
				}
			}

			Character boy = new Character(R.drawable.boy);
			Character girl = new Character(R.drawable.girl);
			// Character boy = new Character(R.drawable.boy);
			ArrayList<Character> characters = new ArrayList<Character>();

			class ClickableDirectionBitmap extends BitmapButton {

				final Direction dir;

				public ClickableDirectionBitmap(Bitmap bitmap, int x, int y,
						ClickableMode mode, GameState st, int keyCode,
						Direction dir) {
					super(bitmap, x, y, mode, st, keyCode);
					this.dir = dir;
				}

				@Override
				public void draw(Canvas canvas) {
					draw.getPaint().setAlpha(15);
					super.draw(canvas);
				}

				@Override
				public void click() {
					girl.input.directions.add(dir);
				}

				@Override
				public void up() {
					girl.input.directions.remove(dir);
				}
			}

			boolean shouldAddFive = false;

			BitmapButton addFive = new BitmapButton(
					decodeBitmap(R.drawable.boxred), 50, 50,
					ClickableMode.SINGLE_CLICK, GameState.RUNNING) {
				@Override
				public void click() {
					shouldAddFive = true;
				}
			};

			ClickableDirectionBitmap left = new ClickableDirectionBitmap(
					decodeBitmap(R.drawable.btnleft), 0, 0,
					ClickableMode.HOLDABLE, GameState.RUNNING,
					KeyEvent.KEYCODE_DPAD_LEFT, Direction.LEFT);
			ClickableDirectionBitmap right = new ClickableDirectionBitmap(
					decodeBitmap(R.drawable.btnright), 0, 25,
					ClickableMode.HOLDABLE, GameState.RUNNING,
					KeyEvent.KEYCODE_DPAD_RIGHT, Direction.RIGHT);
			ClickableDirectionBitmap up = new ClickableDirectionBitmap(
					decodeBitmap(R.drawable.btnup), 0, 50,
					ClickableMode.HOLDABLE, GameState.RUNNING,
					KeyEvent.KEYCODE_DPAD_UP, Direction.UP);
			ClickableDirectionBitmap down = new ClickableDirectionBitmap(
					decodeBitmap(R.drawable.btndown), 0, 75,
					ClickableMode.HOLDABLE, GameState.RUNNING,
					KeyEvent.KEYCODE_DPAD_DOWN, Direction.DOWN);

			Histogram fps = new Histogram();

			float avgFps = 30.0f;
			boolean drawFps = false;

			Bitmap sunset = decodeBitmap(R.drawable.sunset4);
			Bitmap heart = decodeBitmap(R.drawable.heart);
			float test2 = 0;

			@Override
			public void newGame() {
				test2 = 0;
				girl.pos.setX(-15);
			}

			@Override
			public void drawGame(Canvas canvas) {
				this.blank(canvas, Color.BLACK);
				float scale = yMax / sunset.getHeight();

				Rect src = new Rect(0, 0, sunset.getWidth(), sunset.getHeight());
				Rect dest = new Rect((int) test2, 0, (int) test2
						+ (int) (sunset.getWidth() * scale),
						(int) (sunset.getHeight() * scale));
				if (test2 > -xMax * 2) {
					test2 -= SPEED * delta;
				}
				boy.pos.setX(test2 + xMax * 2 + 300);

				canvas.drawBitmap(sunset, src, dest, draw.getPaint());

				for (Character character : characters) {
					character.draw(canvas);
				}

				if (drawHeart) {
					canvas.drawBitmap(heart, 220, 350, null);
					this.drawText(canvas, "HAPPY 10th ANNIVERSARY!", 25,
							yMax - 25);
				}

				// this.drawText(canvas, "girl: " + girl.pos.getX() + ", " +
				// girl.pos.getY(), 25, yMax - 25);

				if (drawFps) {
					fps.updateValue((int) (delta * 400));
					fps.pos.set(25, yMax - 50);
					fps.draw(canvas);
					float curFps = (1.0f / delta);
					avgFps = (curFps + avgFps) / 2;
					this.drawText(canvas, "FPS: " + avgFps, 25, yMax - 25);
				}
			}

			public static final int SPEED = 100;

			class RandomInput {
				Character c;
				int timer = 0;

				RandomInput(Character c) {
					this.c = c;
				}

				void update() {
					timer--;
					if (timer < 0) {
						timer = rand.nextInt(10);
						c.input.directions.clear();
						switch (rand.nextInt(4)) {
						case 0:
							c.input.directions.add(Direction.LEFT);
							break;
						case 1:
							c.input.directions.add(Direction.RIGHT);
							break;
						case 2:
							c.input.directions.add(Direction.UP);
							break;
						case 3:
							c.input.directions.add(Direction.DOWN);
							break;
						}
					}
				}
			}

			ArrayList<RandomInput> inputs = new ArrayList<RandomInput>();

			@Override
			public void updateGame() {
				if (this.getGameState() == GameState.RUNNING) {
					this.updateDelta();
				}
				if (shouldAddFive) {
					shouldAddFive = false;
					addFiveGuys();
				}
				for (RandomInput i : inputs) {
					i.update();
				}
				for (Character character : characters) {
					character.update();
				}

				if (girl.pos.getX() < 184
						&& !girl.input.directions.contains(Direction.RIGHT)) {
					girl.input.directions.add(Direction.RIGHT);
				} else if (heartCountdown == 0 && !drawHeart) {
					girl.input.directions.remove(Direction.RIGHT);
					if (test2 > -xMax * 2) {
						girl.sprite.Update(delta);
					} else {
						girl.sprite.setFrame(1);
						heartCountdown = 90;
					}
				}

				if (heartCountdown > 0) {
					heartCountdown--;
					if (heartCountdown <= 0) {
						drawHeart = true;
					}
				}
			}

			int heartCountdown = 0;
			boolean drawHeart = false;

			@Override
			public void initGame() {
				/*
				 * buttonManager.addButton(addFive);
				 * buttonManager.addButton(left);
				 * buttonManager.addButton(right); buttonManager.addButton(up);
				 * buttonManager.addButton(down);
				 */
				this.setGameState(GameState.RUNNING);

				addFiveGuys();
				characters.add(boy);
				boy.sprite.setScale(2.2f);
				boy.sprite.setTrack(3);
				boy.sprite.setFrame(1);
				boy.pos.setX(300);
				boy.pos.setY(450);
				characters.add(girl);
				girl.sprite.setScale(1.9f);
				girl.pos.set(-15, 454);
			}

			private void addFiveGuys() {
				/*
				 * for (int i = 0; i < 5; i++) { Character c = new
				 * Character(R.drawable.boy); c.sprite.setScale(2.0f);
				 * c.pos.set(rand.nextInt(xMax), rand.nextInt(yMax));
				 * characters.add(c); RandomInput input = new RandomInput(c);
				 * inputs.add(input); }
				 */
			}

			@Override
			public void reinitGame() {
				// TODO Auto-generated method stub
				up.x = xMax - up.width * 2;
				up.y = yMax - up.height * 2;
				down.x = xMax - down.width * 2;
				down.y = yMax - down.height;
				left.x = xMax - left.width * 3;
				left.y = (int) (yMax - left.height * 1.5);
				right.x = xMax - right.width;
				right.y = (int) (yMax - right.height * 1.5);
			}

		};
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
		case R.id.new_game:
			this.gameView.newGame();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
}