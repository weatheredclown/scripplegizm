package com.scripplegizm.elements;

import java.util.ArrayList;
import java.util.HashSet;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.util.Log;

import com.scripplegizm.elements.Blob.Farmer;
import com.scripplegizm.elements.ElementsUtils.Func0;
import com.scripplegizm.elements.ElementsUtils.Func1;
import com.scripplegizm.elements.ElementsUtils.Func2;
import com.scripplegizm.elements.ElementsUtils.Func3;
import com.scripplegizm.elements.ElementsUtils.Value;
import com.scripplegizm.elements.GameObject.WorldColor;
import com.scripplegizm.elements.Scenario.Rule.Processor;
import com.scripplegizm.gameutils.GameView;
import com.scripplegizm.gameutils.Point2D;

class WorldCell {

	enum AddMode {
		REGULAR_CONSUMER, FARMER, FOOD_GATHERER, TOXIN_GATHERER
	}

	ArrayList<GameObject> objects = new ArrayList<GameObject>();
	private ArrayList<GameObject> deferredObjectAdds = new ArrayList<GameObject>();
	private ArrayList<Road> roads = new ArrayList<Road>();
	int[] colorAmount = new int[WorldColor.values().length]; // collected
	// resources
	int[] colorResource = new int[WorldColor.values().length]; // available

	// resources
	HashSet<WorldColor> colors = new HashSet<WorldColor>(); // set
															// of
															// colors
															// present
															// in
															// this
															// square

	boolean active = true;
	int yOffset = 0;

	static Integer _i;

	static final int INITIAL_X_ELEMENT_OFFSET = 30;
	static final int INITIAL_Y_ELEMENT_OFFSET = 25;
	static final int ELEMENT_Y_SPACING = 30;
	static final int ELEMENT_X_SPACING = 30;
	static final int ELEMENT_GROUP_SPACING = 125;
	static final int ELEMENT_X_MARGIN = 130;

	static ThreadLocal<Boolean> isGameThread = new ThreadLocal<Boolean>() {
		@Override
		protected synchronized Boolean initialValue() {
			return false;
		}
	};

	public static void threadCheck() {
		if (!isGameThread.get()) {
			_i++;
		}
	}

	WorldCell() {
		Log.i("_", "Create: " + this);

		for (int i = 0; i < WorldColor.values().length; i++) {
			// amount is gathered, resource is potential
			colorAmount[i] = 0;
			colorResource[i] = 32; // randomize this
		}

	}

	// try to gather first, then pull from available
	boolean absorbColor(WorldColor c) {
		return absorbColor(c, 1);
	}

	boolean absorbColor(WorldColor c, int amt) {
		gather(c, amt, true);
		boolean b = consume(c, amt) != 0;
		if (b) {
			Log.i("_", "[ABSORBED " + c + "]");
		}
		return b;
	}

	public void addBlob(Blob object) {
		absorbColor(object.getColor());
		object.setCurrentCell(this);
		addObject(object);
	}

	/*
	 * gmBlob AddBlob(gmPlayerData player, gmBlob object) { // Log.i("_",
	 * "add blob\n"); if (CanAddBlob(player)) { Log.i("_",
	 * "AddBlob: absorb living color " + player.getBlobLivingColor().value +
	 * "\n"); AbsorbColor(player.getBlobLivingColor().value);
	 * object.SetCurrentCell(this); return object; } return null; }
	 */
	/*
	 * gmBlobFarmer AddFarmer(gmPlayerData player) { if (CanAddBlob(player,
	 * AddMode.FARMER) && AbsorbColor(player.getBlobLivingColor())) { return
	 * (gmBlobFarmer) AddObject(new gmBlobFarmer( player.getBlobLivingColor(),
	 * this, player)); // this // will // be // added } return null; }
	 */

	GameObject addObject(GameObject obj) {
		return addObject(obj, false);
	}

	GameObject addObject(GameObject obj, boolean deferred) {
		Log.i("_", this + " added " + obj.getColor() + " " + obj + " for "
				+ obj.playerData);
		if (deferred) {
			deferredObjectAdds.add(obj);
		} else {
			threadCheck();
			objects.add(obj);
		}
		return obj;
	}

	Road addRoad(Road road) {
		Log.i("_", this + " added " + road.getColor() + " " + road);
		roads.add(road);
		return road;
	}

