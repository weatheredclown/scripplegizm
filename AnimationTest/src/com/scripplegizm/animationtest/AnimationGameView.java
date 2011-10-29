package com.scripplegizm.animationtest;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.util.Pair;
import android.view.KeyEvent;
import android.view.MotionEvent;

import com.scripplegizm.gameutils.AnimatedSprite;
import com.scripplegizm.gameutils.AnimatedSprite.AnimMode;
import com.scripplegizm.gameutils.AnimatedSprite.NextAnim;
import com.scripplegizm.gameutils.ClickableRect;
import com.scripplegizm.gameutils.GameView;
import com.scripplegizm.gameutils.Histogram;
import com.scripplegizm.gameutils.Point2D;

public class AnimationGameView extends GameView {
	enum RoomType {
		TYPE_A(0, new Rect(0, 300, 150, 310), new Rect(250, 300, 1280, 310) // floor with hole
				, new Rect(50, 50, 60, 300) // left wall
				, new Rect(400, 50, 410, 200) // right wall with door
				, new Rect(0, 50, 1280, 60) // roof
				, new Rect(0, 550, 1280, 560) // lower floor
				),
		TYPE_B(1, new Rect(0, 300, 650, 310), new Rect(700, 300, 1280, 310) // floor,
				 , new Rect(0, 50, 150, 60), new Rect(250, 50, 1280, 60) // roof with hole
				 , new Rect(400, 50, 410, 300) // right wall
				 , new Rect(50, 50, 60, 200) // left wall with door
		, new Rect(0, 550, 1280, 560) // lower floor
				), 
		TYPE_C(2, new Rect(0, 300, 1280, 310)  // floor,
				, new Rect(0, 50, 150, 60), new Rect(250, 50, 1280, 60) // roof with hole
				, new Rect(50, 50, 60, 200) // left wall with door
				, new Rect(400, 50, 410, 200) // right wall with door
		, new Rect(0, 550, 1280, 560) // lower floor
				),
		TYPE_D(3, new Rect(0, 50, 1280, 60) // roof
				, new Rect(0, 300, 150, 310), new Rect(250, 300, 1280, 310) // floor with hole		
				, new Rect(50, 50, 60, 200) // left wall with door
				, new Rect(400, 50, 410, 300) // right wall
		, new Rect(0, 550, 1280, 560) // lower floor
				),
		TYPE_E(4, new Rect(0, 300, 1280, 310) // floor,
				, new Rect(0, 50, 1280, 60) // roof		
				, new Rect(50, 50, 60, 200) // left wall with door
				, new Rect(400, 50, 410, 200) // right wall with door
		, new Rect(0, 550, 1280, 560) // lower floor
				),
		TYPE_F(5, 
				new Rect(0, 50, 128, 60),
				new Rect(128, 256, 256, 266),
				new Rect(256, 384, 384, 394),
				new Rect(384, 550, 512, 560),
				new Rect(256, 640, 384, 650)
				), // floor,
		TYPE_G(6, new Rect(50, 50, 60, 300), // left wall
				  new Rect(400, 50, 410, 300), // right wall
				  new Rect(0, 300, 1280, 310), // floor
			      new Rect(0, 50, 150, 60), new Rect(250, 50, 1280, 60) // roof with hole
		, new Rect(0, 550, 1280, 560) // lower floor
		      ),
		TYPE_H(0, new Rect(0, 300, 150, 310), new Rect(250, 300, 1280, 310) // floor with hole
				, new Rect(50, 50, 60, 300) // left wall
				, new Rect(400, 50, 410, 200) // right wall with door
				, new Rect(0, 50, 1280, 60) // roof
				, new Rect(0, 550, 300, 560), new Rect(450, 550, 1280, 560), new Rect(400, 600, 450, 610) // lower floor with hole
				),
		TYPE_I(1 				, new Rect(0, 300, 150, 310), new Rect(250, 300, 1280, 310) // floor with hole		
				 , new Rect(0, 50, 1280, 60) // roof
				 , new Rect(400, 50, 410, 300) // right wall
				 , new Rect(50, 50, 60, 200) // left wall with door
		, new Rect(0, 550, 300, 560), new Rect(450, 550, 1280, 560), new Rect(400, 600, 450, 610) // lower floor with hole
				), 
		TYPE_J(2, new Rect(0, 300, 1280, 310)  // floor,
				, new Rect(0, 50, 150, 60), new Rect(250, 50, 1280, 60) // roof with hole
				, new Rect(50, 50, 60, 200) // left wall with door
				, new Rect(400, 50, 410, 200) // right wall with door
		, new Rect(0, 550, 300, 560), new Rect(450, 550, 1280, 560), new Rect(400, 600, 450, 610) // lower floor with hole
				),
		TYPE_K(3, new Rect(0, 50, 1280, 60) // roof
				, new Rect(0, 300, 150, 310), new Rect(250, 300, 1280, 310) // floor with hole		
				, new Rect(50, 50, 60, 200) // left wall with door
				, new Rect(400, 50, 410, 300) // right wall
		, new Rect(0, 550, 300, 560), new Rect(450, 550, 1280, 560), new Rect(400, 600, 450, 610) // lower floor with hole
				),
		TYPE_L(4, new Rect(0, 300, 1280, 310) // floor,
				, new Rect(0, 50, 1280, 60) // roof		
				, new Rect(50, 50, 60, 200) // left wall with door
				, new Rect(400, 50, 410, 200) // right wall with door
		, new Rect(0, 550, 300, 560), new Rect(450, 550, 1280, 560), new Rect(400, 600, 450, 610) // lower floor with hole
				),
		TYPE_N(6, new Rect(50, 50, 60, 300), // left wall
				  new Rect(400, 50, 410, 300) // right wall
				, new Rect(0, 300, 170, 310), new Rect(270, 300, 1280, 310) // floor with hole		
			    , new Rect(0, 50, 150, 60), new Rect(250, 50, 1280, 60) // roof with hole
		, new Rect(0, 550, 300, 560), new Rect(450, 550, 1280, 560), new Rect(400, 600, 450, 610) // lower floor with hole
		      ),
				;
		ArrayList<Rect> rects = new ArrayList<Rect>();
		int index;

