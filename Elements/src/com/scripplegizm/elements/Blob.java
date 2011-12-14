package com.scripplegizm.elements;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.util.Log;

import com.scripplegizm.elements.Device.FoodDuplicator;
import com.scripplegizm.elements.Device.ToxinIncinerator;
import com.scripplegizm.elements.ElementsUtils.Func0;
import com.scripplegizm.elements.ElementsUtils.Value;
import com.scripplegizm.elements.PlayerData.ColorRole;
import com.scripplegizm.elements.Structure.Factory;
import com.scripplegizm.elements.Structure.Storehouse;
import com.scripplegizm.gameutils.GameView;
import com.scripplegizm.gameutils.GameView.ExtraMath;

abstract class Blob extends GameObject {

	static final int FARMER_CROP_PRODUCTION = 6;

	static class Farmer extends Blob {

		public static final int COST = 5;

		@Override
		public void doAction(int action) {
			switch (action) {
			case 1:
				plant();
				break;
			}
		}

		@Override
		public String getActionCaption(int action) {
			switch (action) {
			case 1:
				return "Plant";
			}
			return null;
		}

		@Override
		boolean makesFood() {
			return true;
		}

		@Override
		public void farm() {
			int productionAmt = getCapacity(playerData.getFoodColor());
			if (productionAmt == 0) {
				if (!currentCell.isResourceAvailable(playerData.getFoodColor())) {
					if (!starving) {
						plant();
						Log.i("_", this + " plant " + playerData.getFoodColor()
								+ " food");
					} else {
						Log.i("_", this + " is too hungry to plant "
								+ playerData.getFoodColor());
					}
				} else {
					Log.i("_",
							"Too toxic (" + playerData.getToxicColor()
									+ ") for " + this + " to farm "
									+ playerData.getFoodColor());
				}
			} else {
				int produced = currentCell.produce(playerData.getFoodColor(),
						productionAmt);
				Log.i("_",
						this + " produce " + produced + " "
								+ playerData.getFoodColor() + " food in "
								+ currentCell);
			}
		}

		@Override
		public String getDrawLetter() {
			return "F";
		}

		Farmer(WorldColor c, WorldCell parent, PlayerData playerdata) {
			super(c, parent, playerdata);
		}

		@Override
		int getCapacity(WorldColor c) {
			if (c == playerData.getFoodColor()) {
				int toxins = currentCell.getColorResourceAvailable(playerData
						.getToxicColor());
				float multi = ExtraMath.clamp(0.0f, (float) (64 - toxins)
						/ (float) 64, 1.0f);
				int capacity = (int) (FARMER_CROP_PRODUCTION * multi);
				// Log.i("FARM", "farmer multiplier: " + multi);
				// Log.i("_", this + " capacity: " + capacity + " [food " + food
				// + " / toxic " + toxins + " (" + multi + " multi)]\n");
				return capacity;
			}
			return 0;
		}

		@Override
		String getName() {
			return "FARMER";
		}

		// take food from the supplies and put it into the ground
		@Override
		void plant() {
			if (currentCell.isColorAvailable(playerData.getFoodColor())) {
				currentCell.consume(playerData.getFoodColor(), 1);
				currentCell.plant(playerData.getFoodColor(), 1);
			}
		}

		@Override
		void produce() {
		}
	};

	static abstract class ThingMaker<T extends GameObject> extends Blob {
		ThingMaker(WorldColor c, WorldCell parent, PlayerData playerdata) {
			super(c, parent, playerdata);
			progress = 0;
			curProject = null;
		}

		@Override
		public void draw(Canvas canvas) {
			super.draw(canvas);
			if (curProject != null) {
				float pct = (float) progress / (float) curProject.getCost();
				curProject.x = x;
				curProject.y = (int) (y - (pct * 20));
				curProject.draw(canvas);
			}
		}

		@Override
		public String getDrawLetter() {
			return "B";
		}

		@Override
		boolean isMovable() {
			return curProject == null;
		}

		T GetProject() {
			return curProject;
		}