	boolean canAddBlob(PlayerData player, AddMode mode) {
		int population = getPopulation(player);
		int toxicAmt = colorResource[player.getToxicColor().ordinal()];
		int foodAmt = colorResource[player.getFoodColor().ordinal()];
		int limitAmt = Math.max(0, toxicAmt - foodAmt);
		int popLimit = Math.max(0, 22 - (int) (limitAmt * 0.667f));
		Log.i("POPULATION", "pop:" + population + ", lim:" + popLimit
				+ ", tox:" + toxicAmt);
		if (toxicAmt != 0 && population > popLimit
				&& mode != AddMode.TOXIN_GATHERER) {
			ElementsView.showMessage("Too much toxic " + player.getToxicColor()
					+ ", can't grow population.");
			Log.i("_", "too toxic for " + player + "!\n");
			return false;
		}
		boolean enoughFood = isColorAvailable(player.getFoodColor())
				|| mode == AddMode.FARMER
				|| (mode == AddMode.FOOD_GATHERER && isResourceAvailable(player
						.getFoodColor()));
		if (enoughFood) {
			int collected = colorAmount[player.getLivingColor().ordinal()];
			int available = colorResource[player.getLivingColor().ordinal()];
			int total = collected + available;
			int cost = (mode == AddMode.FARMER) ? Farmer.COST : 1;
			if (total < cost) {
				ElementsView.showMessage("Need at least " + cost + " LIVING "
						+ player.getLivingColor() + ".");
				Log.i("_", player.getLivingColor()
						+ " living color not available for " + player + ".");
			}
			return total != 0;
		} else {
			Value<Integer> edibleBuildings = new Value<Integer>(0);
			ForEach(new Func2<GameObject, Value<Integer>, WorldColor>(
					edibleBuildings, player.getFoodColor()) {
				@Override
				void call(GameObject t, Value<Integer> value, WorldColor c) {
					if (t.isStructure() && t.getColor() == c) {
						value.value++;
					}
				}
			});

			if (edibleBuildings.value > 0) {
				return true;
			}

			ElementsView.showMessage(player.getFoodColor()
					+ " food not available.");
			Log.i("_", player.getFoodColor() + " food not available.");
		}
		return false;
	}

	void clearResources() {
		for (int i = 0; i < WorldColor.values().length; i++) {
			colorResource[i] = 0;
		}
	}

	public void click(Value<Float> dist2, Point2D pos, Value<GameObject> obj) {
		// Log.i("CLICK", "CELL: " + this);
		int x = INITIAL_X_ELEMENT_OFFSET;
		int y = INITIAL_Y_ELEMENT_OFFSET + yOffset;

		Point2D curColorPos = new Point2D(0, 0);
		Point2D foundColorPos = new Point2D(World.curColorPos);
		float dist = foundColorPos.dist2(pos);
		WorldColor foundColor = World.curColor;
		for (WorldColor curColor : WorldColor.values()) {
			if (curColor == WorldColor.NONE) {
				continue;
			}
			curColorPos.set(x, y);
			float d = curColorPos.dist2(pos);
			if (d < dist) {
				foundColorPos.set(x, y);
				dist = d;
				foundColor = curColor;
			}

			x += ELEMENT_X_SPACING;
			if (x > GameView.xMax - ELEMENT_X_MARGIN) {
				y += ELEMENT_Y_SPACING;
				x = INITIAL_X_ELEMENT_OFFSET;
			}
		}

		if (dist > 2500) {
			foundColorPos.set(0, 0);
			foundColor = WorldColor.NONE;
		}

		World.setClickedColor(foundColorPos, foundColor);

		ForEach(new Func3<GameObject, Value<Float>, Point2D, Value<GameObject>>(
				dist2, pos, obj) {
			@Override
			void call(GameObject obj, Value<Float> dist2, Point2D pos,
					Value<GameObject> foundObj) {
				obj.click(dist2, pos, foundObj);
			}
		});
	}

