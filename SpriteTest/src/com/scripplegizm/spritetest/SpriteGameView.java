package com.scripplegizm.spritetest;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.view.KeyEvent;

import com.scripplegizm.gameutils.Clickables.BitmapButton;
import com.scripplegizm.gameutils.Clickables.Dialog;
import com.scripplegizm.gameutils.GameView;
import com.scripplegizm.qbutils.Draw;
import com.scripplegizm.qbutils.Draw.Point2D;

public class SpriteGameView extends GameView {

	final static int RUN_SPEED = 300;
	final static int MOVE_SPEED = 400;
	final static int FALL_SPEED = 500;
	final static int JUMP_SPEED = 300;

	class Guy {
		Draw.Point2D pos = new Draw.Point2D(0, 0);
		Draw.Point2D jumpOffset = new Draw.Point2D(0, 0);
		Draw.Point2D jumpVelocity = new Draw.Point2D(0, 0);
		boolean jump = false;
		private Bitmap sprite = null;
		private Bitmap shadow = null;
		boolean isPlayer = false;

		Guy(boolean isPlayer) {
			this.isPlayer = isPlayer;
		}

		public void setSprite(Bitmap sprite, Bitmap shadow) {
			this.sprite = sprite;
			this.shadow = shadow;
		}

		public void draw(Canvas canvas) {
			if (sprite != null) {
				if (screenIndex == 0) {
					canvas.drawBitmap(shadow, pos.getX() - 10, pos.getY() + 5,
							null);
				}
				canvas.drawBitmap(sprite, pos.getX() + jumpOffset.getX(),
						getScreenY(), null);
			}
		}

		public void update() {
			if (jump) {
				jump = false;
				jumpVelocity.addY(-(float) JUMP_SPEED);
			}

			if (jumpOffset.mag2() > 0) {
				jumpVelocity.addY(FALL_SPEED * delta);
			}

			jumpOffset.addX(jumpVelocity.getXFloat() * delta);
			jumpOffset.addY(jumpVelocity.getYFloat() * delta);

			if (jumpOffset.getY() > 0) {
				jumpOffset.setY(0);
				jumpVelocity.setY(0);
				if (isPlayer) {
					playSound(SOUNDS.LAND);
				}
			}
		}

		public void init(Bitmap body, Bitmap shadow, int i, int j) {
			setSprite(body, shadow);
			pos.set(i, j);
		}

		public int getScreenY() {
			return pos.getY() + jumpOffset.getY() + (screenIndex * yMax);
		}

		public void setScreenJumpY(int max) {
			jumpOffset.setY(max - (screenIndex * yMax));
		}

		public int getSpriteHeight() {
			if (sprite == null) {
				return 32;
			}
			return sprite.getHeight();
		}

		public int getSpriteWidth() {
			if (sprite == null) {
				return 32;
			}
			return sprite.getWidth();
		}
	}

	enum SOUNDS {
		DEATH(1, R.raw.thundermagic), JUMP(2, R.raw.thip), START(3, R.raw.menu), SCREENUP(
				4, R.raw.funkyzap), LAND(5, R.raw.robotnoise), GAMEOVER(6,
				R.raw.drainmagic), SCREENDOWN(7, R.raw.crush), BLOCKEDBYBAR(8,
				R.raw.metalhit), LANDONBAR(9, R.raw.enemydeath), GETPOINTS(10,
				R.raw.confirm), LOSTMULTIPLIER(11, R.raw.fart);

		int index;
		int assetId;

		SOUNDS(int index, int assetId) {
			this.index = index;
			this.assetId = assetId;
		}

		public int getIndex() {
			return index;
		}

		public int getAssetId() {
			return assetId;
		}
	}

	void playSound(SOUNDS s) {
		playSound(s, 1f);
	}

	void playSound(SOUNDS s, float rate) {
		soundManager.playSound(s.getIndex(), rate);
	}

	public SpriteGameView(Context context) {
		super(context);
		for (SOUNDS s : SOUNDS.values()) {
			soundManager.addSound(s.getIndex(), s.getAssetId());
		}
	}

