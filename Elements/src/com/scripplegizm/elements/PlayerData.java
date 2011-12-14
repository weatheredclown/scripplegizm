package com.scripplegizm.elements;

import java.util.ArrayList;

import android.util.Log;

import com.scripplegizm.elements.Blob.Banker;
import com.scripplegizm.elements.Blob.Builder;
import com.scripplegizm.elements.Blob.FactoryWorker;
import com.scripplegizm.elements.Blob.Farmer;
import com.scripplegizm.elements.Blob.Gatherer;
import com.scripplegizm.elements.Blob.Eater;
import com.scripplegizm.elements.Blob.Paver;
import com.scripplegizm.elements.Blob.ToxicCollector;
import com.scripplegizm.elements.Blob.Carrier;
import com.scripplegizm.elements.GameObject.WorldColor;
import com.scripplegizm.elements.Structure.Factory;
import com.scripplegizm.elements.WorldCell.AddMode;

class PlayerData {
	enum ColorRole {
		LIVING(0), FOOD(1), BRICKS(2), TOXIC(3), ROAD(4), TECH(5), FUEL(6), MONEY(
				7);

		int index;

		ColorRole(int i) {
			index = i;
		}
	}

	public String toString() {
		return getClass().getSimpleName() + ":" + (hashCode() & 0xFF);
	}

	PlayerData copy(PlayerData that) {
		colorArray = new ArrayList<WorldColor>(that.colorArray);
		return this;
	}

	WorldColor getLivingColor() {
		return colorArray.get(ColorRole.LIVING.index);
	}

	WorldColor getFoodColor() {
		return colorArray.get(ColorRole.FOOD.index);
	}

	WorldColor getStructureColor() {
		return colorArray.get(ColorRole.BRICKS.index);
	}

	WorldColor getToxicColor() {
		return colorArray.get(ColorRole.TOXIC.index);
	}

	WorldColor getRoadColor() {
		return colorArray.get(ColorRole.ROAD.index);
	}

	WorldColor getTechColor() {
		return colorArray.get(ColorRole.TECH.index);
	}

	WorldColor getFuelColor() {
		return colorArray.get(ColorRole.FUEL.index);
	}

	WorldColor getMoneyColor() {
		return colorArray.get(ColorRole.MONEY.index);
	}

	void setBlobLivingColor(WorldColor color) {
		colorArray.set(ColorRole.LIVING.index, color);
	}

	void setBlobFoodColor(WorldColor color) {
		colorArray.set(ColorRole.FOOD.index, color);
	}

	void setStructureColor(WorldColor color) {
		colorArray.set(ColorRole.BRICKS.index, color);
	}

	void setToxicColor(WorldColor color) {
		colorArray.set(ColorRole.TOXIC.index, color);
	}

	void setRoadColor(WorldColor color) {
		colorArray.set(ColorRole.ROAD.index, color);
	}

	void setTechColor(WorldColor color) {
		colorArray.set(ColorRole.TECH.index, color);
	}

	void setFuelColor(WorldColor color) {
		colorArray.set(ColorRole.FUEL.index, color);
	}

	void setMoneyColor(WorldColor color) {
		colorArray.set(ColorRole.MONEY.index, color);
	}

	static final int STARTING_ACTIONS = 2;

	int actions = STARTING_ACTIONS;
	int id;
	ArrayList<WorldColor> colorArray = new ArrayList<WorldColor>();

	PlayerData() {
		Log.i("_", "Create " + this);
		for (@SuppressWarnings("unused")
		ColorRole role : ColorRole.values()) {
			colorArray.add(WorldColor.NONE);
		}
	}

	public Gatherer createGatherer(WorldColor gatherColor) {
		return new Gatherer(getLivingColor(), null, this, gatherColor);
	}

	public FactoryWorker createFactoryWorker(Factory factory) {
		return new FactoryWorker(getLivingColor(), null, this, factory);
	}

	public Builder AddBuilder(WorldCell cell) {
		if (cell.canAddBlob(this, AddMode.REGULAR_CONSUMER)) {
			Builder object = new Builder(getLivingColor(), null, this);
			cell.addBlob(object);
			return object;
		}
		return null;
	}

	public Paver AddPaver(WorldCell cell) {
		if (cell.canAddBlob(this, AddMode.REGULAR_CONSUMER)) {
			Paver object = new Paver(getLivingColor(), null, this);
			cell.addBlob(object);
			return object;
		}
		return null;
	}

	public ToxicCollector AddToxicCollector(WorldCell cell) {
		if (cell.canAddBlob(this, AddMode.TOXIN_GATHERER)) {
			ToxicCollector object = new ToxicCollector(getLivingColor(), null,
					this);
			cell.addBlob(object);
			return object;
		}
		return null;
	}

	public Gatherer AddGatherer(WorldCell cell, WorldColor gatherColor) {
		if (cell.canAddBlob(this,
				gatherColor == getFoodColor() ? AddMode.FOOD_GATHERER
						: AddMode.REGULAR_CONSUMER)) {
			Gatherer object = createGatherer(gatherColor);
			cell.addBlob(object);
			return object;
		}
		return null;
	}

	public Farmer AddFarmer(WorldCell cell) {
		if (cell.canAddBlob(this, AddMode.FARMER)
				&& cell.absorbColor(getLivingColor(), Farmer.COST)) {
			return (Farmer) cell.addObject(new Farmer(getLivingColor(), cell,
					this)); // this will be added
		}
		return null;
	}

	public WorldColor getColor(ColorRole role) {
		return colorArray.get(role.index);
	}

	public FactoryWorker AddFactoryWorker(WorldCell cell, Factory factory) {
		if (cell.canAddBlob(this, AddMode.REGULAR_CONSUMER)) {
			FactoryWorker object = createFactoryWorker(factory);
			cell.addBlob(object);
			return object;
		}
		return null;
	}

	public Eater AddEater(WorldCell cell) {
		if (cell.canAddBlob(this, AddMode.REGULAR_CONSUMER)) {
			Eater object = new Eater(getLivingColor(), null, this);
			cell.addBlob(object);
			return object;
		}
		return null;
	}

	public Carrier AddCarrier(WorldCell cell, WorldColor transportColor) {
		if (cell.canAddBlob(this, AddMode.REGULAR_CONSUMER)) {
			Carrier object = new Carrier(getLivingColor(), null, this,
					transportColor);
			cell.addBlob(object);
			return object;
		}
		return null;
	}

	public Banker AddBanker(WorldCell cell) {
		if (cell.canAddBlob(this, AddMode.REGULAR_CONSUMER)) {
			Banker object = new Banker(getLivingColor(), null, this);
			cell.addBlob(object);
			return object;
		}
		return null;
	}

	public ColorRole getColorRole(WorldColor storageColor) {
		for (int i = 0; i < colorArray.size(); i++) {
			if (colorArray.get(i) == storageColor) {
				return ColorRole.values()[i];
			}
		}
		return null;
	}

	public String getColorRoleName(WorldColor color) {
		ColorRole role = getColorRole(color);
		return role == null ? color.toString() : role.toString();
	}
}
