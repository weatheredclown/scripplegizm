package com.scripplegizm.tablettest;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.os.Bundle;
import android.view.Window;

import com.scripplegizm.gameutils.GameView;

public class TabletTestActivity extends Activity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        GameView gv = new GameView(this) {
        	Bitmap box32 = BitmapFactory.decodeResource(getResources(), R.drawable.boxpurple);
        	Bitmap box64 = BitmapFactory.decodeResource(getResources(), R.drawable.boxred);
        	Bitmap box128 = BitmapFactory.decodeResource(getResources(), R.drawable.boxblue);

			@Override
			public void drawGame(Canvas canvas) {
				// TODO Auto-generated method stub
				for (int i=0; i<xMax/2; i+=33) {
					for (int j=0; j<yMax/2; j+=33) {
						canvas.drawBitmap(box32, i, j, draw.getPaint());
					}
				}
				for (int i=xMax/2; i<xMax; i+=65) {
					for (int j=yMax/2; j<yMax; j+=65) {
						canvas.drawBitmap(box64, i, j, draw.getPaint());
					}
				}
				for (int i=0; i<xMax/2; i+=129) {
					for (int j=yMax/2; j<yMax; j+=129) {
						canvas.drawBitmap(box128, i, j, draw.getPaint());
					}
				}
			}

			@Override
			public void updateGame() {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void reinitGame() {
				// TODO Auto-generated method stub
				//box32.setDensity(DisplayMetrics.DENSITY_HIGH);
			}
        };
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(gv);
    }
}