		@Override
		void update() {
			if (curProject != null) {
				if (starving) {
					Log.i("_", this + " is starving and can't work on "
							+ curProject);
					return;
				}
				Log.i("_", "      == update " + this);
				if (progress < curProject.getCost()) {
					// continue the project
					int amt = curProject.getCost() - progress;
					Log.i("_", getName() + " " + this + " needs " + amt
							+ " units of " + this.GetProject().getColor()
							+ "\n");
					int progressAmt = currentCell.consume(GetProject()
							.getColor(), amt);
					if (progressAmt == 0) {
						ElementsView.showMessage("Need more "
								+ this.GetProject().getColor() + " to finish "
								+ GetProject().getName());
					} else {
						progress += progressAmt;
					}
				}

				// could be true regardless of if above executed
				if (progress == curProject.getCost()) {
					Log.i("_", curProject.getName() + " done\n");
					this.FinishProject();
					curProject = null;
				}
			}
		}

		void SetProject(T pProject) {
			curProject = pProject;
			progress = 0;
		}

		void FinishProject() {
			currentCell.addObject(curProject, true);
		}

		@Override
		int getCapacity(WorldColor c) {
			return (curProject != null && c == curProject.getColor()) ? curProject
					.getCost() : 0;
		}

		@Override
		void produce() {
			if (curProject != null) {
				Log.i("_", this + " gather " + curProject.getColor() + " from "
						+ currentCell + " for " + curProject);
				currentCell.gather(curProject.getColor(), 1);
			}
		}

		int progress;
		T curProject; // we're building this
	}

	static class Builder extends ThingMaker<Structure> {

		@Override
		public void doAction(int action) {
			switch (action) {
			case 1:
				BuildStorehouse(ElementsView.curRole);
				break;
			case 2:
				BuildFactory(WorldColor.NONE, WorldColor.NONE,
						playerData.getColor(ElementsView.curRole));
				break;
			}
		}

		@Override
		public String getActionCaption(int action) {
			switch (action) {
			case 1:
				return "Build Storehouse";
			case 2:
				return "Build Factory";
			}
			return null;
		}

		Builder(WorldColor c, WorldCell parent, PlayerData playerdata) {
			super(c, parent, playerdata);
		}

		@Override
		String getName() {
			String retVal = "BUILDER";
			if (this.curProject != null) {
				retVal += " building " + curProject.getName();
			}
			return retVal;
		}

		public Storehouse BuildStorehouse(ColorRole role) {
			Storehouse storehouse = new Storehouse(playerData.getColor(role),
					playerData);
			SetProject(storehouse);
			return storehouse;
		}

		public Factory BuildFactory(WorldColor input1, WorldColor input2,
				WorldColor output) {
			Factory factory = new Factory(input1, input2, output, playerData);
			SetProject(factory);
			return factory;
		}
	};

	static class Gatherer extends Blob {

		@Override
		public void doAction(int action) {
			switch (action) {
			case 1:
				int cellCapacity = currentCell.remainingCapacity(gatherColor);
				int gatherAmt = getCapacity(gatherColor);
				Log.i("_", this + " check " + cellCapacity + " vs " + gatherAmt);
				if (cellCapacity >= gatherAmt) {
					SetGatherColor(playerData.getColor(ElementsView.curRole));
				} else {
					ElementsView.showMessage("Sorry! Gatherer is holding "
							+ gatherColor);
				}
				break;
			}
		}

		@Override
		public String getActionCaption(int action) {
			switch (action) {
			case 1:
				return "Change Gather";
			}
			return null;
		}

		Gatherer(WorldColor c, WorldCell parent, PlayerData playerdata,
				WorldColor gatherColor) {
			super(c, parent, playerdata);
			SetGatherColor(gatherColor);
		}

		@Override
		String getName() {
			return "GATHERER - " + playerData.getColorRoleName(gatherColor);
		}

		@Override
		public String getDrawLetter() {
			return "G";
		}

		void SetGatherColor(WorldColor c) {
			gatherColor = c;
		}

		@Override
		int getCapacity(WorldColor c) {
			if (c == gatherColor) {
				return 2;
			}
			return 0;
		}

		@Override
		boolean makesFood() {
			return gatherColor != playerData.getFoodColor();
		}

		@Override
		void produce() {
			if (starving && !makesFood()) {
				Log.i("starving", this + " is starving!  Can't do work.");
			}
			if (gatherColor != WorldColor.NONE) {
				Log.i("_", this + " gather " + gatherColor + " from "
						+ currentCell);
				currentCell.gather(gatherColor, 2);
				if (gatherColorDeadly()) {
					Log.i("_", "Collecting " + gatherColor + " will kill " + this);
					this.readyForDeletion = true;
				}
			}
		}

		public boolean gatherColorDeadly() {
			return gatherColor == playerData.getToxicColor();
		}

		WorldColor gatherColor;
	};