	/**
	 * Remove amount of color from the gathered color in the cell. Resources
	 * cease to exist.
	 * 
	 * @param color
	 *            color to absorb
	 * @param amount
	 *            amount of color to absorb
	 * @return Amount actually consumed (may be less than requested)
	 */
	int consume(WorldColor color, int amount) {
		if (amount < 0)
			return -produce(color, -amount);
		if (colorAmount[color.ordinal()] < amount)
			amount = colorAmount[color.ordinal()];
		colorAmount[color.ordinal()] -= amount;
		if (colorAmount[color.ordinal()] == 0)
			colors.remove(color);
		return amount;
	}

	public void deleteDeadObjects() {
		// delete dead objects
		ArrayList<GameObject> toRemove = new ArrayList<GameObject>();
		threadCheck();
		for (GameObject object : objects) {
			if (object.readyForDeletion) {
				object.onRemove();
				if (World.curObject == object) {
					World.setCurObject(null);
				}
				Log.i("_", object + " was ready for delete, now gone");
				toRemove.add(object);
			}
		}
		objects.removeAll(toRemove);
	}

	public void draw(Canvas canvas, int yOffset) {
		this.yOffset = yOffset;
		int x = INITIAL_X_ELEMENT_OFFSET;
		int y = INITIAL_Y_ELEMENT_OFFSET + yOffset;

		Paint paint = GameView.draw.getPaint();
		paint.setStyle(Style.STROKE);
		canvas.drawRect(15, yOffset, GameView.xMax - ELEMENT_X_MARGIN + 10,
				105 + yOffset, paint);
		for (int i = 1; i < WorldColor.values().length; i++) {
			GameView.draw.getPaint()
					.setColor(WorldColor.values()[i].colorValue);
			if (colorResource[i] == 0) {
				GameView.draw.getPaint().setTextSize(15);
			} else {
				GameView.draw.getPaint().setTextSize(18);
			}
			canvas.drawText(Integer.toString(colorResource[i]), x, y,
					GameView.draw.getPaint());
			GameView.draw.getPaint().setTextSize(18);
			if (colorAmount[i] == 0) {
				GameView.draw.getPaint().setTextSize(15);
			} else {
				GameView.draw.getPaint().setTextSize(18);
			}
			canvas.drawText(Integer.toString(colorAmount[i]), x, y
					+ ELEMENT_GROUP_SPACING, GameView.draw.getPaint());
			x += ELEMENT_X_SPACING;
			if (x > GameView.xMax - ELEMENT_X_MARGIN) {
				y += ELEMENT_Y_SPACING;
				x = INITIAL_X_ELEMENT_OFFSET;
			}
		}
		GameView.draw.getPaint().setTextSize(18);
		x = 30;
		y = 225 + yOffset;

		int LINE_HEIGHT = 35;

		threadCheck();
		for (GameObject object : objects) {
			object.x = x;
			object.y = y;
			object.draw(canvas);
			x += 25;
			if (x > GameView.xMax - 10) {
				x = 30;
				y += LINE_HEIGHT;
			}
		}
	}

	public void ForEach(Func0<GameObject> func0) {
		threadCheck();
		for (GameObject object : objects) {
			func0.call(object);
		}
	}

	public <A> void ForEach(Func1<GameObject, A> func1) {
		threadCheck();
		for (GameObject object : objects) {
			func1.call(object, func1.y);
		}
	}

	public <A, B> void ForEach(Func2<GameObject, A, B> func2) {
		threadCheck();
		for (GameObject object : objects) {
			func2.call(object, func2.y, func2.z);
		}
	}

	public <A, B, C> void ForEach(Func3<GameObject, A, B, C> func2) {
		threadCheck();
		for (GameObject object : objects) {
			func2.call(object, func2.y, func2.z, func2.a);
		}
	}

	int gather(WorldColor c, int amt) {
		return gather(c, amt, false);
	}

	int gather(WorldColor c, int amt, boolean ignoreCapacity) {
		// move a resource to the gathered array
		if (colorResource[c.ordinal()] < amt) {
			amt = colorResource[c.ordinal()];
		}

		int capacity = getCapacity(c);
		if (!ignoreCapacity && colorAmount[c.ordinal()] + amt > capacity)
			amt = capacity - colorAmount[c.ordinal()];
		if (amt < 0)
			amt = 0;

		Log.i("_", "gathered " + amt + "/" + colorResource[c.ordinal()]
				+ " units of " + c
				+ (ignoreCapacity ? "" : " (capacity " + capacity + ")\n"));
		colorResource[c.ordinal()] -= amt;
		colorAmount[c.ordinal()] += amt;
		colors.add(c);
		return amt;
	}

