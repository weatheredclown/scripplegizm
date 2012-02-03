package com.scripplegizm.spritetest;

import com.scripplegizm.gameutils.GameActivity;
import com.scripplegizm.gameutils.GameView;

public class SpriteTestActivity extends GameActivity {

    GameView gv;

	@Override
	public GameView createGameView() {
	    gv = new SpriteGameView(this);
	    return gv;
	}
}