	Bitmap cloud1 = decodeBitmap(R.drawable.spritecloud01);
	Bitmap cloud2 = decodeBitmap(R.drawable.spritecloud02);
	Bitmap fence = decodeBitmap(R.drawable.spritefence);
	Bitmap tree = decodeBitmap(R.drawable.spritetree);
	Bitmap mountain = decodeBitmap(R.drawable.spritemountain);
	Bitmap guy = decodeBitmap(R.drawable.spriteguy);
	Bitmap shadow = decodeBitmap(R.drawable.spriteshadow);
	Bitmap jumpBtnBitmap = decodeBitmap(R.drawable.btnjump);
	Bitmap rightBtnBitmap = decodeBitmap(R.drawable.btnright);
	Bitmap leftBtnBitmap = decodeBitmap(R.drawable.btnleft);
	Bitmap badguy = decodeBitmap(R.drawable.spritebadguy);

	float posFloat = 0.0f;
	int pos = 0;
	int cloud1Offset = 0;
	int cloud2Offset = 0;
	int fenceOffset = 0;
	int treeOffset = 0;
	int mountainOffset = 0;

	Dialog dialog = null;

	static final int FENCE_DELAY = 1;
	static final int TREE_DELAY = 15;
	static final int MOUNTAIN_DELAY = 32;
	static final int CLOUD2_DELAY = 40;
	static final int CLOUD1_DELAY = 60;

	static final int MIN_GUY_Y = 312;
	static final int BOX_WIDTH = 250;
	static final int BOX_HEIGHT = 50;
	static final int MIN_BOX_Y = 128;

	Guy player = new Guy(true);
	Guy enemy = new Guy(false);

	BitmapButton jumpButton = new BitmapButton(jumpBtnBitmap, 0, 0, ClickableMode.SINGLE_CLICK,
			GameState.RUNNING, KeyEvent.KEYCODE_DPAD_UP) {
		@Override
		public void click() {
			player.jump = true;
			playSound(SOUNDS.JUMP);
		}
	};

	BitmapButton leftButton = new BitmapButton(leftBtnBitmap, 120, 0, ClickableMode.HOLDABLE,
			GameState.RUNNING, KeyEvent.KEYCODE_DPAD_LEFT) {

		@Override
		public void click() {
			player.pos.addX(-MOVE_SPEED * delta);
			if (player.pos.getX() < 0) {
				player.pos.setX(0);
			}
		}

	};

	BitmapButton rightButton = new BitmapButton(rightBtnBitmap, 240, 0, ClickableMode.HOLDABLE,
			GameState.RUNNING, KeyEvent.KEYCODE_DPAD_RIGHT) {

		@Override
		public void click() {
			player.pos.addX(MOVE_SPEED * delta);
			if (player.pos.getX() > xMax - player.sprite.getWidth()) {
				player.pos.setX(xMax - player.sprite.getWidth());
			}
		}

	};

	private void startGame() {
		playSound(SOUNDS.START);
		setGameState(GameState.RUNNING);
		gameLives.setValue(3);
		gameBonusMulti.setValue(0);
		gameScore.setValue(0);
		player.jumpOffset.zero();
		player.jumpVelocity.zero();
	}

	class JumpBox {
		Draw.Point2D boxPos = new Draw.Point2D(0, 400);
		int boxColor = Color.BLUE;
		boolean gotTouch = false;
		boolean neededTouch = false;

		JumpBox() {
			resetBox();
		}

		public JumpBox(int i) {
			resetBox(i);
		}

		public void resetBox() {
			resetBox(screenIndex);
		}

		public void resetBox(int idx) {
			boxPos.setX(pos + xMax + rand.nextInt(300));
			boxPos.setY(rand.nextInt(maxBoxY - MIN_BOX_Y) + MIN_BOX_Y);
			switch (idx) {
			case 0:
				boxColor = Color.BLUE;
				break;
			case 1:
				boxColor = Color.MAGENTA;
				break;
			default:
				boxColor = Color.RED;
			}
			gotTouch = false;
			neededTouch = false;
		}