	int getCapacity(WorldColor c) {
		Value<Integer> retVal = new Value<Integer>(0);
		ForEach(new Func2<GameObject, Value<Integer>, WorldColor>(retVal, c) {
			@Override
			void call(GameObject t, Value<Integer> value, WorldColor c) {
				t.tallyCapacity(value, c);
			}
		});
		// Log.i("_", c + " capacity for cell " + this + " is " +
		// retVal.value
		// + "\n");
		return retVal.value;
	}

	/**
	 * Gets the amount of color that has been produced for immediate use in this
	 * cell.
	 * 
	 * @param color
	 *            the color to check
	 * @return the amount of color available for use
	 */
	int getColorAvailable(WorldColor color) {
		return colorAmount[color.ordinal()];
	}

	int getColorResourceAvailable(WorldColor c) {
		return colorResource[c.ordinal()];
	}

	int getPopulation() {
		Value<Integer> retVal = new Value<Integer>(0);
		ForEach(new Func1<GameObject, Value<Integer>>(retVal) {
			@Override
			void call(GameObject t, Value<Integer> count) {
				t.doCountBlobs(count);
			}
		});
		return retVal.value;
	}

	int getPopulation(PlayerData player) {
		Value<Integer> retVal = new Value<Integer>(0);
		ForEach(new Func2<GameObject, Value<Integer>, PlayerData>(retVal,
				player) {
			@Override
			void call(GameObject t, Value<Integer> count, PlayerData player) {
				t.doCountPlayerBlobs(count, player);
			}
		});
		return retVal.value;
	}

	int getStructureCount() {
		Value<Integer> count = new Value<Integer>(0);
		ForEach(new Func1<GameObject, Value<Integer>>(count) {
			@Override
			void call(GameObject t, Value<Integer> value) {
				t.doCountStructures(value);
			}
		});
		return count.value;
	}

	/**
	 * Is a color available for use in this cell?
	 * 
	 * @param color
	 *            the color to check
	 * @return true if GetColorAvailable would return a non-zero value
	 */
	boolean isColorAvailable(WorldColor color) {
		return colors.contains(color);
	}

	boolean isResourceAvailable(WorldColor c) {
		return isResourceAvailable(c, 1);
	}

	boolean isResourceAvailable(WorldColor c, int amt) {
		return colorResource[c.ordinal()] >= amt;
	}

	void plant(WorldColor c, int amt) {
		colorResource[c.ordinal()] += amt;
	}

	public void process(final ArrayList<Processor> processors) {
		ForEach(new Func0<GameObject>() {
			@Override
			void call(GameObject t) {
				t.process(WorldCell.this, processors);
			}
		});
	}

	/**
	 * Create amount of color out of thin air, up to the maximum storage
	 * capacity for this cell.
	 * 
	 * NOTE: Producing a negative amount is the same as consuming the opposite
	 * amount.
	 * 
	 * @param color
	 *            the color to produce
	 * @param amount
	 *            the amount to create
	 * @return amount actually produced (amount limited by capacity)
	 */
	int produce(WorldColor color, int amount) {

		if (amount < 0) {
			return -consume(color, -amount);
		}

		int capacity = getCapacity(color);
		int colorIndex = color.ordinal();
		if (colorAmount[colorIndex] > capacity) {
			// somehow we're already over
			amount = 0;
			colorAmount[colorIndex] = capacity;
		} else {
			colorAmount[colorIndex] += amount;
			if (colorAmount[colorIndex] > capacity) {
				amount -= (colorAmount[colorIndex] - capacity);
				colorAmount[colorIndex] = capacity;
			}
		}
		Log.i("PRODUCE", "Cell now has " + colorAmount[colorIndex] + "/"
				+ capacity + " " + color);
		colors.add(color);
		return amount;
	}

	int remainingCapacity(WorldColor c) {
		int capacity = getCapacity(c);
		return Math.max(0, capacity - colorAmount[c.ordinal()]);
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName() + ":" + (this.hashCode() & 0xFF);
	}

