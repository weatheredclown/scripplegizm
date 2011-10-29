package com.scripplegizm.animationtest;

import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.scripplegizm.gameutils.GameActivity;
import com.scripplegizm.gameutils.GameView;

public class AnimationTestActivity extends GameActivity {
	AnimationGameView gm;

	@Override
	public GameView createGameView() {
		gm = new AnimationGameView(this);
		return gm;
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
		case R.id.new_game:
			gm.newGame();
			return true;
		case R.id.toggle_map:
			gm.toggleMap();
		default:
			return super.onOptionsItemSelected(item);
		}
	}
}