		RoomType(int index, Rect... a) {
			this.index = index;
			for (Rect rect : a) {
				rects.add(rect);
			}
		}

		public int getIndex() {
			return index;
		}

		public Collection<? extends Rect> getPlatforms() {
			return rects;
		}
	}

	enum RoomColor {
		RED(0, Color.RED), BLUE(1, Color.BLUE), GREEN(2, Color.GREEN), YELLOW(
				3, Color.YELLOW);

		private int color;
		private int index;

		public int getColor() {
			return color;
		}

		public int getIndex() {
			return index;
		}

		RoomColor(int index, int color) {
			this.color = color;
			this.index = index;
		}
	}

	public AnimationGameView(Context context) {
		super(context);
	}

	class ClickableDirectionScreenArea extends ClickableRect {

		final Direction dir;

		public ClickableDirectionScreenArea(int x, int y, int width,
				int height, ClickableMode mode, GameState st, int keyCode,
				Direction dir) {
			super(x, y, width, height, mode, st, keyCode);
			this.dir = dir;
		}

		@Override
		public void click() {
			direction.add(dir);
		}

		@Override
		public void up() {
			direction.remove(dir);
		}
	}

	class PlatformManager {
		ArrayList<Platform> platforms = new ArrayList<Platform>();

		public PlatformManager() {
		}

		Rect lastRect = new Rect();
		Rect lastLastRect = new Rect();

		// Doing this component-style is probably insufficient.
		public Pair<Boolean, Float> overlapX(Rect guyRect, float deltaX) {
			Rect collisionRect = new Rect(guyRect);
			collisionRect.bottom--; // fudge to not overlap what we stand on.
			if (deltaX > 0.0f) {
				collisionRect.right += deltaX;
			} else {
				collisionRect.left += deltaX;
			}

			for (Platform platform : platforms) {
				if (Rect.intersects(platform.rect, collisionRect)) {
					if (deltaX > 0.0f) {
						return new Pair<Boolean, Float>(true,
								(float) platform.rect.left);
					} else {
						return new Pair<Boolean, Float>(true,
								(float) platform.rect.right);
					}
				}
			}
			return new Pair<Boolean, Float>(false, 0.0f);
		}

