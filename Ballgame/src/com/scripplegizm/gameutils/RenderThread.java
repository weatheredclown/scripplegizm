package com.scripplegizm.gameutils;

import android.graphics.Canvas;
import android.view.SurfaceHolder;

class RenderThread extends Thread {
	private SurfaceHolder surfaceHolder;
	private GameView view;
	private boolean run = false;

	public RenderThread(SurfaceHolder surfaceHolder, GameView panel) {
		this.surfaceHolder = surfaceHolder;
		view = panel;
	}

	public void setRunning(boolean run) {
		this.run = run;
	}

	Exception eTest;

	@Override
	public void run()  {
		Canvas c;
		while (run) {
			c = null;
			try {
				c = surfaceHolder.lockCanvas(null);
				synchronized (surfaceHolder) {
					view.onDraw(c);
				}
			} catch(Exception e) {
				e.printStackTrace();
			} finally {
				// do this in a finally so that if an exception is thrown
				// during the above, we don't leave the Surface in an
				// inconsistent state
				if (c != null) {
					surfaceHolder.unlockCanvasAndPost(c);
				}
			}
		}
	}
}
