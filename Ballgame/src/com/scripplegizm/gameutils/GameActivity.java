package com.scripplegizm.gameutils;

import android.app.Activity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.Window;

public abstract class GameActivity extends Activity {

	protected GameView gameView;
	
	public abstract GameView createGameView();

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
		gameView = createGameView();
		gameView.loadGame(this);
		setContentView(gameView);
	}

	@Override
	protected void onStop() {
		super.onStop();
		gameView.saveGame(this);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (!gameView.onOptionsItemSelected(item.getItemId())) {
			return super.onOptionsItemSelected(item);
		}
		return true;
	}
}