	static class ToxicCollector extends Gatherer {
		ToxicCollector(WorldColor c, WorldCell parent, PlayerData playerdata) {
			super(c, parent, playerdata, playerdata.getToxicColor());
		}

		public boolean gatherColorDeadly() {
			return gatherColor != playerData.getToxicColor();
		}
		
		@Override
		public String getDrawLetter() {
			return "T";
		}

		@Override
		String getName() {
			return "TOXICCOLLECTOR";
		}
	};

	static class Eater extends Blob {
		public Eater(WorldColor c, WorldCell parent, PlayerData playerdata) {
			super(c, parent, playerdata);
		}

		@Override
		public String getActionCaption(int action) {
			switch (action) {
			case 1:
				return "Eat Food";
			case 2:
				return "Eat Building";
			}
			return null;
		}

		@Override
		public String getName() {
			return "EATER";
		}
		
		@Override
		public void doAction(int action) {
			switch (action) {
			case 1:
				if (0 != currentCell.consume(this.playerData.getFoodColor(), 1)) {
					justAte();
				} else {
					ElementsView.showMessage("No food to eat!");
				}
				break;
			case 2: {
				final Value<Boolean> done = new Value<Boolean>(false);
				currentCell.ForEach(new Func0<GameObject>() {
					@Override
					void call(GameObject object) {
						if (!done.value
								&& object.isStructure()
								&& object.playerData.getStructureColor() == playerData
										.getFoodColor()) {
							Log.i("_", "eat gingerbread " + object.getColor()
									+ " " + object.getName()
									+ " to ward off starvation\n");
							object.AddEnergy(-1);
							done.value = true;
						}
					}
				});
				if (done.value) {
					justAte();
				} else {
					ElementsView.showMessage("No buildings to eat!");
				}
				break;
			}
			}
		}

		@Override
		public String getDrawLetter() {
			return "E";
		}
	}

	static class Paver extends ThingMaker<Road> {
		Paver(WorldColor c, WorldCell parent, PlayerData playerdata) {
			super(c, parent, playerdata);
		}

		@Override
		public String getActionCaption(int action) {
			switch (action) {
			case 1:
				return "Make Road";
			}
			return null;
		}

		@Override
		public void doAction(int action) {
			switch (action) {
			case 1:
				// This will have to change if there are more cells.
				MakeRoad(ElementsView.cell0, ElementsView.cell1);
				break;
			}
		}

		@Override
		String getName() {
			return "PAVER";
		}

		@Override
		public String getDrawLetter() {
			return "P";
		}

		void MakeRoad(WorldCell a, WorldCell b) {
			this.SetProject(new Road(playerData.getRoadColor(), a, b,
					playerData));
		}

		@Override
		void FinishProject() {
			// finish the project
			curProject.connectCells();
		}
	};

	WorldCell currentCell;
	boolean starving = false;
	int timeToEat = 0;

	@Override
	public void draw(Canvas canvas) {
		Paint paint = GameView.draw.getPaint();
		paint.setStyle(Style.FILL);
		paint.setColor(getColor().noAlphaColor
				+ (starving ? Color.argb(128, 0, 0, 0) : Color.argb(255, 0, 0,
						0)));
		canvas.drawCircle(x, y, 5, paint);
		paint.setStyle(Style.STROKE);
		paint.setColor(getColor().colorValue);
		canvas.drawCircle(x, y, 5, paint);
		drawLetter(canvas, x, y);
	}

	public abstract String getDrawLetter();

	public void drawLetter(Canvas canvas, int x, int y) {
		canvas.drawText(getDrawLetter(), x - 6, y + 20,
				GameView.draw.getPaint());
	}

	boolean makesFood() {
		return false;
	}

	public Blob(WorldColor c, WorldCell parent, PlayerData playerdata) {
		super(c, playerdata);
		currentCell = parent;
	}

	@Override
	void produce() {
	}

	void plant() {
	}

	int getNextTimeToEat() {
		return 3;
	}

