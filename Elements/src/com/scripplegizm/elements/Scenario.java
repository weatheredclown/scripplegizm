package com.scripplegizm.elements;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import android.util.Log;
import android.util.Pair;

import com.scripplegizm.elements.ElementsActivity.MenuAction;
import com.scripplegizm.elements.GameObject.WorldColor;
import com.scripplegizm.elements.Scenario.ObjectCountRule.CompareMode;
import com.scripplegizm.elements.Scenario.Rule.Processor.Result;

public class Scenario {
	static public class ResourceCountRule implements Rule {
		class ResourceCountProcessor implements Processor {
			public Result getResult() {
				int count = cell == null ? ElementsView.world
						.GetColorResourceAvailable(color) : cell
						.getColorResourceAvailable(color);
				Log.i("RESULT", "Count " + color + ": " + count + "/"
						+ goalCount);
				return mode.getResult(count, goalCount);
			}

			public void accept(WorldCell cell, GameObject obj) {
			}
		}

		private int goalCount;
		private CompareMode mode;
		private WorldColor color;
		private WorldCell cell;

		public ResourceCountRule(int i, CompareMode mode, WorldColor color) {
			this(i, mode, color, null);
		}

		public ResourceCountRule(int i, CompareMode mode, WorldColor color,
				WorldCell cell) {
			this.goalCount = i;
			this.mode = mode;
			this.color = color;
			this.cell = cell;
		}

		public Processor genProcessor() {
			return new ResourceCountProcessor();
		}
	}
	
	static public class AvailableCountRule implements Rule {
		class AvailableCountProcessor implements Processor {
			public Result getResult() {
				int count = cell == null ? ElementsView.world
						.GetColorAvailable(color) : cell
						.getColorAvailable(color);
				Log.i("RESULT", "Count " + color + ": " + count + "/"
						+ goalCount);
				return mode.getResult(count, goalCount);
			}

			public void accept(WorldCell cell, GameObject obj) {
			}
		}

		private int goalCount;
		private CompareMode mode;
		private WorldColor color;
		private WorldCell cell;

		public AvailableCountRule(int i, CompareMode mode, WorldColor color) {
			this(i, mode, color, null);
		}

		public AvailableCountRule(int i, CompareMode mode, WorldColor color,
				WorldCell cell) {
			this.goalCount = i;
			this.mode = mode;
			this.color = color;
			this.cell = cell;
		}

		public Processor genProcessor() {
			return new AvailableCountProcessor();
		}

	}

	interface Rule {
		Processor genProcessor();

		interface Processor {
			enum Result {
				NO_RESULT, NOT_DONE, DONE, LOSE, FINAL_DONE;
			}

			Result getResult();

			void accept(WorldCell cell, GameObject obj);
		}
	}

	static class ObjectCountRule implements Rule {

		class ObjectCountProcessor implements Processor {

			int count = 0;

			public Result getResult() {
				return mode.getResult(count, goalCount);
			}

			public void accept(WorldCell cell, GameObject obj) {
				if (ObjectCountRule.this.cell == null || 
						ObjectCountRule.this.cell == cell) {
					if (objectType.isInstance(obj)) {
						count++;
					}
				}
			}
		}

		enum CompareMode {
			LOSE_IF_LESS, LOSE_IF_GREATER, DONE_IF_LESS, DONE_IF_GREATER;

			public Result getResult(int count, int goalCount) {
				switch (this) {
				case LOSE_IF_LESS:
					return count < goalCount ? Result.LOSE : Result.NO_RESULT;
				case LOSE_IF_GREATER:
					return count > goalCount ? Result.LOSE : Result.NO_RESULT;
				case DONE_IF_LESS:
					return count < goalCount ? Result.DONE : Result.NOT_DONE;
				case DONE_IF_GREATER:
					return count > goalCount ? Result.DONE : Result.NOT_DONE;
				}
				return null;
			}
		}

		final int goalCount;
		final CompareMode mode;
		final Class<? extends GameObject> objectType;
		final WorldCell cell;

		ObjectCountRule(int count, CompareMode mode,
				Class<? extends GameObject> objType) {
			this(count, mode, objType, null);
		}

		ObjectCountRule(int count, CompareMode mode,
				Class<? extends GameObject> objType,
				WorldCell cell) {
			this.cell = cell;
			this.goalCount = count;
			this.mode = mode;
			this.objectType = objType;
		}

