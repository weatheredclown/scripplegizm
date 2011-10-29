package com.scripplegizm.gameutils;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.Paint.Align;
import android.view.KeyEvent;

import com.scripplegizm.gameutils.GameView.ClickableMode;
import com.scripplegizm.gameutils.GameView.GameState;

public class Clickables {
	public static abstract class Dialog extends ClickableRect {

		Rect rect = new Rect();
		int fgcolor;
		int bgcolor;
		String string;

		public Dialog(int x, int y, int width, int height, String string,
				int bgcolor, int fgcolor) {
			super(x - width / 2, y - height / 2, width, height,
					ClickableMode.SINGLE_CLICK, GameState.PAUSED);
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
			GameView.draw.getPaint().setColor(Color.BLACK);
			canvas.drawRect(4 + rect.left, 4 + rect.top, 4 + rect.right,
					4 + rect.bottom, GameView.draw.getPaint());
			GameView.draw.getPaint().setColor(bgcolor);
			canvas.drawRect(rect.left, rect.top, rect.right, rect.bottom,
					GameView.draw.getPaint());
			GameView.draw.getPaint().setColor(fgcolor);
			GameView.draw.getPaint().setTextAlign(Align.CENTER);
			canvas.drawText(string, (rect.left + rect.right) / 2,
					rect.bottom - 8, GameView.draw.getPaint());
			GameView.draw.getPaint().setTextAlign(Align.LEFT);

			super.draw(canvas);
		}

	}

	public static abstract class BitmapButton extends ClickableRect {
		private Bitmap bitmap;
		private boolean visible;

		public BitmapButton(Bitmap bitmap, int x, int y, ClickableMode mode,
				GameState state) {
			this(bitmap, x, y, mode, state, KeyEvent.KEYCODE_UNKNOWN);
		}

		public BitmapButton(Bitmap bitmap, int x, int y, ClickableMode mode,
				GameState state, int boundKeyCode) {
			super(x, y, bitmap.getWidth(), bitmap.getHeight(), mode, state,
					boundKeyCode);
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
					GameView.draw.getPaint().setAlpha(128);
				}
				canvas.drawBitmap(bitmap, x, y, GameView.draw.getPaint());
				GameView.draw.getPaint().setAlpha(255);
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
}
