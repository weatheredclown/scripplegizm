package com.scripplegizm.elements;

import java.util.ArrayList;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.util.Log;

import com.scripplegizm.elements.Blob.FactoryWorker;

class Structure extends GameObject {
	Structure(PlayerData playerdata) {
		super(playerdata.getStructureColor(), playerdata);
	}

	final int BUILDING_HALF_HEIGHT = 3;

	@Override
	public void draw(Canvas canvas) {
		Paint paint = ElementsView.draw.getPaint();
		paint.setStyle(Style.STROKE);
		paint.setColor(getColor().colorValue);
		canvas.drawRect(x - BUILDING_HALF_HEIGHT, y - BUILDING_HALF_HEIGHT, x
				+ BUILDING_HALF_HEIGHT, y + BUILDING_HALF_HEIGHT, paint);
	}

	@Override
	String getName() {
		return "STRUCTURE";
	}

	@Override
	boolean isStructure() {
		return true;
	}

	static final int STOREHOUSE_CAPACITY = 20;

	static class Storehouse extends Structure {
		WorldColor storageColor;

		WorldColor GetStorageColor() {
			return storageColor;
		}

		Storehouse(WorldColor c, PlayerData playerdata) {
			super(playerdata);
			storageColor = c;
		}

		static final int COST = 5;

		@Override
		public void draw(Canvas canvas) {
			Paint paint = ElementsView.draw.getPaint();
			paint.setStyle(Style.FILL);
			paint.setColor(storageColor.colorValue);
			canvas.drawRect(x - BUILDING_HALF_HEIGHT, y - BUILDING_HALF_HEIGHT,
					x + BUILDING_HALF_HEIGHT, y + BUILDING_HALF_HEIGHT, paint);
			super.draw(canvas);
		}

		@Override
		String getName() {
			return "STOREHOUSE: " + playerData.getColorRoleName(storageColor);
		}

		@Override
		int getCost() {
			return COST;
		}

		@Override
		int getCapacity(WorldColor c) {
			if (c == storageColor)
				return STOREHOUSE_CAPACITY;
			return 0;
		}
	}

	static class Factory extends Structure {
		@Override
		public String getName() {
			String retVal = "FACTORY";
			if (input1 != WorldColor.NONE && input2 != WorldColor.NONE) {
				retVal += " - " + playerData.getColorRoleName(input1) + "+"
						+ playerData.getColorRoleName(input2) + "="
						+ playerData.getColorRoleName(output);
			}
			return retVal;
		}

		@Override
		public void draw(Canvas canvas) {
			Paint paint = ElementsView.draw.getPaint();
			if (workers.size() > 0) {
				paint.setStyle(Style.FILL);
				paint.setColor(playerData.getLivingColor().colorValue);
				canvas.drawRect(x - BUILDING_HALF_HEIGHT, y
						- BUILDING_HALF_HEIGHT, x + BUILDING_HALF_HEIGHT, y
						+ BUILDING_HALF_HEIGHT, paint);
			}
			super.draw(canvas);
			paint.setStyle(Style.FILL_AND_STROKE);
			paint.setColor(input1.colorValue);
			canvas.drawRect(x - BUILDING_HALF_HEIGHT * 3, y
					- BUILDING_HALF_HEIGHT * 3, x - BUILDING_HALF_HEIGHT, y
					- BUILDING_HALF_HEIGHT, paint);
			paint.setColor(input2.colorValue);
			canvas.drawRect(x + BUILDING_HALF_HEIGHT, y - BUILDING_HALF_HEIGHT
					* 3, x + BUILDING_HALF_HEIGHT * 3,
					y - BUILDING_HALF_HEIGHT, paint);
			paint.setColor(output.colorValue);
			canvas.drawRect(x - BUILDING_HALF_HEIGHT, 1 + y
					+ BUILDING_HALF_HEIGHT, x + BUILDING_HALF_HEIGHT + 1, 2 + y
					+ BUILDING_HALF_HEIGHT * 3, paint);
		}

		ArrayList<FactoryWorker> workers = new ArrayList<FactoryWorker>();

		public final int MAX_WORKERS = 5;
		static final int COST = 9;

		int getCost() {
			return COST;
		}

		@Override
		public boolean onClick(GameObject prevObject) {
			if (prevObject != null) {
				if (prevObject instanceof FactoryWorker
						&& this.workers.isEmpty()) {
					FactoryWorker blob = (FactoryWorker) prevObject;
					blob.SetCurrentFactory(this);
					return false;
				}
			}
			return true;
		}

		void AddWorker(FactoryWorker worker) {
			if (workers.size() < MAX_WORKERS) {
				WorldColor toxic = worker.playerData.getToxicColor();
				if (output == toxic || input1 == toxic || input2 == toxic) {
					Log.i("_", "Working in " + this + " will kill " + worker);
					worker.readyForDeletion = true;
				}
				workers.add(worker);
			}
			Log.i("_", "add worker " + worker + ": " + workers);
		}

		void RemoveWorker(FactoryWorker worker) {
			workers.remove(worker);
			Log.i("_", "remove worker " + worker + ": " + workers);
		}

		WorldColor input1, input2;
		WorldColor output;

		public Factory(WorldColor input1, WorldColor input2, WorldColor output,
				PlayerData playerData) {
			super(playerData);
			this.input1 = input1;
			this.input2 = input2;
			this.output = output;
		}

		public boolean canProduce(WorldCell cell) {
			return input1 != WorldColor.NONE && input2 != WorldColor.NONE
					&& cell.isResourceAvailable(input1, 2)
					&& cell.isResourceAvailable(input2, 2);
		}
	}
}