	void Travel(Blob potentialTraveller, WorldCell cell) {
		Log.i("TRAVEL", "** Start Travel Search**");
		Road pCur = null;
		for (Road road : roads) {
			Log.i("TRAVEL", "Find road: " + road + " -- " + road.color);
			if (road.color == potentialTraveller.playerData.getRoadColor()) {
				if (road.b == cell || road.a == cell) {
					pCur = road;
					if (pCur.curObject == null) {
						break;
					}
				}
			}
		}

		if (pCur == null) {
			Log.i("TRAVEL",
					"NO " + potentialTraveller.playerData.getRoadColor()
							+ " ROAD");
			return;
		}

		threadCheck();
		if (!objects.contains(potentialTraveller)) {
			Log.i("TRAVEL", "NO " + potentialTraveller + " IN " + this);
			return;
		}

		if (!potentialTraveller.isMovable()) {
			Log.i("TRAVEL", "NOT MOVABLE Traveller: " + potentialTraveller);
			return;
		}

		if (pCur.curObject != null
				|| (pCur.roadForOtherCell != null && pCur.roadForOtherCell.curObject != null)) {
			Log.i("TRAVEL", pCur + " is in use already.");
			ElementsView.showMessage("ROAD is in use already.");
			return;
		}

		pCur.curObject = potentialTraveller;
	}

	void update() {
		if (!active) {
			return;
		}
		Log.i("_", "    == update " + this);
		ForEach(new Func0<GameObject>() {
			@Override
			void call(GameObject t) {
				t.farm();
			}
		});
		ForEach(new Func0<GameObject>() {
			@Override
			void call(GameObject t) {
				t.produce();
			}
		});
		ForEach(new Func0<GameObject>() {
			@Override
			void call(GameObject t) {
				t.consume();
			}
		});
		/*
		 * // the old consume code gmWorldObjNode*pCur = m_Head; while(pCur) {
		 * gmWorldObjNode* pNext = pCur.m_Next; pCur.GetData().Consume(); pCur =
		 * pNext; }
		 */
		ForEach(new Func0<GameObject>() {
			@Override
			void call(GameObject t) {
				t.update();
			}
		});

		for (GameObject object : deferredObjectAdds) {
			objects.add(object);
		}
		deferredObjectAdds.clear();

		deleteDeadObjects();

		// debug prints
		/*
		 * Log.i("_", "cell " + this + " gmWorldColor.HOT_PINK (" +
		 * gmWorldColor.HOT_PINK.ordinal() + ") capacity: [" +
		 * m_ColorAmount[gmWorldColor.HOT_PINK.ordinal()] + "/" +
		 * GetCapacity(gmWorldColor.HOT_PINK) + "]\n"); Log.i("_", "cell " +
		 * this + " gmWorldColor.AQUAMARINE (" +
		 * gmWorldColor.AQUAMARINE.ordinal() + ") capacity: [" +
		 * m_ColorAmount[gmWorldColor.AQUAMARINE.ordinal()] + "/" +
		 * GetCapacity(gmWorldColor.AQUAMARINE) + "]\n");
		 */
	}

	void WriteHTML(String f) {
		Log.d(f, "<td>\nAmount:<br>\n<table border=1><tr>\n");

		for (WorldColor color : WorldColor.values())
			if (colorAmount[color.ordinal()] > 0) {
				Log.d(f, "<td bgcolor='" + color.colorValue + "'>&nbsp;"
						+ colorAmount[color.ordinal()] + "&nbsp;</td>\n");
			}
		Log.d(f, "</tr></table><br>\n");
		Log.d(f, "Resources:<br>\n<table border=1><tr>\n");
		int j = 0;
		for (WorldColor color : WorldColor.values())
			if (colorResource[color.ordinal()] > 0) {
				Log.d(f, "<td bgcolor='" + color.colorValue + "'>&nbsp;"
						+ colorResource[color.ordinal()] + "&nbsp;</td>\n");
				j++;
				if (j > 10) {
					j = 0;
					Log.d(f, "</tr><tr>");
				}
			}
		Log.d(f, "</tr></table><br>Player:<br><table border=1><tr>\n");
		Log.d(f, "</tr></table><br>\n");
		Log.d(f, "Population: " + getPopulation() + "<br>\n");
		Log.d(f, "Structure: " + getStructureCount() + "<br>\n");
		Log.d(f, "</td>\n");
	}
};