		public Pair<Boolean, Float> overlapY(Rect guyRect, float deltaY) {
			Rect collisionRect = new Rect(guyRect);
			if (deltaY > 0.0f) {
				collisionRect.bottom += deltaY;
			} else {
				collisionRect.top += deltaY;
			}

			lastLastRect.set(lastRect);
			lastRect.set(collisionRect);

			for (Platform platform : platforms) {
				if (Rect.intersects(platform.rect, collisionRect)) {
					if (deltaY > 0.0f) {
						return new Pair<Boolean, Float>(true,
								(float) platform.rect.top);
					} else {
						return new Pair<Boolean, Float>(true,
								(float) platform.rect.bottom);
					}
				}
			}
			return new Pair<Boolean, Float>(false, 0.0f);
		}

		void debugDraw(Canvas canvas) {
			// Debug Draw
			draw.getPaint().setStyle(Style.STROKE);
			draw.getPaint().setColor(Color.BLACK);
			canvas.drawRect(lastRect, draw.getPaint());
			draw.getPaint().setStyle(Style.FILL);
			drawText(canvas, "Coliision: " + lastRect.left + ", "
					+ lastRect.top, 100, 90);
			draw.getPaint().setColor(Color.BLACK);
			canvas.drawLine(lastRect.right, lastRect.bottom,
					lastLastRect.right, lastLastRect.bottom, draw.getPaint());
		}

		public void draw(Canvas canvas) {
			for (Platform platform : platforms) {
				platform.draw(canvas);
			}
			// debugDraw(canvas);
		}

		public void setPlatforms(RoomType type) {
			platforms.clear();
			for (Rect r : type.getPlatforms()) {
				platforms.add(new Platform(r));
			}
		}
	}

	class Guy {
		AnimatedSprite sprite_run = new AnimatedSprite(
				decodeBitmap(R.drawable.guyrun), 10, 4, AnimMode.PINGPONG);
		AnimatedSprite sprite_look = new AnimatedSprite(
				decodeBitmap(R.drawable.guylook), 5, 4, AnimMode.PINGPONG);
		AnimatedSprite sprite_stand = new AnimatedSprite(
				decodeBitmap(R.drawable.guystand), 5, 3, AnimMode.PINGPONG);

		static final int RUN_SPEED = 120;

		Point2D pos = new Point2D(50, 100);
		AnimatedSprite curAnim = sprite_stand;
		boolean flipped = false;
		float idleTime = 15.0f;
		float yVelocity = 0.0f;
		Rect rect = new Rect();

