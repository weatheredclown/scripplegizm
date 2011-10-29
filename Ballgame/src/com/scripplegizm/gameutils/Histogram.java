package com.scripplegizm.gameutils;

import java.util.ArrayList;

import android.graphics.Canvas;
import android.graphics.Color;

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
		GameView.draw.getPaint().setColor(Color.WHITE);
		for (int i = 0; i < 50; i++) {
			int magic = (i + lastX) % 50;
			canvas.drawRect(pos.getX() + i * 5, pos.getY() - lastValue.get(magic),
					pos.getX() + 5 + i * 5, pos.getY(), GameView.draw.getPaint());
		}
	}
	public void updateValue(int val) {
		lastValue.set(lastX++, val);
		lastX = lastX % 50;
	}
}
