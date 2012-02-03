package com.scripplegizm.oneredpeg;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint.Style;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;

import com.scripplegizm.gameutils.GameActivity;
import com.scripplegizm.gameutils.GameView;
import com.scripplegizm.gameutils.Point2D;

public class OneRedPegActivity extends GameActivity {
	private final class OneRedPegView extends GameView {
		abstract class AudioTrack {
			int audioId;
			boolean special = false;

			public AudioTrack(int id) {
				audioId = id;
			}

			void play(float rate) {
				soundManager.playSound(audioId, rate);
			}

			abstract boolean shouldPlay();
		}

		private static final float COLOR_BOOST = 0.5f;
		static final float COOLDOWN_TIME = 4.0f;
		ArrayList<AudioTrack> audioTracks = new ArrayList<AudioTrack>();
		int BOARD_OFFSET_X = 20;
		int BOARD_OFFSET_Y = 20;
		float bonusAmount = 0.0f;
		final float BOOST_TIME = 0.1f;
		boolean boostChanged = false;
		int challenge = 1;
		ArrayList<Point2D> directions = new ArrayList<Point2D>();
		float doBoostTime = 0.0f;
		int gameHiLevel = 0; // Not the highest level reached, but the level
								// associated with the high score.
		int gameHiScore = 0;
		int score = 0;
		float lastSine = 0.0f;
		Point2D lastTouchPoint = new Point2D(0, 0);
		Point2D movedTouchPoint = new Point2D(0, 0);
		ParticleSystem particle = null;
		int PEG_SIZE = 10;
		int PEG_SPACING = 1;
		List<Point2D> points = new ArrayList<Point2D>();
		int redPegsStillNeeded = 0;
		Point2D selectedPeg = new Point2D(-1, -1);
		float sineCount = 0.0f;
		boolean slide = true;
		boolean sliding = false;
		SineState state = SineState.PEAK;
		float tally = 0.0f;

		OneRedPegView(Context context) {
			super(context);
		}

		void addScore(int i) {
			score += i;
			if (score > gameHiScore) {
				gameHiScore = score;
				gameHiLevel = numWins;
			}
		}

		void clearPegs() {
			for (int i = 0; i < GRID_SIZE; i++) {
				for (int j = 0; j < GRID_SIZE; j++) {
					setPeg(i, j, null);
				}
			}
		}

		public void combinePegs(int newX, int newY, int prevX, int prevY) {
			int color = pegs[prevX][prevY].ordinal();
			color++;
			float bonusMulti = Math.max(1, bonusAmount * 10);
			setPeg(prevX, prevY, null);
			PegColors newColor = null;
			if (color < PegColors.values().length) {
				newColor = PegColors.values()[color];
				addScore((int) (color * bonusMulti));
			} else {
				particle.start(lastTouchPoint.getX(), lastTouchPoint.getY());
				addScore((int) (100 * bonusMulti));
			}
			setPeg(newX, newY, newColor);
			selectedPeg.set(-1, -1);
			bonusAmount = Math.min(bonusAmount + COLOR_BOOST, 1.0f);
			this.bonusDecreaseDelay = BONUS_DECREASE_DELAY;
			if (PegColors.PEG_RED.equals(pegs[newX][newY])) {
				redPegsStillNeeded--;
				if (redPegsStillNeeded == 0) {
					winGame();
				}
			} else if (!validateBoard()) {
				loseGame();
			}
			return;
		}

		public void createDirections() {
			directions.clear();
			directions.add(new Point2D(-0, -1));
			directions.add(new Point2D(-0, +1));
			directions.add(new Point2D(-1, -0));
			directions.add(new Point2D(+1, -0));
			if (allowDiagonal) {
				directions.add(new Point2D(-1, -1));
				directions.add(new Point2D(+1, -1));
				directions.add(new Point2D(-1, +1));
				directions.add(new Point2D(+1, +1));
			}
		}

		void createGame() {
			Log.i("", "createGame");
			createDirections();
			setGameState(GameState.RUNNING);
			clearPegs();
			redPegsStillNeeded = 0;

			for (int i = 0; i < challenge; i++) {
				int startX = rand.nextInt(GRID_SIZE);
				int startY = rand.nextInt(GRID_SIZE);
				if (isViable(startX, startY)) {
					setPeg(startX, startY, PegColors.PEG_RED);
					points.clear();
					if (splitPeg(startX, startY)) {
						redPegsStillNeeded++;
						// break;
					} else {
						setPeg(startX, startY, null);
					}
				}
			}
			if (redPegsStillNeeded == 0) {
				createGame();
			}
		}