		void update() {
			if (direction.size() == 0) {
				if (curAnim != sprite_stand && curAnim != sprite_look) {
					curAnim = sprite_stand;
					idleTime = rand.nextInt(15) + 10;
				}
				idleTime -= delta;
				if (idleTime <= 0.0f && curAnim == sprite_stand) {
					curAnim = sprite_look;
					sprite_look.start(AnimMode.PINGPONG_SEQUENCE,
							new NextAnim() {
								@Override
								public void onEnd(AnimatedSprite prev) {
									curAnim = sprite_stand;
									idleTime = rand.nextInt(15) + 1;
								}

							});
				}
			} else {
				curAnim = sprite_run;
			}

			float xDelta = 0.0f;

			if (direction.contains(Direction.LEFT)) {
				flipped = true;
				xDelta = delta * -RUN_SPEED;
			} else if (direction.contains(Direction.RIGHT)) {
				flipped = false;
				xDelta = delta * RUN_SPEED;
			}

			Rect guyRect = this.calcRect();
			Pair<Boolean, Float> xCollision = platformManager.overlapX(guyRect,
					xDelta);
			if (xCollision.first) {
				if (xDelta > 0.0f) {
					pos.setX(xCollision.second - guyRect.width());
				} else {
					pos.setX(xCollision.second + 1);
				}
			} else {
				pos.addX(xDelta);
			}

			if (yVelocity == 0 && direction.contains(Direction.UP)) {
				yVelocity = -500;
			}

			float yDelta = yVelocity * delta;
			Pair<Boolean, Float> yCollision = platformManager.overlapY(guyRect,
					yDelta);
			if (yCollision.first) {
				if (yVelocity > 0.0f) {
					pos.setY(yCollision.second - guyRect.height());
					yVelocity = 0.0f;
				} else {
					pos.setY(yCollision.second + 1);
					yVelocity = 0.1f;
				}
			} else {
				yVelocity += 500.0f * delta;
				pos.addY(yDelta);
			}

			if (pos.getX() > xMax) {
				pos.setX(-32);
				setRoom(getRoom(curRoom.pos.getX() + 1, curRoom.pos.getY()));
			}
			if (pos.getX() < -32) {
				pos.setX(xMax);
				setRoom(getRoom(curRoom.pos.getX() - 1, curRoom.pos.getY()));
			}
			if (pos.getY() > yMax) {
				pos.setY(-32);
				setRoom(getRoom(curRoom.pos.getX(), curRoom.pos.getY() + 1));
			}
			if (pos.getY() < -32) {
				pos.setY(yMax);
				setRoom(getRoom(curRoom.pos.getX(), curRoom.pos.getY() - 1));
			}
			curAnim.Update(delta);
			curAnim.setFlip(flipped);
			curAnim.pos.set(pos.getX(), pos.getY());
		}

		private Rect calcRect() {
			rect.set(pos.getX(), pos.getY(),
					pos.getX() + curAnim.getSpriteWidth(),
					pos.getY() + curAnim.getSpriteHeight());
			return rect;
		}

		void draw(Canvas canvas) {
			curAnim.draw(canvas);
		}
	}

	private void setRoom(Room room) {
		curRoom = room;
		platformManager.setPlatforms(room.type);
	}

	class Room {
		Point2D pos = new Point2D(0, 0);
		RoomColor color;
		RoomType type;

		public void save(int i, Editor editor) {
			editor.putInt("room" + i + "_x", pos.getX());
			editor.putInt("room" + i + "_y", pos.getY());
			editor.putInt("room" + i + "_color", color.getIndex());
			editor.putInt("room" + i + "_type", type.getIndex());
		}

		public void load(int i, SharedPreferences settings) {
			int roomColor = settings.getInt("room" + i + "_color", 0);
			color = RoomColor.values()[roomColor];
			pos.setX(settings.getInt("room" + i + "_x", 0));
			pos.setY(settings.getInt("room" + i + "_y", 0));
			int roomType = settings.getInt("room" + i + "_type", 0);
			type = RoomType.values()[roomType];
		}
	}

	class Platform {
		Rect rect;

		public Platform(Rect r) {
			rect = new Rect(r);
		}

		public Platform(int x1, int y1, int x2, int y2) {
			rect = new Rect(x1, y1, x2, y2);
		}

		void draw(Canvas canvas) {
			draw.getPaint().setColor(Color.BLACK);
			canvas.drawRect(rect, draw.getPaint());
		}
	}

	PlatformManager platformManager = new PlatformManager();

	Set<Direction> direction = new HashSet<Direction>();
	boolean shouldDrawMap = false;
	Bitmap trophy = decodeBitmap(R.drawable.trophy);
	Rect trophyRect = new Rect(100, 300-trophy.getHeight(), 100+trophy.getWidth(), 300);
	ClickableRect rightButton = new ClickableDirectionScreenArea(0, 0, 32, 32,
			ClickableMode.HOLDABLE, GameState.RUNNING, KeyEvent.KEYCODE_DPAD_RIGHT,
			Direction.RIGHT);
	ClickableRect leftButton = new ClickableDirectionScreenArea(0, 0, 32, 32,
			ClickableMode.HOLDABLE, GameState.RUNNING, KeyEvent.KEYCODE_DPAD_LEFT, Direction.LEFT);
	ClickableRect upButton = new ClickableDirectionScreenArea(0, 0, 32, 32,
			ClickableMode.HOLDABLE, GameState.RUNNING, KeyEvent.KEYCODE_DPAD_UP, Direction.UP);
	ClickableRect downButton = new ClickableDirectionScreenArea(0, 0, 32, 32,
			ClickableMode.HOLDABLE, GameState.RUNNING, KeyEvent.KEYCODE_DPAD_DOWN, Direction.DOWN);
	// could also have a map of maps for x and y coords fast search of
	// sparse data.
	ArrayList<Room> rooms = new ArrayList<Room>();
	Guy guy = new Guy();
	Room curRoom = null;
	Room treasureRoom = null;
	int loadedCount = 0;
	int winCount = 0;
	int mostRoomsWin = 0;