		public void updateBox(Guy guy) {
			if (boxPos.getX() - pos < -BOX_WIDTH) {
				resetBox();
			}

			int guyY = guy.getScreenY();
			if (boxPos.getX() - pos < guy.pos.getX()
					&& boxPos.getX() + BOX_WIDTH - pos > guy.pos.getX()) {
				neededTouch = true;
				int boxBottom = boxPos.getY() + BOX_HEIGHT;
				if (guyY < boxBottom) {
					if (guyY > boxPos.getY()) {
						if (guy.jumpVelocity.getY() < 5) {
							playSound(SOUNDS.BLOCKEDBYBAR);
						}
						guy.jumpVelocity.setY(Math.max(5,
								guy.jumpVelocity.getY()));
						guy.setScreenJumpY(boxBottom - guy.pos.getY());
					} else {
						int guyRelativeBoxTop = boxPos.getY()
								- guy.getSpriteHeight();
						float yVelocity = guy.jumpVelocity.getXFloat() * delta;
						if (guyY > guyRelativeBoxTop
								|| guyY + yVelocity > guyRelativeBoxTop) {
							guyY = guyRelativeBoxTop;
							guy.setScreenJumpY(guyRelativeBoxTop
									- guy.pos.getY());
						}
						if (guyY == guyRelativeBoxTop) {
							guy.jumpVelocity.setY(Math.min(-5,
									-guy.jumpVelocity.getY()));
							if (!gotTouch) {
								playSound(SOUNDS.LANDONBAR);
								boxColor = Color.YELLOW;
								gotTouch = true;
								gameBonusMulti.add(Math.max(1, screenIndex
										* screenIndex * 5));
							}
							playSound(SOUNDS.GETPOINTS);
							gameScore.add(1 * gameBonusMulti.getValue());
						}
					}
				}
			} else if (neededTouch) {
				if (!gotTouch) {
					if (gameBonusMulti.getValue() > 0) {
						gameBonusMulti.setValue(0);
						playSound(SOUNDS.LOSTMULTIPLIER);
					}
				}
				neededTouch = false;
				gotTouch = false;
			}
		}
	}

	ArrayList<JumpBox> boxes = null;

	// GAME SCORING
	private TrackedValue gameHiScore = new TrackedValue(0);
	private TrackedValue gameLives = new TrackedValue(3);
	private TrackedValue gameScore = new TrackedValue(0);
	private TrackedValue gameBonusMulti = new TrackedValue(0);

	int screenIndex = 0;
	List<Point2D> stars = new ArrayList<Point2D>();

	@Override
	public void drawGame(Canvas canvas) {
		if (screenIndex == 0) {
			canvas.drawColor(Color.rgb(0, 255, 255));
			drawLayer(canvas, cloud1, 5.3f, 158, cloud1Offset);
			drawLayer(canvas, cloud2, 5.6f, 168, cloud2Offset);
			drawLayerAlignBottom(canvas, mountain, 0.625f, 300, mountainOffset);
			drawLayerAlignBottom(canvas, tree, 1.0f, 300, treeOffset);
			drawLayerAlignBottom(canvas, fence, 1.0f, 300, fenceOffset);
			draw.getPaint().setColor(Color.GREEN);
			canvas.drawRect(0, 332, xMax, yMax, draw.getPaint());
		} else if (screenIndex == 1) {
			canvas.drawColor(Color.rgb(0, 255, 255));
			drawLayer(canvas, cloud1, 70, 128, cloud1Offset);
			drawLayer(canvas, cloud2, 95, 200, cloud2Offset);
			drawLayer(canvas, cloud2, 80, 260, cloud2Offset);
			drawLayer(canvas, cloud2, 130, 290, cloud2Offset);
			drawLayer(canvas, cloud1, 210, 328, cloud1Offset);
			drawLayer(canvas, cloud2, 64, 368, cloud2Offset);
			drawLayer(canvas, cloud1, 140, 400, cloud1Offset);
			drawLayer(canvas, cloud1, 195, 460, cloud1Offset);
			drawLayer(canvas, cloud1, 105, 490, cloud1Offset);
			drawLayer(canvas, cloud2, 115, 528, cloud2Offset);
		} else {
			// SPACE, SPACE, SPACE!
			canvas.drawColor(Color.BLACK);
			draw.getPaint().setColor(Color.WHITE);
			for (Point2D star : stars) {
				int starY = Math.abs((star.getY() + player.jumpOffset.getY()))
						% yMax;
				canvas.drawRect(new Rect(star.getX(), starY, star.getX() + 5,
						starY + 5), draw.getPaint());
			}
		}

		enemy.draw(canvas);
		player.draw(canvas);

		draw.getPaint().setColor(Color.BLACK);
		this.startUiDraw();
		this.drawUiValue(canvas, "Score: ", gameScore);
		drawUiValue(canvas,
				"Multi: " + Integer.toString(gameBonusMulti.getValue()) + "x",
				gameBonusMulti.processValue());
		drawUiValue(canvas, "Lives: ", gameLives);
		drawUiValue(canvas, "High: ", gameHiScore);

		// debugDraw(canvas);
		
		int boxDrawPos = boxes.get(BOX_CUR_SCREEN).boxPos.getX() - pos;
		if (boxDrawPos + BOX_WIDTH > 0 || boxDrawPos < xMax) {
			draw.getPaint().setColor(boxes.get(BOX_CUR_SCREEN).boxColor);
			canvas.drawRect(boxDrawPos,
					boxes.get(BOX_CUR_SCREEN).boxPos.getY(), boxDrawPos
							+ BOX_WIDTH,
					boxes.get(BOX_CUR_SCREEN).boxPos.getY() + BOX_HEIGHT,
					draw.getPaint());
		}

		if (getGameState() != GameState.RUNNING && dialog == null) {
			dialog = new Dialog(xMax / 2, yMax / 2, 200, 30, "Start",
					Color.RED, Color.WHITE) {
						@Override
						public void click() {
							startGame();
						}
			};
			buttonManager.addButton(dialog);
		}
	}