		@Override
		public void drawGame(Canvas canvas) {
			this.blank(canvas, Color.BLACK);
			final int TOTAL_PEG_SIZE = PEG_SIZE + PEG_SPACING;
			for (int i = 0; i < GRID_SIZE; i++) {
				for (int j = 0; j < GRID_SIZE; j++) {
					boolean hasPeg = pegs[i][j] != null;
					boolean isSelected = (i == selectedPeg.getX() && j == selectedPeg
							.getY()) && hasPeg;
					if (hasPeg) {
						draw.getPaint()
								.setColor(
										pegs[i][j].getColor(bonusAmount,
												isBoost(SineState.PEAK) ? 0.25f
														: 0.0f));
					} else {
						int rippleMod = redPegsStillNeeded == 0 ? (i + 1)
								* (j + 1) : i + j;
						int colInt1 = getRippleColor(rippleMod);
						int colInt2 = getRippleColor((rippleMod));
						int colInt3 = getRippleColor((rippleMod));
						draw.getPaint().setColor(
								Color.rgb(colInt1, colInt2, colInt3));
					}
					int x = BOARD_OFFSET_X + i * TOTAL_PEG_SIZE;
					int y = BOARD_OFFSET_Y + j * TOTAL_PEG_SIZE;
					canvas.drawRect(x, y, x + PEG_SIZE, y + PEG_SIZE,
							draw.getPaint());
					int margin = 6;
					if (isSelected) {
						draw.getPaint().setColor(pegs[i][j].boostColor);
					} else if (hasPeg) {
						draw.getPaint().setColor(pegs[i][j].hiColor);
					} else {
						int colInt = 16;
						draw.getPaint().setColor(
								Color.rgb(colInt, colInt, colInt));
					}
					canvas.drawRect(x + margin, y + margin, x + PEG_SIZE
							- margin, y + PEG_SIZE - margin, draw.getPaint());
				}
			}
			draw.getPaint().setColor(Color.WHITE);
			String mode = expert ? "Expert" : "Normal";
			mode += allowDiagonal ? " (diagonals)" : "";
			boolean landScape = (xMax > yMax);
			int textX = 50 + (landScape ? xMax / 2 : 0);
			int textY = yMax - 20;
			if (getGameState() == GameState.RUNNING) {
				canvas.drawText("Remaining: " + redPegsStillNeeded, textX,
						textY, draw.getPaint());
			} else {
				if (redPegsStillNeeded == 0) {
					canvas.drawText("YOU WIN", textX, textY, draw.getPaint());
				} else {
					draw.getPaint().setColor(Color.RED);
					canvas.drawText("GAME OVER", textX, textY, draw.getPaint());
				}
			}
			textY -= 30;
			canvas.drawText("Mode: " + mode, textX, textY, draw.getPaint());
			textY -= 30;
			canvas.drawText("Wins: " + numWins, textX, textY, draw.getPaint());
			textY -= 30;
			canvas.drawText("Score: " + score, textX, textY, draw.getPaint());

			textY -= 50;
			draw.getPaint().setStyle(Style.FILL_AND_STROKE);
			draw.getPaint().setColor(Color.BLUE);
			// if (bonusDecreaseDelay > 0.0f) {
			// draw.getPaint().setColor(Color.YELLOW);
			// }
			canvas.drawRect(textX, textY, textX + 50 * bonusAmount, textY + 16,
					draw.getPaint());
			draw.getPaint().setStyle(Style.STROKE);
			draw.getPaint().setColor(Color.WHITE);
			canvas.drawRect(textX, textY, textX + 50, textY + 16,
					draw.getPaint());
			draw.getPaint().setStyle(Style.FILL_AND_STROKE);

			textY -= 50;
			draw.getPaint().setColor(Color.WHITE);
			canvas.drawText("high score: " + gameHiScore, textX, textY,
					draw.getPaint());

			particle.draw(canvas);
		}

