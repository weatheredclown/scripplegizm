package com.scripplegizm.gameutils;

import java.util.ArrayList;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint.Style;
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

	public boolean visible = false;
	public String caption = "";
	public int yTextOffset = 35;
	public int color = Color.BLUE;

	public void draw(Canvas canvas) {
		if (visible) {
			GameView.draw.getPaint().setColor(color);
			GameView.draw.getPaint().setStyle(Style.STROKE);
			canvas.drawRect(x, y, x + width, y + height,
					GameView.draw.getPaint());
			GameView.draw.getPaint().setColor(Color.WHITE);
			// GameView.draw.getPaint().getTextSize();
			canvas.drawText(caption, x+10, y + yTextOffset , GameView.draw.getPaint());
		}
	}

	boolean clickPending = false;
	boolean upPending = false;
	public void update() {
		if (activated) {
			click();
		}

		if (clickPending) {
			clickPending = false;
			click();
		}

		if (upPending) {
			upPending = false;
			up();
		}
	}

	public void onUp() {
		activated = false;
		upPending = true;
	}

	public void onDown() {
		activated = (mode.equals(ClickableMode.HOLDABLE));
		clickPending = !activated;
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
			if (down && eventY < y + height && eventX < x + width && eventY > y
					&& eventX > x) {
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

	public void setCaption(String string) {
		caption = string;
	}
}