	void debugDraw(Canvas canvas) {
		int fonty = yMax - 96;
		draw.getPaint().setColor(Color.BLACK);
		canvas.drawText(" Pos: " + player.pos.getY(), 30, fonty,
				draw.getPaint());
		fonty += 16;
		canvas.drawText(" Jump: " + player.getScreenY(), 30, fonty,
				draw.getPaint());
		fonty += 16;
		canvas.drawText(" Screen: " + screenIndex, 30, fonty, draw.getPaint());
		fonty += 16;
		canvas.drawText(" BoxY: " + boxes.get(BOX_CUR_SCREEN).boxPos.getY(),
				30, fonty, draw.getPaint());
		fonty += 16;
		canvas.drawText(" delta: " + delta, 30, fonty, draw.getPaint());
		fonty += 16;
		canvas.drawText(" jumpvel: " + player.jumpVelocity.getY(), 30, fonty,
				draw.getPaint());
	}

	final static int BOX_UP_SCREEN = 2;
	final static int BOX_CUR_SCREEN = 1;
	final static int BOX_DN_SCREEN = 0;

	int curBox = 0;

	private void drawLayerAlignBottom(Canvas canvas, Bitmap bitmap, float f,
			int bottomY, int offset) {
		int height = bitmap.getHeight();
		drawLayer(canvas, bitmap, f, bottomY - height, offset);
	}

	private void drawLayer(Canvas canvas, Bitmap bitmap, float f, int y,
			int offset) {
		int width = (int) (bitmap.getWidth() * f);
		drawLayer(canvas, bitmap, width, y, offset);
	}

	private void drawUiValue(Canvas canvas, String string, TrackedValue tv) {
		drawUiValue(canvas, string + tv.getValue(), tv.processValue());
	}

	private void drawUiValue(Canvas canvas, String string, float processValue) {
		float textSize = 16.0f + 5.0f * processValue;
		draw.getPaint().setTextSize(textSize);

		float pct = 1.0f - (processValue * 0.5f);
		int colorRange = Color.WHITE - Color.MAGENTA;
		int textColor = Color.MAGENTA + (int) (colorRange * pct);
		draw.getPaint().setColor(Color.BLACK);
		canvas.drawText(string, 101, uiFontY + 1, draw.getPaint());

		draw.getPaint().setColor(textColor);
		canvas.drawText(string, 100, uiFontY, draw.getPaint());
		draw.getPaint().setTextSize(16.0f);
		uiFontY += (int) textSize + 2;
	}

	int uiFontY = 30;

	private void startUiDraw() {
		uiFontY = 30;
	}

	private void drawLayer(Canvas canvas, Bitmap bitmap, int xInterval, int y,
			int offset) {
		for (int i = -xInterval; i < xMax + xInterval; i += xInterval) {
			canvas.drawBitmap(bitmap, (i - offset % xInterval), y, null);
		}
	}