	@Override
	void consume() {
		if (timeToEat > 0 && !starving) {
			timeToEat--;
			return;
		}
		starving = currentCell.consume(playerData.getFoodColor(), 1) == 0;
		final Value<Boolean> done = new Value<Boolean>(!starving);
		if (starving) {
			currentCell.ForEach(new Func0<GameObject>() {

				@Override
				void call(GameObject object) {
					if (!done.value
							&& object.isStructure()
							&& object.playerData.getStructureColor() == playerData
									.getFoodColor()) {
						Log.i("_", "eat gingerbread " + object.getColor() + " "
								+ object.getName()
								+ " to ward off starvation\n");
						object.AddEnergy(-1);
						done.value = true;
					}
				}
			});
			/*
			 * for (GameObject object : currentCell.objects) { }
			 */
		}

		if (!done.value) {
			Log.i("_", getColor() + " " + getName() + " " + this
					+ " can't find " + playerData.getFoodColor()
					+ " and is starving!");
			/*
			 * AddEnergy(-1); if (this.m_ReadyForDeletion) { // recycle
			 * m_CurrentCell.Plant(1, m_PlayerData.getBlobLivingColor()); }
			 */
		} else {
			justAte();
		}
	}

	public void justAte() {
		starving = false;
		energy = OBJECT_DEFAULT_ENERGY;
		timeToEat = this.getNextTimeToEat();
	}

	@Override
	void update() {
		// Produce();
		// Consume();
	}

	/**
	 * Called when moving into cell..
	 */
	@Override
	void setCurrentCell(WorldCell cur) {
		currentCell = cur;
	}

	@Override
	boolean isBlob() {
		return true;
	}

	static class FactoryWorker extends Blob {
		boolean working = true;

		@Override
		public void doAction(int action) {
			switch (action) {
			case 1:
				working = !working;
				break;
			}
		}

		@Override
		public String getActionCaption(int action) {
			switch (action) {
			case 1:
				return working ? "Working: YES" : "Working: NO";
			}
			return null;
		}

		@Override
		public String getDrawLetter() {
			return "W";
		}

		@Override
		void die(boolean conversion) {
			super.die(conversion);
			this.SetCurrentFactory(null);
		}

		@Override
		int getCapacity(WorldColor c) {
			if (this.currentFactory != null && this.currentFactory.output == c) {
				return 2; // A little bit of capacity for the sake of being able
							// to build.
			}
			return 0;
		}

		@Override
		public void onRemove() {
			super.onRemove();
			this.SetCurrentFactory(null);
		}

		/**
		 * Called when moving out of a cell.
		 */
		@Override
		public void onMove(WorldCell source, WorldCell dest) {
			Log.i("_", "move " + this);
			super.onMove(source, dest);
			this.SetCurrentFactory(null);
		}

		public FactoryWorker(WorldColor c, WorldCell parent,
				PlayerData playerdata, Factory factory) {
			super(c, parent, playerdata);
			SetCurrentFactory(factory);
		}

		@Override
		void produce() {
			super.produce();
			if (readyToGenerate) {
				readyToGenerate = false;
				Log.i("_", this + " producing " + currentFactory.output);
				currentCell.produce(currentFactory.output, 2);
			}
		}

		@Override
		String getName() {
			return "FACTORYWORKER";
		}

		@Override
		boolean makesFood() {
			return currentFactory.output == playerData.getFoodColor();
		}

		@Override
		void consume() {
			super.consume();
			if (working && currentFactory != null
					&& currentFactory.canProduce(currentCell)) {
				if (starving && !makesFood()) {
					Log.i("starving", this + " is starving!  Can't do work.");
					return;
				}
				int capacity = currentCell
						.remainingCapacity(currentFactory.output);
				Log.i("FACTORY", currentCell + " capacity for output "
						+ currentFactory.output + " is " + capacity);
				if (capacity == 0) {
					return;
				}
				readyToGenerate = true;
				currentCell.plant(currentFactory.input1, -2);
				currentCell.plant(currentFactory.input2, -2);
				// Toxic by-product?
				// m_CurrentCell.Plant(1, m_PlayerData.getToxicColor());
				Log.i("_", this + " mining " + currentFactory.input1 + " and "
						+ currentFactory.input2);
			}
		}

		Factory currentFactory = null;
		boolean readyToGenerate = false;

		public void SetCurrentFactory(Factory factory) {
			Log.i("_", this + " set factory: " + factory);
			if (currentFactory != factory) {
				produce(); // In case we were sitting on some resources.
				if (currentFactory != null) {
					currentFactory.RemoveWorker(this);
				}
			}
			currentFactory = factory;
			if (factory != null) {
				factory.AddWorker(this);
			}
		}

	};

	static class Banker extends Blob {

