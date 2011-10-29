package com.scripplegizm.gameutils;

import java.util.ArrayList;

import android.graphics.Canvas;
import android.view.KeyEvent;
import android.view.MotionEvent;

import com.scripplegizm.gameutils.GameView.ClickableMode;
import com.scripplegizm.gameutils.GameView.GameState;

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
