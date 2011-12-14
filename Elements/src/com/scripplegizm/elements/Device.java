package com.scripplegizm.elements;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.RectF;

public class Device extends GameObject {

	private static final int BUILDING_HALF_HEIGHT = 7;
	private static final int BUILDING_HALF_WIDTH = 5;

	Device(WorldColor c, PlayerData playerdata) {
		super(c, playerdata);
	}

	String getName() {
		return "DEVICE";
	}

	@Override
	public void draw(Canvas canvas) {
		Paint paint = ElementsView.draw.getPaint();
		paint.setStyle(Style.STROKE);
		paint.setColor(getColor().colorValue);
		RectF rect = new RectF(x - BUILDING_HALF_WIDTH, y
				- BUILDING_HALF_HEIGHT, x + BUILDING_HALF_WIDTH, y
				+ BUILDING_HALF_HEIGHT);
		canvas.drawRoundRect(rect, BUILDING_HALF_WIDTH + 1,
				BUILDING_HALF_HEIGHT + 1, paint);
	}

	public static class FoodDuplicator extends Device {

		public static final int COST = 20;
		public static final int MAX_DUPLICATE = 50;
		WorldCell currentCell;
		int duplicateCount = 0;

		@Override
		int getCost() {
			return COST;
		}

		@Override
		public void draw(Canvas canvas) {
			super.draw(canvas);
			ElementsView.draw.circle(canvas, x, y, 3,
					playerData.getFoodColor().colorValue, true);
		}

		FoodDuplicator(WorldColor c, PlayerData playerdata, WorldCell cell) {
			super(c, playerdata);
			currentCell = cell;
		}

		String getName() {
			return "FOOD_DUPLICATOR";
		}

		@Override
		public void doAction(int action) {
			switch (action) {
			case 1:
				WorldColor foodColor = playerData.getFoodColor();
				duplicateCount += currentCell.produce(foodColor, Math.min(
						currentCell.getColorAvailable(foodColor), MAX_DUPLICATE
								- duplicateCount));
				if (duplicateCount == MAX_DUPLICATE) {
					this.readyForDeletion = true;
					ElementsView.showMessage("POOF!");
				}
				break;
			}
		}

		@Override
		public String getActionCaption(int action) {
			switch (action) {
			case 1:
				return "Duplicate!!1!!";
			}
			return null;
		}
	}

	public static class ToxinIncinerator extends Device {

		public static final int COST = 20;
		public static final int MAX_INCINERATE = 50;
		WorldCell currentCell;
		int incinerateCount = MAX_INCINERATE;

		@Override
		int getCost() {
			return COST;
		}

		@Override
		public void draw(Canvas canvas) {
			super.draw(canvas);
			ElementsView.draw.circle(canvas, x, y, 3,
					playerData.getToxicColor().colorValue, true);
		}

		ToxinIncinerator(WorldColor c, PlayerData playerdata, WorldCell cell) {
			super(c, playerdata);
			currentCell = cell;
		}

		String getName() {
			return "TOXIN_INCINERATOR";
		}

		@Override
		public void doAction(int action) {
			switch (action) {
			case 1:
				incinerateCount -= currentCell.consume(
						playerData.getToxicColor(), incinerateCount);
				if (incinerateCount == 0) {
					this.readyForDeletion = true;
					ElementsView.showMessage("POOF!");
				}
				break;
			}
		}

		@Override
		public String getActionCaption(int action) {
			switch (action) {
			case 1:
				return "Incincerate!!1!!";
			}
			return null;
		}
	}
}
