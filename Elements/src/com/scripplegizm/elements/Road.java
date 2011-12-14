package com.scripplegizm.elements;

import com.scripplegizm.gameutils.GameView;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.util.Log;

class Road extends GameObject {
	Road roadForOtherCell;
	WorldCell a; // must be owned by the placing player
	WorldCell b;
	Blob curObject; // object traversing the road

	Road(WorldColor c, WorldCell a, WorldCell b, PlayerData playerdata) {
		super(c, playerdata);
		this.a = a;
		this.b = b;
		curObject = null;
	}

	@Override
	public void update() {
		if (curObject == null) {
			return;
		}

		WorldCell pOtherCell = null;
		WorldCell pThisCell = null;
		if (curObject.currentCell == a) {
			pOtherCell = b;
			pThisCell = a;
		} else {
			pOtherCell = a;
			pThisCell = b;
		}
		curObject.onMove(pThisCell, pOtherCell);

		// remove from cur cell list
		WorldCell.threadCheck();
		pThisCell.objects.remove(curObject);
		pOtherCell.objects.add(curObject);
		curObject.setCurrentCell(pOtherCell);
		Log.i("_", "**** moved " + curObject.getName() + " " + curObject
				+ " to cell " + pOtherCell);
		curObject = null;
	}

	@Override
	public void draw(Canvas canvas) {
		Paint paint = GameView.draw.getPaint();
		paint.setStyle(Style.FILL_AND_STROKE);
		paint.setColor(getColor().colorValue);
		canvas.drawRect(x - 1, y - 3, x + 1, y + 3, paint);
		if (curObject != null) {
			canvas.drawRect(x - 3, y - 1, x + 3, y + 1, paint);
		}
	}

	static final int cost = 6;

	@Override
	String getName() {
		return "ROAD " + (curObject == null ? "" : curObject.getName());
	}

	@Override
	public void onRemove() {
		if (roadForOtherCell != null) {
			roadForOtherCell.roadForOtherCell = null;
			roadForOtherCell.readyForDeletion = true;
		}
	}

	@Override
	int getCost() {
		return cost;
	}

	public void connectCells() {
		a.addObject(this);
		a.addRoad(this);

		roadForOtherCell = new Road(color, b, a, playerData);
		roadForOtherCell.roadForOtherCell = this;
		b.addObject(roadForOtherCell);
		b.addRoad(roadForOtherCell);
	}

	@Override
	public boolean onClick(GameObject prevObject) {
		if (prevObject != null) {
			if (prevObject instanceof Blob) {
				Blob blob = (Blob) prevObject;
				if (blob.currentCell == a) {
					blob.currentCell.Travel(blob, b);
					return false;
				} else if (blob.currentCell == b) {
					blob.currentCell.Travel(blob, a);
					return false;
				}
			}
		}
		return true;
	}
};
