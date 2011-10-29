package com.scripplegizm.gameutils;

import java.util.ArrayList;
import java.util.List;

import android.graphics.Canvas;
import android.view.MotionEvent;

public class ButtonManager {
	List<ClickableRect> buttons = new ArrayList<ClickableRect>();

	public void addButton(ClickableRect button) {
		buttons.add(button);
	}

	void updateButtons() {
		for (ClickableRect button : buttons) {
			if (button.getPreferredState() == GameView.getGameState()) {
				button.update();
			}
		}
	}

	void drawButtons(Canvas canvas) {
		for (ClickableRect button : buttons) {
			if (button.getPreferredState() == GameView.getGameState()) {
				button.draw(canvas);
			}
		}
	}

	public boolean onTouchEvent(MotionEvent event) {
		boolean retVal = false;
		for (ClickableRect button : buttons) {
			if (button.getPreferredState() == GameView.getGameState()) {
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
