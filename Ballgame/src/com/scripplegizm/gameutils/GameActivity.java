package com.scripplegizm.gameutils;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.Window;

public abstract class GameActivity extends Activity {

	protected GameView gameView;
	
	public abstract GameView createGameView();

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.i("CREATE", "onCreate");
		super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
		gameView = createGameView();
		gameView.activity = this;
		gameView.loadGame(this);
		setContentView(gameView);
	}

	@Override
	protected void onStop() {
		Log.i("STOP", "onStop");
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