		public int getRippleColor(int rippleMod) {
			float col1 = (float) Math.sin((rippleMod * 0.5f) + tally);
			int multi = Math.min(127, 16 + numWins * 10);
			int colInt = (int) (col1 * multi + multi);
			return colInt;
		}

		boolean hasValidNeighbor(int i, int j) {
			int colorOrd = pegs[i][j].ordinal();
			for (Point2D dir : directions) {
				if (isInGrid(i + dir.getX(), j + dir.getY())) {
					PegColors col = pegs[i + dir.getX()][j + dir.getY()];
					if (col != null && col.ordinal() <= colorOrd) {
						return true;
					}
				}
			}
			return false;
		}

		void initAudio() {
			// soundManager.addSound(2, R.raw.clubbeat01);
			soundManager.addSound(0, R.raw.clubbeat63);
			soundManager.addSound(3, R.raw.melody1);
			soundManager.addSound(4, R.raw.melody2);
			soundManager.addSound(5, R.raw.melody3);
			soundManager.addSound(6, R.raw.rundown);
			soundManager.addSound(1, R.raw.synthbass2);

			AudioTrack audioTrack = new AudioTrack(3) {
				@Override
				boolean shouldPlay() {
					if (isAll(PegColors.PEG_BLUE)) {
						if (!special) {
							special = true;
							if (expert) {
								// Otherwise this is just the default state at
								// level start.
								int bonusMulti = Math.max(1, numWins);
								addScore(5 * bonusMulti);
							}
							return true;
						}
						return false;
					}
					special = false;
					return false;
				}
			};
			audioTracks.add(audioTrack);

			audioTrack = new AudioTrack(4) {
				@Override
				boolean shouldPlay() {
					if (isAll(PegColors.PEG_GREEN)) {
						if (!special) {
							int bonusMulti = Math.max(1, numWins);
							addScore(10 * bonusMulti);
							special = true;
							return true;
						}
						return false;
					}
					special = false;
					return false;
				}
			};
			audioTracks.add(audioTrack);

			audioTrack = new AudioTrack(5) {
				@Override
				boolean shouldPlay() {
					if (isAll(PegColors.PEG_YELLOW)) {
						if (!special) {
							int bonusMulti = Math.max(1, numWins);
							addScore(15 * bonusMulti);
							special = true;
							return true;
						}
						return false;
					}
					special = false;
					return false;
				}
			};
			audioTracks.add(audioTrack);

			audioTrack = new AudioTrack(6) {
				@Override
				boolean shouldPlay() {
					if (isAll(PegColors.PEG_RED)) {
						if (!special) {
							special = true;
							return true;
						}
						return false;
					}
					special = false;
					return false;
				}
			};
			audioTracks.add(audioTrack);

			audioTrack = new AudioTrack(0) {
				@Override
				boolean shouldPlay() {
					if (state == SineState.RISING) {
						special = !special;
						return !special;
					}
					return false;
				}
			};
			audioTracks.add(audioTrack);
			audioTrack = new AudioTrack(1) {
				@Override
				boolean shouldPlay() {
					return numWins > 0 && state == SineState.RISING;
				}
			};
			audioTracks.add(audioTrack);
			/*
			 * audioTrack = new AudioTrack(2) {
			 * 
			 * @Override boolean shouldPlay() { if (state == SineState.RISING &&
			 * numWins > 1) { special = !special; return !special; } return
			 * false; } }; audioTracks.add(audioTrack);
			 */
		}

		@Override
		public void initGame() {
			Log.i("", "initGame");
			draw.getPaint().setTextSize(30);
			createGame();
			initParticles();
			initAudio();
		}

		public void initParticles() {
			BirthRule rule = new BirthRule();
			rule.life = 0.5f;
			rule.velVarianceX = 600;
			rule.velVarianceY = 600;
			rule.emitCountPerSecond = 120;
			rule.particleCount = 200;
			rule.emitCountLimit = 60;
			particle = new ParticleSystem(rule);
		}

		boolean isAll(PegColors goalColor) {
			for (PegColors color : validPegs) {
				if (color != goalColor) {
					return false;
				}
			}
			return true;
		}

		boolean isBoost(SineState requiredState) {
			return doBoostTime > 0.0f && state == requiredState;
		}

		public boolean isInGrid(int x, int y) {
			if (x < 0 || x >= GRID_SIZE) {
				return false;
			}
			if (y < 0 || y >= GRID_SIZE) {
				return false;
			}
			return true;
		}

