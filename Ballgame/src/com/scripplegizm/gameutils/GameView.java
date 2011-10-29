package com.scripplegizm.gameutils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint.Align;
import android.graphics.Rect;
import android.media.AudioManager;
import android.media.SoundPool;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.scripplegizm.qbutils.Draw;
import com.scripplegizm.qbutils.Draw.Point2D;

public abstract class GameView extends SurfaceView implements
		SurfaceHolder.Callback {

	public enum Direction {
		NONE, LEFT, RIGHT, UP, DOWN
	}


	/**
	 * Display a value over time.
	 * @author tlaubach
	 *
	 */
	public class Histogram {
		int lastX = 0;
		ArrayList<Integer> lastValue = new ArrayList<Integer>();
		public Point2D pos = new Point2D(100, 500);
		public Histogram() {
			for (int i = 0; i < 50; i++) {
				lastValue.add(0);
			}
		}
		public void draw(Canvas canvas) {
			draw.getPaint().setColor(Color.WHITE);
			for (int i = 0; i < 50; i++) {
				int magic = (i + lastX) % 50;
				canvas.drawRect(pos.getX() + i * 5, pos.getY() - lastValue.get(magic),
						pos.getX() + 5 + i * 5, pos.getY(), draw.getPaint());
			}
		}
		public void updateValue(int val) {
			lastValue.set(lastX++, val);
			lastX = lastX % 50;
		}
	}
	
	public class ButtonManager {
		List<ClickableRect> buttons = new ArrayList<ClickableRect>();

		public void addButton(ClickableRect button) {
			buttons.add(button);
		}

		void updateButtons() {
			for (ClickableRect button : buttons) {
				if (button.getPreferredState() == getGameState()) {
					button.update();
				}
			}
		}

		void drawButtons(Canvas canvas) {
			for (ClickableRect button : buttons) {
				if (button.getPreferredState() == getGameState()) {
					button.draw(canvas);
				}
			}
		}

		public boolean onTouchEvent(MotionEvent event) {
			boolean retVal = false;
			for (ClickableRect button : buttons) {
				if (button.getPreferredState() == getGameState()) {
					retVal |= button.touch(event.getX(), event.getY(), event);
				}
			}
			return retVal;
		}

		public void keyDown(int keyCode) {
			for (ClickableRect button : buttons) {
				if (button.enabled && button.usesKey(keyCode)) {
					button.onDown();
				}
			}
		}

		public void keyUp(int keyCode) {
			for (ClickableRect button : buttons) {
				if (button.enabled && button.usesKey(keyCode)) {
					button.onUp();
				}
			}
		}
	}

	protected ButtonManager buttonManager = new ButtonManager();

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
	
	public enum ClickableMode {
		SINGLE_CLICK,
		HOLDABLE
	}
	
	public abstract class ClickableRect {
		public int x;
		public int y;
		public int height;
		public int width;
		boolean enabled = true;
		boolean activated = false;
		ClickableMode mode = ClickableMode.SINGLE_CLICK;
		GameState preferredState;
		private ArrayList<Integer> boundKeyCodes = new ArrayList<Integer>();
		
		public ClickableRect(int x, int y, int width, int height,
				ClickableMode mode, GameState st) {
			this(x, y, width, height, mode, st, KeyEvent.KEYCODE_UNKNOWN);
		}
		
		public ClickableRect(int x, int y, int width, int height,
				ClickableMode mode, GameState st, int boundKeyCode) {
			this.boundKeyCodes.add(boundKeyCode);
			this.mode = mode;
			this.x = x;
			this.y = y;
			this.width = width;
			this.height = height;
			this.preferredState = st;
		}

		public void addKey(int keyCode) {
			boundKeyCodes.add(keyCode);
		}
		
		public boolean usesKey(int keyCode) {
			return boundKeyCodes.contains(keyCode);
		}
		
		public GameState getPreferredState() {
			return preferredState;
		}

		public void draw(Canvas canvas) {
			// Debug Draw 
			//draw.getPaint().setColor(Color.BLUE);
			//canvas.drawRect(x, y, x+width, y+height, draw.getPaint());
		}

		public void update() {
			if (activated) {
				click();
			}
		}

		public void onUp() {
			activated = false;
			up();
		}
		
		public void onDown() {
			activated = (mode.equals(ClickableMode.HOLDABLE));
			click();
		}

		public boolean touch(float eventX, float eventY, MotionEvent event) {
			boolean up = false;
			boolean down = false;
			final int action = event.getAction();
			switch (action & MotionEvent.ACTION_MASK) {
			case MotionEvent.ACTION_DOWN:
			case MotionEvent.ACTION_POINTER_DOWN: {
				down = true;
				break;
			}
			case MotionEvent.ACTION_UP:
			case MotionEvent.ACTION_POINTER_UP: {
				up = true;
				break;
			}
			}
			if (enabled && (down || up)) {
				if (down && eventY < y + height && eventX < x + width
						&& eventY > y && eventX > x) {
					onDown();
					return true;
				}
				if (up) {
					onUp();
					return true;
				}
			}
			return false;
		}

		public abstract void click();

		public void up() {
		}

		public int getX() {
			return x;
		}

		public void setX(int x) {
			this.x = x;
		}
	}

	public abstract class Dialog extends ClickableRect {

		Rect rect = new Rect();
		int fgcolor;
		int bgcolor;
		String string;

		public Dialog(int x, int y, int width, int height, String string,
				int bgcolor, int fgcolor) {
			super(x - width / 2, y - height / 2, width, height, ClickableMode.SINGLE_CLICK,
					GameState.PAUSED);
			rect.top = y - height / 2;
			rect.left = x - width / 2;
			rect.right = x + width / 2;
			rect.bottom = y + height / 2;
			this.string = string;
			this.fgcolor = fgcolor;
			this.bgcolor = bgcolor;
		}

		@Override
		public void draw(Canvas canvas) {
			draw.getPaint().setColor(Color.BLACK);
			canvas.drawRect(4 + rect.left, 4 + rect.top, 4 + rect.right,
					4 + rect.bottom, draw.getPaint());
			draw.getPaint().setColor(bgcolor);
			canvas.drawRect(rect.left, rect.top, rect.right, rect.bottom,
					draw.getPaint());
			draw.getPaint().setColor(fgcolor);
			draw.getPaint().setTextAlign(Align.CENTER);
			canvas.drawText(string, (rect.left + rect.right) / 2,
					rect.bottom - 8, draw.getPaint());
			draw.getPaint().setTextAlign(Align.LEFT);
			
			super.draw(canvas);
		}

	}

	public void blank(Canvas canvas, int black) {
		draw.getPaint().setColor(black);
		canvas.drawRect(0, 0, xMax+1, yMax+1, draw.getPaint());
	}

	public void drawText(Canvas canvas, String string, int x, int y) {
		draw.getPaint().setColor(Color.BLACK);
		canvas.drawText(string, x + 1, y + 1, draw.getPaint());

		draw.getPaint().setColor(Color.WHITE);
		canvas.drawText(string, x, y, draw.getPaint());
	}

	public abstract class BitmapButton extends ClickableRect {
		private Bitmap bitmap;
		private boolean visible;

		public BitmapButton(Bitmap bitmap, int x, int y, ClickableMode mode,
				GameState state) {
			this(bitmap, x, y, mode, state, KeyEvent.KEYCODE_UNKNOWN);
		}

		public BitmapButton(Bitmap bitmap, int x, int y, ClickableMode mode,
				GameState state, int boundKeyCode) {
			super(x, y, bitmap.getWidth(), bitmap.getHeight(), mode, state, boundKeyCode);
			this.bitmap = bitmap;
			this.x = x;
			this.y = y;
			visible = true;
			activated = false;
			preferredState = state;
			this.height = bitmap.getHeight();
			this.width = bitmap.getWidth();
		}

		@Override
		public void draw(Canvas canvas) {
			if (visible) {
				if (activated) {
					draw.getPaint().setAlpha(128);
				}
				canvas.drawBitmap(bitmap, x, y, draw.getPaint());
				draw.getPaint().setAlpha(255);
			}
		}

		public boolean isVisible() {
			return visible;
		}

		public void setVisible(boolean visible) {
			this.visible = visible;
			this.enabled = visible;
		}

		public Bitmap getBitmap() {
			return bitmap;
		}

		public void setBitmap(Bitmap bitmap) {
			this.bitmap = bitmap;
		}
	}

	public class SoundManager {
		private SoundPool mSoundPool;
		private HashMap<Integer, Integer> mSoundPoolMap;
		private AudioManager mAudioManager;
		private Context mContext;

		public void initSounds(Context theContext) {
			mContext = theContext;
			mSoundPool = new SoundPool(4, AudioManager.STREAM_MUSIC, 0);
			mSoundPoolMap = new HashMap<Integer, Integer>();
			mAudioManager = (AudioManager) mContext
					.getSystemService(Context.AUDIO_SERVICE);
		}

		public void playSound(int index) {
			playSound(index, 1f);
		}

		public void playSound(int index, float rate) {
			float streamVolume = mAudioManager
					.getStreamVolume(AudioManager.STREAM_MUSIC);
			streamVolume = streamVolume
					/ mAudioManager
							.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
			mSoundPool.play(mSoundPoolMap.get(index), streamVolume,
					streamVolume, 1, 0, rate);
		}

		public void playLoopedSound(int index) {
			float streamVolume = mAudioManager
					.getStreamVolume(AudioManager.STREAM_MUSIC);
			streamVolume = streamVolume
					/ mAudioManager
							.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
			mSoundPool.play(mSoundPoolMap.get(index), streamVolume,
					streamVolume, 1, -1, 1f);
		}

		public void addSound(int index, int SoundID) {
			mSoundPoolMap.put(index, mSoundPool.load(mContext, SoundID, 1));
		}
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
	}

	protected float delta;
	protected long currentTime;
	private long lastUpdateTime;

	public void updateDelta() {
		this.currentTime = System.currentTimeMillis();
		this.delta = Math.min(0.15f, (currentTime - lastUpdateTime) / 1000.0f);
		this.lastUpdateTime = currentTime;
	}

	static class RenderThread extends Thread {
		private SurfaceHolder surfaceHolder;
		private GameView view;
		private boolean run = false;

		public RenderThread(SurfaceHolder surfaceHolder, GameView panel) {
			this.surfaceHolder = surfaceHolder;
			view = panel;
		}

		public void setRunning(boolean run) {
			this.run = run;
		}

		Exception eTest;

		@Override
		public void run() {
			Canvas c;
			while (run) {
				c = null;
				try {
					c = surfaceHolder.lockCanvas(null);
					synchronized (surfaceHolder) {
						view.onDraw(c);
					}
				} finally {
					// do this in a finally so that if an exception is thrown
					// during the above, we don't leave the Surface in an
					// inconsistent state
					if (c != null) {
						surfaceHolder.unlockCanvasAndPost(c);
					}
				}
			}
		}
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

	protected SoundManager soundManager = new SoundManager();

	public enum AnimMode {
		ONCE, LOOPED, PINGPONG, SEQUENCE, PINGPONG_SEQUENCE
	};

	public abstract class NextAnim {
		public abstract void onEnd(AnimatedSprite prev);
	}

	public class AnimatedSprite {
		private Bitmap mAnimation;
		public Point2D pos;
		private Rect mSRectangle;
		private float mFPS;
		private int mNoOfFrames;
		private int mNoOfTracks;
		private int mCurrentFrame;
		private float mFrameTimer;
		private int mSpriteHeight;
		private int mSpriteWidth;
		private int mDirection;
		private AnimMode mMode;
		private boolean mFlip;
		private NextAnim mSequence;
		private int mTrack;
		private float mScale;

		public void start(AnimMode mode) {
			start(mode, null);
		}

		public void setTrack(int track) {
			mTrack = Math.min(track, mNoOfTracks - 1);
			mSRectangle.top = mTrack * mSpriteHeight;
			mSRectangle.bottom = mSRectangle.top + mSpriteHeight;
		}
		
		public void start(AnimMode mode, NextAnim anim) {
			if (mode.equals(AnimMode.SEQUENCE) && anim == null) {
				mode = AnimMode.ONCE;
			}
			mMode = mode;
			mSequence = anim;
			mCurrentFrame = 0;
		}

		// Looped or PingPong
		public AnimatedSprite(Bitmap theBitmap, int theFPS, int theFrameCount,
				AnimMode mode) {
			this(theBitmap, theFPS, theFrameCount, 1, mode);
		}

		public AnimatedSprite(Bitmap theBitmap, int theFPS, int theXFrameCount, int theYFrameCount,
			AnimMode mode) {
			mScale = 1.0f;
			mTrack = 0;
			mSequence = null;
			mSRectangle = new Rect(0, 0, 0, 0);
			mFrameTimer = 0;
			mDirection = 1;
			mCurrentFrame = 0;
			pos = new Point2D(80, 200);
			mAnimation = theBitmap;
			mSpriteHeight = theBitmap.getHeight() / theYFrameCount;
			mSpriteWidth = theBitmap.getWidth() / theXFrameCount;
			mSRectangle.top = 0;
			mSRectangle.bottom = mSpriteHeight;
			mSRectangle.left = 0;
			mSRectangle.right = mSpriteWidth;
			mFPS = 1.0f / theFPS;
			mNoOfFrames = theXFrameCount;
			mNoOfTracks = theYFrameCount;
			mMode = mode;
			mFlip = false;
		}

		public void Update(float delta) {
			mFrameTimer += delta;
			if (mFrameTimer >= mFPS) {
				mFrameTimer = 0.0f;
				mCurrentFrame += mDirection;

				if (mCurrentFrame >= mNoOfFrames || mCurrentFrame < 0) {
					switch (mMode) {
					case LOOPED:
						mCurrentFrame = 0;
						break;
					case PINGPONG:
						mDirection *= -1;
						mCurrentFrame += (mDirection * 2);
						break;
					case ONCE:
						mCurrentFrame = mNoOfFrames - 1;
						mDirection = 0;
						break;
					case SEQUENCE:
						mCurrentFrame = 0;
						mSequence.onEnd(this);
						break;
					case PINGPONG_SEQUENCE:
						if (mCurrentFrame >= mNoOfFrames) {
							mDirection *= -1;
							mCurrentFrame += (mDirection * 2);
						} else {
							mCurrentFrame = 0;
							mDirection = 1;
							mSequence.onEnd(this);
						}
					}
				}
			}

			int left = mCurrentFrame * mSpriteWidth;
			int right = left + mSpriteWidth;
			mSRectangle.left = left; // mFlip?right:left;
			mSRectangle.right = right; // mFlip?left:right;
		}

		public void draw(Canvas canvas) {
			if (mFlip) {
				int left = -pos.getX() - (int) (mSpriteWidth * mScale);
				int right = -pos.getX();
				Rect dest = new Rect(left, pos.getY(), right, pos.getY()
						+ (int) (mSpriteHeight * mScale));
				canvas.save();
				canvas.scale(-1.0f, 1.0f);
				canvas.drawBitmap(mAnimation, mSRectangle, dest, null);
				canvas.restore();
			} else {
				int left = pos.getX();
				int right = pos.getX() + (int) (mSpriteWidth * mScale);
				Rect dest = new Rect(left, pos.getY(), right, pos.getY()
						+ (int) (mSpriteHeight * mScale));
				canvas.drawBitmap(mAnimation, mSRectangle, dest, null);
			}
		}

		public void flip() {
			mFlip = !mFlip;
		}

		public void setFlip(boolean b) {
			mFlip = b;
		}

		public int getSpriteWidth() {
			return mSpriteWidth;
		}
		
		public int getSpriteHeight() {
			return mSpriteHeight;
		}

		public void setScale(float f) {
			this.mScale = f;
		}

		public void setFrame(int i) {
			mCurrentFrame = i;
		}
	}

	public GameView(Context context) {
		super(context);

		soundManager.initSounds(context);

		getHolder().addCallback(this);
		thread = new RenderThread(getHolder(), this);
		draw = new Draw();

		// To enable touch mode
		this.setFocusableInTouchMode(true);
	}

	protected RenderThread thread;
	protected Draw draw;
	protected int xMax;
	protected int yMax;
	protected Random rand = new Random();

	// Touch-input handler
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (buttonManager.onTouchEvent(event)) {
			return true;
		}
		return false;
	}

	public enum GameState {
		PAUSED, RUNNING,
	}

	private GameState gameState = GameState.PAUSED;

	// Called back when the view is first created or its size changes.
	@Override
	public void onSizeChanged(int w, int h, int oldW, int oldH) {
		// Set the movement bounds for the ball
		this.xMax = w - 1;
		this.yMax = h - 1;
		this.reinitGame();
	}

	@Override
	protected void onDraw(Canvas canvas) {
		if (!gameInitialized) {
			gameInitialized = true;
			initGame();
		}

		// Update the position of the ball, including collision detection and
		// reaction.
		buttonManager.updateButtons();
		this.updateGame();

		this.drawGame(canvas);
		buttonManager.drawButtons(canvas);
	}

	public abstract void drawGame(Canvas canvas);

	public abstract void updateGame();

	public abstract void reinitGame();

	boolean gameInitialized = false;

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

	public GameState getGameState() {
		return gameState;
	}

	public void setGameState(GameState gameState) {
		this.gameState = gameState;
	}

	public void newGame() {
	}

	public void loadGame(Activity activity) {
		SharedPreferences settings = activity.getPreferences(0);
		loadGame(settings);
	}

}