	@Override
	public void updateGame() {

		if (getGameState() == GameState.RUNNING) {
			updateDelta();
		} else {
			delta = 0;
		}

		int lastIdx = screenIndex;
		screenIndex = -(player.pos.getY() + player.jumpOffset.getY() - yMax)
				/ yMax;
		jumpButton.setVisible(screenIndex < 2);

		if (lastIdx != screenIndex) {
			if (lastIdx < screenIndex) {
				playSound(SOUNDS.SCREENUP, 1.0f + (screenIndex * 0.1f));
				boxes.set(BOX_DN_SCREEN, boxes.get(BOX_CUR_SCREEN));
				boxes.set(BOX_CUR_SCREEN, boxes.get(BOX_UP_SCREEN));
				boxes.set(BOX_UP_SCREEN, new JumpBox(screenIndex + 1));
				boxes.get(BOX_UP_SCREEN).boxPos.setX(pos + rand.nextInt(xMax));
			} else {
				playSound(SOUNDS.SCREENDOWN);
				boxes.set(BOX_UP_SCREEN, boxes.get(BOX_CUR_SCREEN));
				boxes.set(BOX_CUR_SCREEN, boxes.get(BOX_DN_SCREEN));
				boxes.set(BOX_DN_SCREEN, new JumpBox(screenIndex - 1));
				boxes.get(BOX_DN_SCREEN).boxPos.setX(pos + rand.nextInt(xMax));
			}
		}

		posFloat += RUN_SPEED * delta;
		pos = (int) posFloat;
		cloud1Offset = pos / CLOUD1_DELAY;
		cloud2Offset = pos / CLOUD2_DELAY;
		fenceOffset = pos / FENCE_DELAY;
		treeOffset = pos / TREE_DELAY;
		mountainOffset = pos / MOUNTAIN_DELAY;

		player.update();

		updateEnemy();

		updateBoxes(player);

		if (gameScore.getValue() > gameHiScore.getValue()) {
			gameHiScore.setValue(gameScore.getValue());
		}
		if (gameLives.getValue() == 0 && getGameState() == GameState.RUNNING) {
			// END GAME
			setGameState(GameState.PAUSED);
			playSound(SOUNDS.GAMEOVER);
		}
	}

	private void updateBoxes(Guy guy) {
		// for (JumpBox box : boxes) {
		boxes.get(BOX_CUR_SCREEN).updateBox(guy);
		// }
	}

	float enemyXFloat = 0.0f;
	private int maxBoxY = 256;
	float enemyNextJumpTime = 0.0f;

	private void updateEnemy() {
		if (getGameState() != GameState.RUNNING) {
			enemyNextJumpTime = rand.nextFloat() * 3.0f;
			return;
		}
		enemy.update();
		enemyNextJumpTime -= delta;
		if (enemyNextJumpTime <= 0.0f) {
			enemyNextJumpTime = rand.nextFloat() * 3.0f;
			enemy.jump = true;
		}
		enemyXFloat -= RUN_SPEED * delta;
		enemy.pos.setX(enemyXFloat);
		if (enemy.pos.getX() < -enemy.getSpriteWidth()) {
			enemy.jumpOffset.setY(-yMax * screenIndex);
			// enemy.jumpVelocity.setY(rand.nextInt(FALL_SPEED));
			enemy.pos.setX(rand.nextInt(200) + xMax);
			enemyXFloat = (float) enemy.pos.getX();
		}
		Point2D epos = Draw.Point2D.add(enemy.pos, enemy.jumpOffset);
		Point2D ppos = Draw.Point2D.add(player.pos, player.jumpOffset);

		if (epos.dist2(ppos) < 1024) {
			if (gameLives.subtract(1) != 0) {
				enemy.pos.setX(-100);
				enemyXFloat = -100.0f;
				playSound(SOUNDS.DEATH);
			}
		}
	}

	void resetBoxes() {
		if (boxes == null) {
			boxes = new ArrayList<JumpBox>();
			for (int i = 0; i < 3; i++) {
				boxes.add(new JumpBox());
			}
		}
		for (JumpBox box : boxes) {
			box.resetBox();
		}
	}

	@Override
	public void reinitGame() {
		player.init(guy, shadow, xMax / 2, (MIN_GUY_Y + yMax) / 2);
		enemy.init(badguy, shadow, rand.nextInt(500) + xMax, player.pos.getY());
		posFloat = 0.0f;
		resetBoxes();
		enemyXFloat = xMax * 2.0f;
		this.maxBoxY = player.pos.getY() - 64 - BOX_HEIGHT;
		stars.clear();
		for (int i = 0; i < 50; i++) {
			stars.add(new Point2D(rand.nextInt(xMax), rand.nextInt(yMax)));
		}
		rightButton.setX(xMax - rightButton.getBitmap().getWidth());
		leftButton.setX(xMax - (leftButton.getBitmap().getWidth() * 2));
	}

	@Override
	public void loadGame(SharedPreferences settings) {
		int myInt = settings.getInt("hiscore", 0);
		gameHiScore.setValue(myInt);
	}

	@Override
	public void saveGame(Editor editor) {
		editor.putInt("hiscore", gameHiScore.getValue());
	}

	@Override
	public void initGame() {
		buttonManager.addButton(jumpButton);
		buttonManager.addButton(leftButton);
		buttonManager.addButton(rightButton);
	}
}