		public Processor genProcessor() {
			return new ObjectCountProcessor();
		}
	}

	static class TurnLimitRule implements Rule {
		class TurnLimitProcessor implements Processor {

			public Result getResult() {
				if (numTurns <= ElementsView.turns)
					return Result.FINAL_DONE;
				return Result.NO_RESULT;
			}

			public void accept(WorldCell cell, GameObject obj) {
			}
		}

		int numTurns;

		TurnLimitRule(int numTurns) {
			this.numTurns = numTurns;
		}

		public Processor genProcessor() {
			return new TurnLimitProcessor();
		}
	}

	static final int NO_TURN_LIMIT = -1;

	boolean done = false;
	boolean success = false;
	ArrayList<Rule> rules = new ArrayList<Rule>();
	ArrayList<MenuAction> allowed = new ArrayList<MenuAction>();
	String scenarioTitle = "";
	HashMap<Pair<WorldCell, WorldColor>, Integer> elements = new HashMap<Pair<WorldCell, WorldColor>, Integer>();
	HashMap<Pair<WorldCell, WorldColor>, Integer> collectedElements = new HashMap<Pair<WorldCell, WorldColor>, Integer>();

	public void addElement(WorldCell cell, WorldColor c, int amt) {
		elements.put(new Pair<WorldCell, WorldColor>(cell, c), amt);
	}

	public Scenario(String string) {
		scenarioTitle = string;
	}

	void update(World world) {
		final ArrayList<Rule.Processor> processors = new ArrayList<Rule.Processor>();
		for (Rule rule : rules) {
			processors.add(rule.genProcessor());
		}

		world.process(processors);

		boolean isSuccess = true;
		boolean maybeDone = false;
		boolean notDone = false;
		for (Rule.Processor processor : processors) {
			Rule.Processor.Result result = processor.getResult();
			Log.i("RESULT", "Process: " + processor + " result " + result);
			switch (result) {
			case FINAL_DONE:
				done = true;
				break;
			case DONE:
				maybeDone = true;
				break;
			case NOT_DONE:
				notDone = true;
				if (done) {
					isSuccess = false;
				}
				break;
			case LOSE:
				done = true;
				isSuccess = false;
				break;
			}
		}
		if (maybeDone && !notDone) {
			done = true;
		}
		if (done) {
			success = isSuccess;
		}
		Log.i("RESULT", "Done: " + done + ", Success: " + success);
	}

	boolean isDone() {
		return done;
	}

	boolean isSuccess() {
		return success;
	}

	public void setAllowed(MenuAction... a) {
		ArrayList<MenuAction> list = new ArrayList<MenuAction>();
		for (MenuAction action : a) {
			list.add(action);
		}
		setAllowed(list);
	}

	private void setAllowed(ArrayList<MenuAction> list) {
		this.allowed.addAll(list);
	}

	public void start() {
		for (Pair<WorldCell, GameObject> pair : addedObjects) {
			Log.i("_", "add " + pair.second + " in " + pair.first);
			pair.first.addObject(pair.second);
		}

		for (Entry<Pair<WorldCell, WorldColor>, Integer> entry : elements
				.entrySet()) {
			Pair<WorldCell, WorldColor> pair = entry.getKey();
			Integer amt = entry.getValue();
			Log.i("_", "plant " + amt + " " + pair.second + " in " + pair.first);
			pair.first.plant(pair.second, amt);
		}

		for (Entry<Pair<WorldCell, WorldColor>, Integer> entry : collectedElements
				.entrySet()) {
			Pair<WorldCell, WorldColor> pair = entry.getKey();
			Integer amt = entry.getValue();
			Log.i("_", "produce " + amt + " " + pair.second + " in " + pair.first);
			pair.first.produce(pair.second, amt);
		}

		for (Rule rule : rules) {
			if (rule instanceof TurnLimitRule) {
				((TurnLimitRule) rule).numTurns += ElementsView.turns;
			}
		}
	}

	// test the scenarios
	// test add the food on start
	public void addCollectedElement(WorldCell cell, WorldColor c, int amt) {
		collectedElements.put(new Pair<WorldCell, WorldColor>(cell, c), amt);
	}

	ArrayList<Pair<WorldCell, GameObject>> addedObjects = new ArrayList<Pair<WorldCell, GameObject>>();

	public void addObject(WorldCell cell, GameObject object) {
		addedObjects.add(new Pair<WorldCell, GameObject>(cell, object));
	}
}
