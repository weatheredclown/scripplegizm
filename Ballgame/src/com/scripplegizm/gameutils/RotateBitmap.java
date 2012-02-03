package com.scripplegizm.gameutils;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;

public class RotateBitmap {
	private boolean scaled = false;
	private int targetXSize = 32;
	private int targetYSize = 32;
	private Bitmap bitmap = null;
	private float rotation = 0.0f;
	Matrix matrix = new Matrix();

	public RotateBitmap() {
	}

	public void rotate(float degrees) {
		setRotation(getRotation() + degrees);
		setRotation(getRotation() % 360);
		matrix.setRotate(getRotation(), getBitmap().getWidth() / 2,
				getBitmap().getHeight() / 2);
	}

	public void draw(Canvas canvas, int x, int y) {
		Matrix composite = new Matrix();
		composite.reset();
		int width = getBitmap().getWidth();
		int height = getBitmap().getHeight();
		if (isScaled()) {
			float xScale = getTargetXSize() / (float) width;
			float yScale = getTargetYSize() / (float) height;
			// Log.i("TARGET", "x: " + targetXSize + ", y:" + targetYSize);
			width = getTargetXSize();
			height = getTargetYSize();
			composite.postScale(xScale, yScale, 0, 0);
		}
		composite.postRotate(getRotation(), width / 2, height / 2);
		composite.postTranslate(x, y);
		// Log.i("ROTATE", "" + rotation);
		// composite.postRotate(rotation, width / 2, height / 2);
		// Log.i("TRANSLATE", "x: " + x + ", y: " + y);
		canvas.drawBitmap(getBitmap(), composite, null);
	}

	public Bitmap getBitmap() {
		return bitmap;
	}

	public void setBitmap(Bitmap bitmap) {
		this.bitmap = bitmap;
	}

	public boolean isScaled() {
		return scaled;
	}

	public void setScaled(boolean scaled) {
		this.scaled = scaled;
	}

	public int getTargetXSize() {
		return targetXSize;
	}

	public void setTargetXSize(int targetXSize) {
		this.targetXSize = targetXSize;
	}

	public int getTargetYSize() {
		return targetYSize;
	}

	public void setTargetYSize(int targetYSize) {
		this.targetYSize = targetYSize;
	}

	public float getRotation() {
		return rotation;
	}

	public void setRotation(float rotation) {
		this.rotation = rotation;
	}
}