		boolean isViable(int x, int y) {
			if (!isInGrid(x, y)) {
				return false;
			}
			return pegs[x][y] == null;
		}

		@Override
		public void loadGame(SharedPreferences settings) {
			this.gameHiScore = settings.getInt("hiscore", 0);
			gameHiLevel = settings.getInt("hilevel", 0);
		}

		public void loseGame() {
			setGameState(GameState.PAUSED);
			challenge = 1;
		}

		void resetScore() {
			score = 0;
		}

		public void nextLevel() {
			requestedNewGame = false;

			// You didn't win the previous level.
			if (redPegsStillNeeded != 0) {
				numWins = 0;
				resetScore();
				bonusAmount = 0.0f;
			}

			if (getGameState() == GameState.RUNNING) {
				loseGame();
			}
			createGame();
		}

		@Override
		public boolean onTouchEvent(MotionEvent event) {
			if (super.onTouchEvent(event)) {
				return true;
			}
			final int action = event.getAction();
			switch (action & MotionEvent.ACTION_MASK) {
			case MotionEvent.ACTION_DOWN:
			case MotionEvent.ACTION_POINTER_DOWN: {
				if (getGameState() == GameState.RUNNING) {
					int y = (int) event.getY();
					int x = (int) event.getX();
					selectPegFromScreenPosition(x, y);
					sliding = true;
					Log.i("", "sliding");
				} else {
					requestedNewGame = true;
					return false;
				}
				return true;
			}
			case MotionEvent.ACTION_UP:
			case MotionEvent.ACTION_POINTER_UP: {
				if (slide) {
					sliding = false;
					Log.i("", "!sliding");
					selectedPeg.set(-1, -1);
				}
				return true;
			}
			case MotionEvent.ACTION_MOVE:
				if (!slide || !sliding) {
					return false;
				}
				if (!this.isInGrid(selectedPeg.getX(), selectedPeg.getY())
						|| pegs[selectedPeg.getX()][selectedPeg.getY()] == null) {
					int y = (int) event.getY();
					int x = (int) event.getX();
					selectPegFromScreenPosition(x, y);
					return true;
				} else {
					movedTouchPoint.set(event.getX(), event.getY());
					if (movedTouchPoint.dist2(lastTouchPoint) > PEG_SIZE
							* PEG_SIZE) {
						Point2D moveDir = new Point2D(movedTouchPoint);
						moveDir.subtract(lastTouchPoint);
						moveDir.normalize();
						Log.i("_", "move: " + moveDir.getXFloat() + ", "
								+ moveDir.getYFloat());
						float closestDir2 = 99.0f;
						Point2D closest = null;
						for (Point2D dir : directions) {
							float dist2 = dir.dist2Float(moveDir);
							Log.i("_", "dist2: " + dist2 + " (" + dir.getX()
									+ ", " + dir.getY() + ")");
							if (dist2 < closestDir2) {
								closestDir2 = dist2;
								closest = dir;
							}
						}
						if (closest != null) {
							int prevX = selectedPeg.getX();
							int prevY = selectedPeg.getY();
							int newX = prevX + closest.getX();
							int newY = prevY + closest.getY();
							if (pegsCombinable(newX, newY, prevX, prevY)) {
								combinePegs(newX, newY, prevX, prevY);
								sliding = false;
								Log.i("", "!sliding");
								return false;
							}
						}
					}
					return true;
				}
			}
			return false;
		}

		public boolean pegsCombinable(int newX, int newY, int prevX, int prevY) {
			if (!isInGrid(prevX, prevY) || !isInGrid(newX, newY)) {
				return false;
			}
			int xDist = Math.abs(prevX - newX);
			int yDist = Math.abs(prevY - newY);
			return pegs[prevX][prevY] == pegs[newX][newY]
					&& pegs[prevX][prevY] != null
					&& ((xDist + yDist == 1) || (allowDiagonal && (xDist
							+ yDist == 2)));
		}

		boolean putPeg(int x, int y, PegColors col) {
			Log.i("", "try " + col + ": " + x + ", " + y);
			if (isViable(x, y)) {
				setPeg(x, y, col);
				if (splitPeg(x, y)) {
					return true;
				}
				setPeg(x, y, null);
			}
			return false;
		}

