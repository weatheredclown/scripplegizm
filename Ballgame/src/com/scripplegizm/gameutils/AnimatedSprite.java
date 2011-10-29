package com.scripplegizm.gameutils;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;

public class AnimatedSprite {
	public enum AnimMode {
		ONCE, LOOPED, PINGPONG, SEQUENCE, PINGPONG_SEQUENCE
	};

	public static abstract class NextAnim {
		public abstract void onEnd(AnimatedSprite prev);
	}
	
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