		public void buyIncinerator() {
			if (currentCell.getColorAvailable(playerData.getMoneyColor()) >= ToxinIncinerator.COST) {
				currentCell.consume(playerData.getMoneyColor(),
						ToxinIncinerator.COST);
				currentCell.addObject(new ToxinIncinerator(playerData
						.getTechColor(), playerData, currentCell));
			} else {
				ElementsView.showMessage("Need at least "
						+ ToxinIncinerator.COST + " money.");
			}
		}

		private void buyDuplicator() {
			if (currentCell.getColorAvailable(playerData.getMoneyColor()) >= FoodDuplicator.COST) {
				currentCell.consume(playerData.getMoneyColor(),
						FoodDuplicator.COST);
				currentCell.addObject(new FoodDuplicator(playerData
						.getTechColor(), playerData, currentCell));
			} else {
				ElementsView.showMessage("Need at least " + FoodDuplicator.COST
						+ " money.");
			}
		}

		@Override
		public void doAction(int action) {
			switch (action) {
			case 1:
				BuyAction();
				break;
			case 2:
				buyIncinerator();
				break;
			case 3:
				buyDuplicator();
				break;
			}
		}

		@Override
		public String getActionCaption(int action) {
			switch (action) {
			case 1:
				return "Buy Action";
			case 2:
				return "Buy Incinerator";
			case 3:
				return "Buy Duplicator";
			}
			return null;
		}

		public Banker(WorldColor c, WorldCell parent, PlayerData playerdata) {
			super(c, parent, playerdata);
		}

		@Override
		public String getDrawLetter() {
			return "$";
		}

		@Override
		String getName() {
			return "BANKER";
		}

		void BuyAction() {
			if (currentCell.isColorAvailable(playerData.getMoneyColor())) {
				currentCell.consume(playerData.getMoneyColor(), 1);
				playerData.actions++;
				Log.i("_", "Buy and action for money.");
			} else {
				ElementsView.showMessage("No available "
						+ playerData.getMoneyColor() + " money.");
			}
		}
	}

	static class Carrier extends Blob {
		@Override
		public void doAction(int action) {
			switch (action) {
			case 1:
				enabled = !enabled;
				break;
			case 2:
				setTransportColor(playerData.getColor(ElementsView.curRole));
				break;
			}
		}

		@Override
		public String getActionCaption(int action) {
			switch (action) {
			case 1:
				return enabled ? "Carry: YES" : "Carry: NO";
			case 2:
				return "Change Carry";
			}
			return null;
		}

		static final int MIN_TRANSPORT_CAPACITY = 2;

		private WorldColor transportColor = WorldColor.NONE;
		private int holdingColorAmt = 0;
		boolean enabled = true;

		// private int transportCapacity = MIN_TRANSPORT_CAPACITY;

		public Carrier(WorldColor c, WorldCell parent, PlayerData playerdata,
				WorldColor transportColor) {
			super(c, parent, playerdata);
			setTransportColor(transportColor);
		}

		@Override
		public String getDrawLetter() {
			return "C";
		}

		@Override
		String getName() {
			return "CARRIER - " + playerData.getColorRoleName(transportColor);
		}

		void setTransportColor(WorldColor c) {
			this.transportColor = c;
		}

		@Override
		public void onMove(WorldCell source, WorldCell dest) {
			super.onMove(source, dest);
			if (!enabled) {
				return;
			}
			int available = this.currentCell.getColorAvailable(transportColor);
			holdingColorAmt = Math.min(available, MIN_TRANSPORT_CAPACITY);
			int destCapacity = dest.remainingCapacity(transportColor);
			holdingColorAmt = Math.min(holdingColorAmt, destCapacity);
			if (holdingColorAmt > 0) {
				currentCell.consume(transportColor, holdingColorAmt);
			} else {
				if (available == 0) {
					ElementsView.showMessage("No " + transportColor + " available for carry.");
				} else {
					ElementsView.showMessage("No room to store carried " + transportColor + ".");
				}
			}
		}

		@Override
		void setCurrentCell(WorldCell cur) {
			super.setCurrentCell(cur);
			if (!enabled) {
				return;
			}
			if (holdingColorAmt > 0) {
				currentCell.produce(transportColor, holdingColorAmt);
				holdingColorAmt = 0;
			}
		}

	}

	/*
	 * 
	 * 
	 * class BlobSoldier extends gmBlob {
	 * 
	 * };
	 * 
	 * class BlobPilot extends gmBlob {
	 * 
	 * };
	 * 
	 * class BlobMiner extends gmBlob {
	 * 
	 * };
	 * 
	 * class BlobThrower extends gmBlob {
	 * 
	 * };
	 */
}