		@Override
		public void reinitGame() {
			PEG_SIZE = Math.min(xMax, yMax) / (GRID_SIZE + 1);
			PEG_SIZE -= this.PEG_SPACING;
			BOARD_OFFSET_X = PEG_SIZE / 2;
			BOARD_OFFSET_Y = PEG_SIZE / 2;
		}

		@Override
		public void saveGame(Editor editor) {
			editor.putInt("hiscore", gameHiScore);
			editor.putInt("hilevel", gameHiLevel);
		}

		void selectPegFromScreenPosition(int x, int y) {
			lastTouchPoint.set(x, y);
			movedTouchPoint.set(x, y);
			Log.i("", "select peg " + x + ", " + y);
			final int TOTAL_PEG_SIZE = PEG_SIZE + PEG_SPACING;
			int newX = x - BOARD_OFFSET_X;
			int newY = y - BOARD_OFFSET_Y;
			newX /= TOTAL_PEG_SIZE;
			newY /= TOTAL_PEG_SIZE;

			int prevX = selectedPeg.getX();
			int prevY = selectedPeg.getY();
			if (pegsCombinable(newX, newY, prevX, prevY)) {
				combinePegs(newX, newY, prevX, prevY);
				return;
			}
			selectedPeg.set(newX, newY);
		}

		void setPeg(int x, int y, PegColors color) {
			if (pegs[x][y] != null) {
				validPegs.remove(pegs[x][y]);
			}
			if (color != null) {
				validPegs.add(color);
			}
			pegs[x][y] = color;
		}

		void setSineState(SineState newState) {
			if (state != newState) {
				doBoostTime = BOOST_TIME;
				boostChanged = true;
				state = newState;
			} else {
				boostChanged = false;
			}
		}

		boolean splitPeg(int x, int y) {
			PegColors ogColor = pegs[x][y];
			Point2D myPt = new Point2D(x, y);
			points.add(myPt);
			int pegOrdinal = ogColor.ordinal();
			int lowest = expert ? 0 : 1;
			if (pegOrdinal == lowest) {
				// We're done splitting now.
				return true;
			}
			PegColors nextColor = PegColors.values()[pegOrdinal - 1];
			setPeg(x, y, nextColor);
			boolean success = false;

			// store the points that are in the stack right now..
			int size = points.size();

			ArrayList<Point2D> myDirections = new ArrayList<Point2D>(directions);
			Collections.shuffle(myDirections);
			for (Point2D dir : myDirections) {
				if (putPeg(x + dir.getX(), y + dir.getY(), nextColor)) {
					success = true;
					break;
				}
			}

			if (success) {
				if (!splitPeg(x, y)) {
					// cleanup/erase anything added by the splitPeg calls in
					// the above putPeg.
					while (size < points.size()) {
						setPeg(points.get(size).getX(),
								points.get(size).getY(), null);
						points.remove(size);
					}
				} else {
					return true;
				}
			}
			setPeg(x, y, ogColor);
			Log.i("", x + ", " + y + " impossible!");
			return false;
		}

		float bonusDecreaseDelay = 0.0f;
		static final float BONUS_DECREASE_DELAY = 0.28f;

		@Override
		public void updateGame() {
			// TODO Auto-generated method stub
			particle.update();
			if (requestedNewGame) {
				nextLevel();
			}
			this.updateDelta();
			tally += delta * (1.0f / strobeSpeed) * (Math.PI * 2.0f);
			// strobeSpeed += delta * 0.5f;
			doBoostTime = Math.max(doBoostTime - delta, 0.0f);
			float sine = (float) Math.sin(tally);
			float rate = 1 / (strobeSpeed / 2);
			if (sine > lastSine) {
				setSineState(sine > 0.0f ? SineState.RISING : SineState.VALLEY);
			} else {
				setSineState(sine > 0.0f ? SineState.PEAK : SineState.FALLING);
			}

			if (boostChanged) {
				for (AudioTrack audioTrack : audioTracks) {
					if (audioTrack.shouldPlay()) {
						Log.i("PLAY", "sound: " + audioTrack.audioId);
						audioTrack.play(rate);
					}
				}
			}

			sineCount += delta;
			if (boostChanged && state == SineState.RISING) {
				Log.i("_", "sine time: " + sineCount);
				sineCount = 0.0f;
			}
			lastSine = sine;

			if (bonusAmount > 0.0f && getGameState() == GameState.RUNNING) {
				if (bonusDecreaseDelay > 0.0f) {
					bonusDecreaseDelay -= delta;
				} else {
					bonusAmount = Math.max(0.0f, bonusAmount
							- (delta * (1.0f / COOLDOWN_TIME)));
				}
			}
			if (bonusAmount > 1.0f) {
				bonusAmount = 1.0f;
			}
		}