	public void loadGame(SharedPreferences settings) {
		int roomCount = settings.getInt("roomcount", 0);
		rooms.clear();
		for (int i = 0; i < roomCount; i++) {
			Room room = new Room();
			room.load(i, settings);
			rooms.add(room);
		}
		if (roomCount > 0) {
			int x = settings.getInt("pos_x", 0);
			int y = settings.getInt("pos_y", 0);
			setRoom(getRoom(x, y));
			int tx = settings.getInt("treasure_x", 0);
			int ty = settings.getInt("treasure_y", 0);
			treasureRoom = getRoom(tx, ty);
			
			guy.pos.setX(settings.getInt("guy_x", 0));
			guy.pos.setY(settings.getInt("guy_y", 0));
			winCount = settings.getInt("win_count", 0);
			mostRoomsWin = settings.getInt("most_rooms_win", 0);
		} else {
			newGame();
		}
		loadedCount = roomCount;
		/*
		 * boolean silent = settings.getBoolean("silentMode", false);
		 */
	}

	public void saveGame(Editor editor) {
		editor.putInt("roomcount", rooms.size());
		int i = 0;
		for (Room room : rooms) {
			room.save(i++, editor);
		}
		editor.putInt("pos_x", curRoom.pos.getX());
		editor.putInt("pos_y", curRoom.pos.getY());
		editor.putInt("treasure_x", treasureRoom.pos.getX());
		editor.putInt("treasure_y", treasureRoom.pos.getY());
		editor.putInt("guy_x", guy.pos.getX());
		editor.putInt("guy_y", guy.pos.getY());
		editor.putInt("win_count", winCount);
		editor.putInt("most_rooms_win", mostRoomsWin);
	}
	
	@Override
	public void drawGame(Canvas canvas) {
		draw.getPaint().setColor(curRoom.color.getColor());
		canvas.drawRect(new Rect(0, 0, xMax + 1, yMax + 1), draw.getPaint());

		platformManager.draw(canvas);
		if (curRoom == treasureRoom) {
			canvas.drawBitmap(trophy, 100, 300-trophy.getHeight(), null);
		}
		guy.draw(canvas);
		this.drawText(canvas, "ROOM: " + curRoom.pos.getX() + ", "
				+ curRoom.pos.getY() + " (" + curRoom.type + ")", 100, 25);
		this.drawText(canvas, "Num Rooms: " + rooms.size(), 100, 45);
		this.drawText(canvas, "Hi Rooms: " + mostRoomsWin, 300, 25);
		this.drawText(canvas, "Win Count: " + winCount, 300, 45);		
		
		drawMap(canvas);

		/*
		 * fps.updateValue((int) (delta * 400)); fps.draw(canvas);
		 */
	}

	Histogram fps = new Histogram();

	void drawMap(Canvas canvas) {
		if (!shouldDrawMap) {
			return;
		}

		int roomSize = 33;

		for (Room room : rooms) {
			int x = xMax / 2;
			int y = yMax / 2;
			x -= curRoom.pos.getX() * roomSize;
			y -= curRoom.pos.getY() * roomSize;
			x += room.pos.getX() * roomSize;
			y += room.pos.getY() * roomSize;
			x += mapX;
			y += mapY;
			draw.getPaint().setColor(Color.BLACK);
			canvas.drawRect(x - 1, y - 1, x + roomSize, y + roomSize,
					draw.getPaint());
			draw.getPaint().setColor(room.color.getColor());
			canvas.drawRect(x, y, x + roomSize - 1, y + roomSize - 1,
					draw.getPaint());
			if (room == treasureRoom) {
				draw.getPaint().setColor(Color.rgb(rand.nextInt(256), rand.nextInt(256),
						rand.nextInt(256)));
				canvas.drawCircle(x + 16, y + 16, 7, draw.getPaint());
			}
			if (room == curRoom) {
				draw.getPaint().setColor(Color.WHITE);
				canvas.drawCircle(x + 16, y + 16, 7, draw.getPaint());
			}
		}
	}

