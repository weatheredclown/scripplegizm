package com.scripplegizm.ballgame;

import android.app.Activity;
import android.graphics.Canvas;
import android.os.Bundle;

import com.scripplegizm.gameutils.GameView;

public class BallgameActivity extends Activity {

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		GameView view = new GameView(this) {

			@Override
			public void drawGame(Canvas canvas) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void updateGame() {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void reinitGame() {
				// TODO Auto-generated method stub
				
			}

		};
		setContentView(view);
	}
}