		boolean validateBoard() {
			for (int i = 0; i < GRID_SIZE; i++) {
				for (int j = 0; j < GRID_SIZE; j++) {
					if (pegs[i][j] != null && pegs[i][j] != PegColors.PEG_RED) {
						if (!hasValidNeighbor(i, j)) {
							return false;
						}
					}
				}
			}
			return true;
		}

		void winGame() {
			numWins++;
			addScore(numWins * 10);
			challenge = Math.max(1, numWins / 2);
			setGameState(GameState.PAUSED);
		}

	}

	enum PegColors {
		PEG_PURPLE(Color.rgb(64, 0, 128), Color.MAGENTA, Color
				.rgb(255, 64, 255)),
		PEG_BLUE(Color.rgb(32, 32, 64), Color.BLUE, Color.rgb(64, 32, 255)),
		PEG_GREEN(Color.rgb(32, 64, 32), Color.GREEN, Color.rgb(64, 255, 64)),
		PEG_YELLOW(Color.rgb(64, 64, 0), Color.YELLOW, Color.rgb(255, 255, 255)),
		PEG_RED(Color.rgb(64, 0, 0), Color.RED, Color.rgb(255, 128, 0));

		private int baseColor;
		private int boostColor;
		private int hiColor;

		PegColors(int baseColor, int hiColor, int boostColor) {
			this.baseColor = baseColor;
			this.hiColor = hiColor;
			this.boostColor = boostColor;
		}

		int getColor(float pct, float boost) {
			int normalColor = lerpColor(Math.min(1.0f, pct), baseColor, hiColor);
			return lerpColor(Math.min(1.0f, boost), normalColor, boostColor);
		}

		public int lerpColor(float percent, int low, int high) {
			int red = (int) (Color.red(low) + ((Color.red(high) - Color
					.red(low)) * percent));
			int blue = (int) (Color.blue(low) + ((Color.blue(high) - Color
					.blue(low)) * percent));
			int green = (int) (Color.green(low) + ((Color.green(high) - Color
					.green(low)) * percent));
			return Color.rgb(red, green, blue);
		}
	}

	enum SineState {
		FALLING, PEAK, RISING, VALLEY
	}

	static boolean allowDiagonal = false;
	static boolean expert = false;
	static final int GRID_SIZE = 12;

	GameView gv = null;
	int numWins = 0;
	PegColors[][] pegs = new PegColors[GRID_SIZE][GRID_SIZE];
	boolean requestedNewGame = false;
	float strobeSpeed = 2.0f; // Time (in seconds) per pulse
	ArrayList<PegColors> validPegs = new ArrayList<PegColors>();

	@Override
	public GameView createGameView() {
		gv = new OneRedPegView(this);
		return gv;
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		// setContentView(R.layout.myLayout);
		Log.i("_", "onConfigurationChanged");
		setContentView(gv);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu2, menu);
		return true;
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
			// strobeSpeed += 0.1f;
			Log.i("", "up " + strobeSpeed);
			numWins++;
		}
		if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
			// strobeSpeed -= 0.1f;
			numWins--;
			Log.i("", "down " + strobeSpeed);
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
		case R.id.newgame2:
			requestedNewGame = true;
			return true;
		case R.id.togglediagonal:
			allowDiagonal = !allowDiagonal;
			requestedNewGame = true;
			return true;
		case R.id.toggleexpert:
			expert = !expert;
			requestedNewGame = true;
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	void showAlert(String message) {
		new AlertDialog.Builder(this)
				.setTitle("Update Status")
				.setMessage(message)
				.setView(gv)
				// should be input?
				.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						// Editable value = input.getText();
						Log.i("", "clicked ok");
					}
				})
				.setNegativeButton("Cancel",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
								// Do nothing.
							}
						}).show();
	}
}