	@Override
	public void updateGame() {
		updateDelta();
		if (curRoom == treasureRoom) {
			if (Rect.intersects(guy.calcRect(), trophyRect)) {
				// YOU WIN!
				winCount++;
				if (rooms.size() > mostRoomsWin) {
					mostRoomsWin = rooms.size();
				}
				newGame();
			}
		}
		guy.update();
	}

	@Override
	public void initGame() {
		setGameState(GameState.RUNNING);
		buttonManager.addButton(rightButton);
		buttonManager.addButton(leftButton);
		buttonManager.addButton(upButton);
		buttonManager.addButton(downButton);

		leftButton.addKey(KeyEvent.KEYCODE_A);
		downButton.addKey(KeyEvent.KEYCODE_S);
		rightButton.addKey(KeyEvent.KEYCODE_D);
		upButton.addKey(KeyEvent.KEYCODE_W);
	}

	@Override
	public void reinitGame() {
		rightButton.x = xMax - 64;
		rightButton.y = 0;
		rightButton.width = 64;
		rightButton.height = yMax;

		leftButton.x = 0;
		leftButton.y = 0;
		leftButton.width = 64;
		leftButton.height = yMax;

		upButton.x = 0;
		upButton.y = 0;
		upButton.width = xMax;
		upButton.height = 64;

		downButton.x = 0;
		downButton.y = yMax - 64;
		downButton.width = xMax;
		downButton.height = 64;
	}

	@Override
	public void newGame() {
		guy.pos.set(50, 100);
		rooms.clear();
		setRoom(getRoom(0, 0));
		treasureRoom = newRoom(rand.nextInt(25)-10, rand.nextInt(25)-10);
		treasureRoom.type = RoomType.TYPE_G;
	}

	private Room getRoom(int x, int y) {
		for (Room room : rooms) {
			if (room.pos.getX() == x && room.pos.getY() == y) {
				return room;
			}
		}
		return newRoom(x, y);
	}

	private Room newRoom(int x, int y) {
		Room room = new Room();
		room.color = randomRoomColor();
		room.type = randomRoomType();
		room.pos.set(x, y);
		rooms.add(room);
		return room;
	}

	private RoomColor randomRoomColor() {
		return RoomColor.values()[rand.nextInt(RoomColor.values().length)];
	}

	private RoomType randomRoomType() {
		return RoomType.values()[rand.nextInt(RoomType.values().length)];
	}
	
	Point2D mapTouch = new Point2D(0, 0);

	int mapX = 0, mapY = 0, fullMapX = 0, fullMapY = 0;

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		final int action = event.getAction();
		switch (action & MotionEvent.ACTION_MASK) {
			case MotionEvent.ACTION_DOWN:
			case MotionEvent.ACTION_POINTER_DOWN: {
				mapTouch.set(event.getX(), event.getY());
				break;
			}
			case MotionEvent.ACTION_UP:
			case MotionEvent.ACTION_POINTER_UP: {
				fullMapX = mapX;
				fullMapY = mapY;
				mapTouch.set(event.getX(), event.getY());
				break;
			}
		}

		mapX = (int) (event.getX() - mapTouch.getX()) + fullMapX;
		mapY = (int) (event.getY() - mapTouch.getY()) + fullMapY; 
		
		boolean superRet = super.onTouchEvent(event);
		return shouldDrawMap || superRet;
	}

	
	public void toggleMap() {
		mapX = 0;
		mapY = 0;
		fullMapX = 0;
		fullMapY = 0;
		shouldDrawMap = !shouldDrawMap;
	}
}
