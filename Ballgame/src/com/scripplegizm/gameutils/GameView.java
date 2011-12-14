package com.scripplegizm.gameutils;

import java.util.Random;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public abstract class GameView extends SurfaceView implements
		SurfaceHolder.Callback {

	public enum GameState {
		PAUSED, RUNNING,
	}

	public enum Direction {
		NONE, LEFT, RIGHT, UP, DOWN, FIRE
	}

	public enum ClickableMode {
		SINGLE_CLICK, HOLDABLE
	}

	static public class ExtraMath {
		public static <T extends Number & Comparable<T>> T clamp(T min,
				T value, T max) {
			if (min.compareTo(value) > 0) {
				return min;
			}
			if (max.compareTo(value) < 0) {
				return max;
			}
			return value;
		}
	}

	protected ButtonManager buttonManager = new ButtonManager();
	protected float delta;
	protected long currentTime;
	protected SoundManager soundManager = new SoundManager();
	protected RenderThread thread;
	public static int xMax;
	public static int yMax;
	public static Random rand = new Random();
	private long lastUpdateTime;
	boolean gameInitialized = false;

	public static Draw draw = new Draw();
	private static GameState gameState = GameState.PAUSED;

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		buttonManager.keyUp(keyCode);
		return super.onKeyUp(keyCode, event);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		buttonManager.keyDown(keyCode);
		return super.onKeyDown(keyCode, event);
	}

	public void blank(Canvas canvas, int black) {
		canvas.drawColor(black);
	}

	public void drawText(Canvas canvas, String string, int x, int y) {
		draw.getPaint().setColor(Color.BLACK);
		canvas.drawText(string, x + 1, y + 1, draw.getPaint());

		draw.getPaint().setColor(Color.WHITE);
		canvas.drawText(string, x, y, draw.getPaint());
	}

	public Bitmap decodeBitmap(int drawable) {
		return BitmapFactory.decodeResource(getResources(), drawable);
	}

	protected class TrackedValue {

		private int value;
		private float valueSetTime = 0.0f;

		public TrackedValue(int val) {
			value = val;
		}

		public int add(int i) {
			return setValue(value + i);
		}

		public int subtract(int i) {
			return setValue(value - i);
		}

		public int setValue(int value) {
			if (value != this.value) {
				valueSetTime = 2.0f;
			}
			this.value = value;
			return this.value;
		}

		public float processValue() {
			float retVal = valueSetTime;
			valueSetTime = Math.max(0.0f, valueSetTime - delta);
			return retVal;
		}

		boolean valueSetRecently() {
			return valueSetTime > 0.0f;
		}

		public int getValue() {
			return value;
		}

		public int draw(Canvas canvas, String string, int x, int uiFontY) {
			return drawUiValue(canvas, String.format(string, getValue()),
					processValue(), x, uiFontY);
		}

		private int drawUiValue(Canvas canvas, String string,
				float processValue, int x, int uiFontY) {
			float textSize = 16.0f + 5.0f * processValue;
			draw.getPaint().setTextSize(textSize);

			float pct = 1.0f - (processValue * 0.5f);
			int colorRange = Color.WHITE - Color.MAGENTA;
			int textColor = Color.MAGENTA + (int) (colorRange * pct);
			draw.getPaint().setColor(Color.BLACK);
			canvas.drawText(string, x + 1, uiFontY + 1, draw.getPaint());

			draw.getPaint().setColor(textColor);
			canvas.drawText(string, x, uiFontY, draw.getPaint());
			draw.getPaint().setTextSize(16.0f);
			return (int) textSize + 2;
		}
	}

	public void updateDelta() {
		this.currentTime = System.currentTimeMillis();
		this.delta = Math.min(0.15f, (currentTime - lastUpdateTime) / 1000.0f);
		this.lastUpdateTime = currentTime;
	}

	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		// TODO Auto-generated method stub
	}

	public void surfaceCreated(SurfaceHolder holder) {
		thread.setRunning(true);
		thread.start();
	}

	public void surfaceDestroyed(SurfaceHolder holder) {
		// simply copied from sample application LunarLander:
		// we have to tell thread to shut down & wait for it to finish, or else
		// it might touch the Surface after we return and explode
		boolean retry = true;
		thread.setRunning(false);
		while (retry) {
			try {
				thread.join();
				retry = false;
			} catch (InterruptedException e) {
				// we will try it again and again...
			}
		}
	}

	public GameView(Context context) {
		super(context);

		soundManager.initSounds(context);

		getHolder().addCallback(this);
		thread = new RenderThread(getHolder(), this);

		// To enable touch mode
		this.setFocusableInTouchMode(true);
	}

	// Touch-input handler
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (buttonManager.onTouchEvent(event)) {
			return true;
		}
		return false;
	}

	// Called back when the view is first created or its size changes.
	@Override
	public void onSizeChanged(int w, int h, int oldW, int oldH) {
		// Set the movement bounds for the ball
		xMax = w - 1;
		yMax = h - 1;
		if (!gameInitialized) {
			// Just moved this from onDraw to here..
			// Is there a threading reason for it to be onDraw?
			gameInitialized = true;
			initGame();
		}
		this.reinitGame();
	}

	@Override
	protected void onDraw(Canvas canvas) {
		// Update the position of the ball, including collision detection and
		// reaction.
		buttonManager.updateButtons();
		this.updateGame();

		this.drawGame(canvas);
		buttonManager.drawButtons(canvas);
	}

	public abstract void drawGame(Canvas canvas);

	public abstract void updateGame();

	/**
	 * This function is called when the screen is resized.
	 */
	public abstract void reinitGame();

	public void initGame() {

	}

	public void loadGame(SharedPreferences settings) {
		/*
		 * boolean silent = settings.getBoolean("silentMode", false);
		 */
	}

	public void saveGame(Activity activity) {
		SharedPreferences settings = activity.getPreferences(0);
		SharedPreferences.Editor editor = settings.edit();
		saveGame(editor);
		// Commit the edits!
		editor.commit();
	}

	public void saveGame(Editor editor) {
		/*
		 * editor.putBoolean("silentMode", mSilentMode);
		 */
	}

	public static GameState getGameState() {
		return gameState;
	}

	public static void setGameState(GameState gameState) {
		GameView.gameState = gameState;
	}

	public void newGame() {
	}

	public void loadGame(Activity activity) {
		SharedPreferences settings = activity.getPreferences(0);
		loadGame(settings);
	}

	public boolean onOptionsItemSelected(int itemId) {
		return false;
	}
}
