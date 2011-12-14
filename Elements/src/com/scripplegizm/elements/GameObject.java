package com.scripplegizm.elements;

import java.util.ArrayList;

import android.graphics.Canvas;
import android.graphics.Color;
import android.util.Log;

import com.scripplegizm.elements.ElementsUtils.Value;
import com.scripplegizm.elements.Scenario.Rule.Processor;
import com.scripplegizm.gameutils.Point2D;

abstract class GameObject {
	/*
	 * static gmObject create(Value<gmWorldColor> value, gmWorldCell cell,
	 * gmPlayerData playerdata) { gmObject ret = new gmObject(value.value,
	 * playerdata); ret.SetCurrentCell(cell); return ret; }
	 */
	enum WorldColor {
		NONE(0), 
		IVORY(0xFFFFF0), 
		BEIGE(0xF5F5DC), // Beige
		WHEAT(0xF5DEB3), // Wheat
		TAN(0xD2B48C), // Tan
		KHAKI(0xC3B091), // Khaki
		SILVER(0xC0C0C0), // Silver
		GRAY(0x808080), // Gray
		CHARCOAL(0x464646), // Charcoal
		NAVY_BLUE(0x000080), // Navy Blue
		ROYAL_BLUE(0x084C9E), // Royal Blue
		MEDIUM_BLUE(0x0000CD), // Medium Blue
		AZURE(0x007FFF), // Azure
		CYAN(0x00FFFF), // Cyan
		AQUAMARINE(0x7FFFD4), // Aquamarine
		TEAL(0x008080), // Teal
		FOREST_GREEN(0x228B22), // Forest Green
		OLIVE(0x808000), // Olive
		CHARTRUSE(0x7FFF00), // Chartreuse
		LIME(0xBFFF00), // Lime
		GOLDEN(0xFFD700), // Golden
		GOLDENROD(0xDAA520), // Goldenrod
		CORAL(0xFF7F50), // Coral
		SALMON(0xFA8072), // Salmon
		HOT_PINK(0xFC0FC0), // Hot Pink
		FUCHSIA(0xFF77FF), // Fuchsia
		PUCE(0xCC8899), // Puce
		MAUVE(0xE0B0FF), // Mauve
		LAVENDER(0xB57EDC), // Lavender
		PLUM(0x843179), // Plum
		INDIGO(0x4B0082), // Indigo
		MAROON(0x800000), // Maroon
		CRIMSON(0xDC143C);// Crimson

		WorldColor(int c) {
			colorValue = Color.argb(255, 0, 0, 0) + c;
			noAlphaColor = c;
		}

		int noAlphaColor;
		int colorValue;
	}

	public String toString() {
		return this.getClass().getSimpleName() + ":"
				+ (this.hashCode() & 0xFF);
	}

	static int count = 0;
	static public final int OBJECT_DEFAULT_ENERGY = 7;

	GameObject(WorldColor c, PlayerData playerdata) {
		color = c;
		playerData = playerdata;
		energy = OBJECT_DEFAULT_ENERGY;
		readyForDeletion = false;
		Log.i("O", "\n+ create obj " + this + " (" + (++count) + ")\n");
	}

	// foreachable functions (non-virtual)
	void tallyCapacity(Value<Integer> value, WorldColor c) {
		value.value += getCapacity(c);
	}

	void doCountPlayerBlobs(Value<Integer> count, PlayerData player) {
		count.value += (isBlob() && playerData == player) ? 1 : 0;
	}

	void doCountPlayerStructures(Value<Integer> count, PlayerData player) {
		count.value += (isStructure() && playerData == player) ? 1 : 0;
	}

	void doCountBlobs(Value<Integer> count) {
		count.value += isBlob() ? 1 : 0;
	}

	void doCountStructures(Value<Integer> count) {
		count.value += isStructure() ? 1 : 0;
	}

	// @Override interface
	boolean isStructure() {
		return false;
	}

	boolean isMovable() {
		return true;
	}

	boolean isBlob() {
		return false;
	}

	String getName() {
		return "OBJECT";
	}

	void setCurrentCell(WorldCell cell) {
	}

	void update() {
	}

	void produce() {
	}

	void consume() {
	}

	int getCapacity(WorldColor color) {
		return 0;
	}

	WorldColor getColor() {
		return color;
	}

	int getCost() {
		return 0;
	} // base object is free

	boolean AddEnergy(int amt) {
		energy += amt;
		if (energy <= 0) {
			readyForDeletion = true;
			die(false);
			return false;
		}
		return true;
	}

	public void onMove(WorldCell source, WorldCell dest) {
		// pack up any resources we are holding and move them.
	}

	void die(boolean conversion) {
		Log.i("_", getName() + " " + this + " has died!\n");
	}

	WorldColor color;
	PlayerData playerData;
	int energy;
	boolean readyForDeletion;

	int x = 0, y = 0;
	
	abstract public void draw(Canvas canvas);

	public void farm() {
	}

	public void onRemove() {
	}

	public boolean onClick(GameObject prevObject) {
		return true;
	}

	public void click(Value<Float> dist2, Point2D pos, Value<GameObject> foundObj) {
		//Log.i("CLICK", "OBJECT: " + this);
		Point2D objPos = new Point2D(x, y);
		float localDist2 = pos.dist2(objPos);
		if (localDist2 < dist2.value) {
			dist2.value = localDist2;
			foundObj.value = this;
		}
	}

	public void deselect() {
	}

	public void process(WorldCell cell, ArrayList<Processor> processors) {
		for (Processor processor : processors) {
			processor.accept(cell, this);
		}
	}

	public void doAction(int actionIndex) {
	}

	public String getActionCaption(int actionIndex) {
		return null;
	}
};
