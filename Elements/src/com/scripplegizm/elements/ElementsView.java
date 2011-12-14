package com.scripplegizm.elements;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;

import com.scripplegizm.elements.Blob.Banker;
import com.scripplegizm.elements.Blob.FactoryWorker;
import com.scripplegizm.elements.Blob.Farmer;
import com.scripplegizm.elements.Blob.Gatherer;
import com.scripplegizm.elements.Blob.Paver;
import com.scripplegizm.elements.Blob.ToxicCollector;
import com.scripplegizm.elements.Device.ToxinIncinerator;
import com.scripplegizm.elements.ElementsActivity.MenuAction;
import com.scripplegizm.elements.GameObject.WorldColor;
import com.scripplegizm.elements.PlayerData.ColorRole;
import com.scripplegizm.elements.Scenario.ObjectCountRule.CompareMode;
import com.scripplegizm.elements.Structure.Factory;
import com.scripplegizm.elements.Structure.Storehouse;
import com.scripplegizm.gameutils.ClickableRect;
import com.scripplegizm.gameutils.GameView;
import com.scripplegizm.gameutils.Point2D;

class ElementsView extends GameView {
	static class ActionButton extends ClickableRect {
		private int actionIndex;

		public ActionButton(int x, int y, int width, int height,
				ClickableMode mode, GameState st, int boundKeyCode,
				int actionIndex) {
			super(x, y, width, height, mode, st, boundKeyCode);
			this.yTextOffset = 25;
			this.actionIndex = actionIndex;
		}

		@Override
		public void click() {
			Log.i("_", "click " + World.curObject);
			if (World.curObject instanceof Banker) {
				World.curObject.doAction(actionIndex);
				updateCaption();
				return;
			}

			if (curPlayer.actions == 0) {
				ElementsView.showOutOfActionsMessage();
				return;
			}

			curPlayer.actions--;

			if (World.curObject != null) {
				World.curObject.doAction(actionIndex);
				updateCaption();
			}
		}

		public void updateCaption() {
			String caption = null;
			if (World.curObject != null) {
				caption = World.curObject.getActionCaption(actionIndex);
			}

			if (caption == null) {
				setCaption("");
				visible = false;
			} else {
				setCaption(caption);
				visible = true;
			}
		}
	}

	static World world = null;
	static PlayerData curPlayer = null;
	static WorldCell cell0 = null;
	static WorldCell cell1 = null;
	private static PlayerData player0 = null;
	private static PlayerData player1 = null;
	private static PlayerData player2 = null;
	static int curPlayerIdx = 0;
	static int turns = 0;
	static String displayMessage = "";
	static float displayMessageTimer = 0.0f;
	static final boolean runScenarios = true;

	static void showMessage(String newMessage) {
		displayMessageTimer = 5.0f;
		displayMessage = newMessage;
		Log.i("MESSAGE", displayMessage);
	}

	boolean nextTurn = false;
	int scenarioCount = 0;
	HashMap<MenuAction, Boolean> activatedActions = new HashMap<MenuAction, Boolean>();
	boolean deleteRequested = false;
	boolean newgameRequested = false;
	boolean justClicked = false;
	Point2D justClickedPos = new Point2D(0, 0);

	ClickableRect nextTurnBtn = new ClickableRect(300, 500, 100, 50,
			ClickableMode.SINGLE_CLICK, GameState.RUNNING,
			KeyEvent.KEYCODE_SPACE) {
		@Override
		public void click() {
			nextTurn = true;
		}
	};

	ArrayList<ClickableRect> ColorRoleButtons = new ArrayList<ClickableRect>();
	ArrayList<ClickableRect> PlayerButtons = new ArrayList<ClickableRect>();

	static final int LOWER_CONTROLS_X = 25;

	static ActionButton action1Btn = new ActionButton(LOWER_CONTROLS_X, 500,
			150, 40, ClickableMode.SINGLE_CLICK, GameState.RUNNING,
			KeyEvent.KEYCODE_1, 1);

	static ActionButton action2Btn = new ActionButton(178, 500, 150, 40,
			ClickableMode.SINGLE_CLICK, GameState.RUNNING, KeyEvent.KEYCODE_2,
			2);

	static ActionButton action3Btn = new ActionButton(331, 500, 150, 40,
			ClickableMode.SINGLE_CLICK, GameState.RUNNING, KeyEvent.KEYCODE_2,
			3);

	static ColorRole curRole = ColorRole.FOOD;

	private static void setActionsButtonsY(int y) {
		action1Btn.y = y;
		action2Btn.y = y;
		action3Btn.y = y;
	}

	public static void showOutOfActionsMessage() {
		showMessage("Out of actions!");
	}

	static void UpdateCurObject() {
		action1Btn.updateCaption();
		action2Btn.updateCaption();
		action3Btn.updateCaption();
	}

	Scenario scenario = null;

	ArrayList<Scenario> scenarios = new ArrayList<Scenario>();

	public ElementsView(Context context) {
		super(context);
	}

	public void createScenarios() {
		scenarioCount = 0;
		scenario = null;
		scenarios.clear();
		Scenario s = new Scenario("Add a Farmer");
		s.setAllowed(MenuAction.ADD_FARMER);
		s.rules.add(new Scenario.TurnLimitRule(5));
		s.rules.add(new Scenario.ObjectCountRule(0,
				CompareMode.DONE_IF_GREATER, Farmer.class));
		s.addElement(cell0, player0.getColor(ColorRole.LIVING), 15);
		s.addElement(cell0, player0.getColor(ColorRole.TOXIC), 28);
		s.addElement(cell0, player0.getColor(ColorRole.BRICKS), Storehouse.COST);
		s.addElement(cell0, player0.getColor(ColorRole.ROAD), Road.cost);
		s.addElement(cell1, player0.getColor(ColorRole.BRICKS), Storehouse.COST);
		s.addElement(cell1, player0.getColor(ColorRole.MONEY), ToxinIncinerator.COST);
		s.addElement(cell1, WorldColor.SILVER, 32);
		s.addElement(cell1, WorldColor.CHARCOAL, 32);
		s.addObject(cell1, new Storehouse(player0.getColor(ColorRole.FOOD),
				player0));
		s.addObject(cell1, new Factory(WorldColor.SILVER,
				WorldColor.CHARCOAL, player0.getStructureColor(), player0));
		s.addCollectedElement(cell1, player0.getColor(ColorRole.FOOD), 15);
		scenarios.add(s);

		s = new Scenario("Build a food storehouse, get 5 food");
		s.setAllowed(MenuAction.ADD_FARMER, MenuAction.ADD_BUILDER);
		s.rules.add(new Scenario.TurnLimitRule(10));
		s.rules.add(new Scenario.ObjectCountRule(0,
				CompareMode.DONE_IF_GREATER, Storehouse.class, cell0));
		s.rules.add(new Scenario.AvailableCountRule(4,
				CompareMode.DONE_IF_GREATER, player0.getFoodColor(), cell0));
		scenarios.add(s);

		s = new Scenario("Add a paver and make a road.");
		s.setAllowed(MenuAction.ADD_FARMER, MenuAction.ADD_BUILDER,
				MenuAction.ADD_PAVER);
		s.rules.add(new Scenario.TurnLimitRule(10));
		s.rules.add(new Scenario.ObjectCountRule(0,
				CompareMode.DONE_IF_GREATER, Road.class));
		s.rules.add(new Scenario.ObjectCountRule(0,
				CompareMode.DONE_IF_GREATER, Paver.class));
		scenarios.add(s);

		s = new Scenario("Add BRICK gatherer, send to other cell.");
		s.setAllowed(MenuAction.ADD_FARMER, MenuAction.ADD_BUILDER,
				MenuAction.ADD_PAVER, MenuAction.ADD_GATHERER);
		s.rules.add(new Scenario.TurnLimitRule(10));
		s.rules.add(new Scenario.AvailableCountRule(0,
				CompareMode.DONE_IF_GREATER, player0.getStructureColor()));
		s.rules.add(new Scenario.ObjectCountRule(0,
				CompareMode.DONE_IF_GREATER, Gatherer.class));
		scenarios.add(s);

		s = new Scenario("Add BRICK carrier. Build toxic storehouse.");
		s.setAllowed(MenuAction.ADD_FARMER, MenuAction.ADD_BUILDER,
				MenuAction.ADD_PAVER, MenuAction.ADD_GATHERER,
				MenuAction.ADD_CARRIER);
		s.rules.add(new Scenario.TurnLimitRule(15));
		s.rules.add(new Scenario.ResourceCountRule(1, CompareMode.DONE_IF_LESS,
				player0.getStructureColor(), cell1));
		s.rules.add(new Scenario.AvailableCountRule(1,
				CompareMode.DONE_IF_LESS, player0.getStructureColor(), cell1));
		s.rules.add(new Scenario.ObjectCountRule(1,
				CompareMode.DONE_IF_GREATER, Storehouse.class, cell0));
		s.rules.add(new Scenario.ObjectCountRule(0,
				CompareMode.DONE_IF_GREATER, Gatherer.class));
		scenarios.add(s);

		s = new Scenario("Add TOXIC Collector. Collect toxin.");
		s.setAllowed(MenuAction.ADD_FARMER, MenuAction.ADD_BUILDER,
				MenuAction.ADD_PAVER, MenuAction.ADD_GATHERER,
				MenuAction.ADD_CARRIER, MenuAction.ADD_TOXIN_COLLECTOR);
		s.rules.add(new Scenario.TurnLimitRule(15));
		s.rules.add(new Scenario.ResourceCountRule(10,
				CompareMode.DONE_IF_LESS, player0.getToxicColor(), cell0));
		s.rules.add(new Scenario.ObjectCountRule(0,
				CompareMode.DONE_IF_GREATER, ToxicCollector.class, cell0));
		scenarios.add(s);

		s = new Scenario("Add Worker. Send to other cell.");
		s.setAllowed(MenuAction.ADD_FARMER, MenuAction.ADD_BUILDER,
				MenuAction.ADD_PAVER, MenuAction.ADD_GATHERER,
				MenuAction.ADD_CARRIER, MenuAction.ADD_TOXIN_COLLECTOR,
				MenuAction.ADD_WORKER);
		s.rules.add(new Scenario.TurnLimitRule(7));
		s.rules.add(new Scenario.ObjectCountRule(0,
				CompareMode.DONE_IF_GREATER, FactoryWorker.class, cell1));
		scenarios.add(s);

		s = new Scenario("Work in factory, make bricks.");
		s.setAllowed(MenuAction.ADD_FARMER, MenuAction.ADD_BUILDER,
				MenuAction.ADD_PAVER, MenuAction.ADD_GATHERER,
				MenuAction.ADD_CARRIER, MenuAction.ADD_TOXIN_COLLECTOR,
				MenuAction.ADD_WORKER);
		s.rules.add(new Scenario.TurnLimitRule(30));
		s.rules.add(new Scenario.AvailableCountRule(20,
				CompareMode.DONE_IF_GREATER, player0.getStructureColor(), cell1));
		scenarios.add(s);
		
		if (runScenarios) {
			cell0.clearResources();
			cell1.clearResources();
			for (ClickableRect r : PlayerButtons) {
				r.visible = false;
			}
		} else {
			scenarios.clear();
		}
	}

	public void doNextTurn() {
		// testGame();
		// testStarveAll();
		// testNoFarmer();
		world.Update();
		if (scenario != null) {
			scenario.update(world);
			if (scenario.done) {
				ElementsView.showMessage("'" + scenario.scenarioTitle
						+ (scenario.success ? "' completed!" : "' failed."));
				if (!scenario.success) {
					scenarios.clear();
				}
				scenario = null;
				if (scenarios.size() == 0) {
					startFreePlay();
					ElementsView.showMessage("Scenarios Complete.  Free Play!");
				}
			}
		}
	}

	public void startFreePlay() {
		// Just to be nice.
		for (int i = 0; i < WorldColor.values().length; i++) {
			cell0.colorResource[i] = 32;
		}
		for (ClickableRect r : PlayerButtons) {
			r.visible = true;
		}
	}

	@Override
	public void drawGame(Canvas canvas) {
		WorldCell.isGameThread.set(true);
		this.blank(canvas, Color.BLACK);
		this.drawText(canvas, "Actions: " + curPlayer.actions, LOWER_CONTROLS_X,
				yMax - 126);
		this.drawText(canvas, "Turns: " + turns, LOWER_CONTROLS_X, yMax - 110);
		world.draw(canvas);
		if (scenario != null) {
			this.drawText(canvas, "Scenario " + scenarioCount + ": "
					+ scenario.scenarioTitle, LOWER_CONTROLS_X, yMax - 85);
		}

		if (displayMessageTimer > 0.0f) {
			displayMessageTimer -= this.delta;
			draw.getPaint().setColor(Color.WHITE);
			canvas.drawText(displayMessage, 30, 300, draw.getPaint());
		}
	}

	private void initButtons() {
		this.buttonManager.addButton(nextTurnBtn);
		this.buttonManager.addButton(action1Btn);
		this.buttonManager.addButton(action2Btn);
		this.buttonManager.addButton(action3Btn);
		nextTurnBtn.setCaption("TURN");
		nextTurnBtn.visible = true;

		int btnHeight = 25;
		for (int i = 0; i < 3; i++) {
			final int j = i;
			ClickableRect playerBtn = new ClickableRect(10,
					i * btnHeight + 300, 100, btnHeight,
					ClickableMode.SINGLE_CLICK, GameState.RUNNING) {

				@Override
				public void click() {
					for (ClickableRect r : PlayerButtons) {
						if (r.caption.getBytes()[0] == '*') {
							r.caption = r.caption.substring(2);
						}
					}
					caption = "* " + caption;
					curPlayerIdx = j;
					curPlayer = world.players.get(j);
				}
			};
			playerBtn.yTextOffset = 20;
			playerBtn.setCaption((curPlayerIdx == j ? "* " : "") + " Player "
					+ j);
			playerBtn.visible = true;
			buttonManager.addButton(playerBtn);
			PlayerButtons.add(playerBtn);
		}

		int i = 0;
		for (ColorRole c : ColorRole.values()) {
			final ColorRole role = c;
			ClickableRect r = new ClickableRect(10, i * btnHeight + 25, 100,
					btnHeight, ClickableMode.SINGLE_CLICK, GameState.RUNNING) {

				ColorRole myColor = role;

				@Override
				public void click() {
					for (ClickableRect r : ColorRoleButtons) {
						if (r.caption.getBytes()[0] == '*') {
							r.caption = r.caption.substring(2);
						}
					}
					caption = "* " + caption;
					curRole = myColor;
				}

				@Override
				public void draw(Canvas canvas) {
					color = curPlayer.getColor(myColor).colorValue;
					super.draw(canvas);
				}
			};
			r.yTextOffset = 20;
			r.setCaption((c == curRole ? "* " : "") + c.toString());
			r.visible = true;
			buttonManager.addButton(r);
			ColorRoleButtons.add(r);
			i++;
		}
	}

	@Override
	public void initGame() {
		for (MenuAction menuAction : MenuAction.values()) {
			activatedActions.put(menuAction, false);
		}

		initButtons();

		setGameState(GameState.RUNNING);
		newGame();

		/*
		 * // HACK! WorldCell.isGameThread.set(true); cell0.AddObject(new
		 * Device(player0.getColor(ColorRole.GOODS), player0));
		 * WorldCell.isGameThread.set(false);
		 */
	}

	@Override
	public void newGame() {
		World.setCurObject(null);

		world = new World();
		player0 = new PlayerData();
		player1 = new PlayerData();
		player2 = new PlayerData();
		cell0 = new WorldCell();
		cell1 = new WorldCell();

		curPlayer = player0;
		curPlayerIdx = 0;

		player0.id = 0;
		player0.setBlobLivingColor(WorldColor.IVORY);
		player0.setBlobFoodColor(WorldColor.HOT_PINK);
		player0.setStructureColor(WorldColor.AQUAMARINE);
		player0.setToxicColor(WorldColor.LIME);
		player0.setRoadColor(WorldColor.AZURE);
		player0.setTechColor(WorldColor.CHARCOAL); // devices, vehicles
													// and throwers
		player0.setFuelColor(WorldColor.CRIMSON); // 2
		player0.setMoneyColor(WorldColor.FOREST_GREEN); // 3

		player1.id = 2;
		player1.setBlobLivingColor(WorldColor.CYAN);
		player1.setBlobFoodColor(WorldColor.LIME);
		player1.setStructureColor(WorldColor.HOT_PINK);
		player1.setToxicColor(WorldColor.AQUAMARINE);
		player1.setRoadColor(WorldColor.CHARCOAL);
		player1.setTechColor(WorldColor.AZURE);
		player1.setFuelColor(WorldColor.CRIMSON);
		player1.setMoneyColor(WorldColor.FOREST_GREEN);

		player2.id = 3;
		player2.setBlobLivingColor(WorldColor.LIME);
		player2.setBlobFoodColor(WorldColor.IVORY);
		player2.setStructureColor(WorldColor.AQUAMARINE);
		player2.setToxicColor(WorldColor.HOT_PINK);
		player2.setRoadColor(WorldColor.FOREST_GREEN);
		player2.setTechColor(WorldColor.CHARCOAL);
		player2.setFuelColor(WorldColor.CRIMSON);
		player2.setMoneyColor(WorldColor.AZURE);

		world.AddCell(cell0);
		world.AddCell(cell1);
		world.AddPlayer(player0);
		world.AddPlayer(player1);
		world.AddPlayer(player2);

		createScenarios();
	}

	public boolean onOptionsItemSelected(int itemId) {
		if (itemId == R.id.delete) {
			deleteRequested = true;
			return true;
		} else if (itemId == R.id.newgame) {
			newgameRequested = true;
			return true;
		}

		MenuAction menuAction = MenuAction.fromId(itemId);
		if (menuAction != null) {
			activatedActions.put(menuAction, true);
			return true;
		}
		return false;
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (super.onTouchEvent(event)) {
			return true;
		}
		justClickedPos.set(event.getX(), event.getY());
		justClicked = true;
		return false;
	}

	protected void processClick() {
		if (justClicked) {
			world.click(justClickedPos.getX(), justClickedPos.getY());
			justClicked = false;
		}
	}

	protected void processMenuRequests() {
		if (newgameRequested) {
			newgameRequested = false;
			newGame();
		}
		if (deleteRequested) {
			deleteRequested = false;
			if (World.curObject != null) {
				Log.i("_", "#### Mark for delete: " + World.curObject);
				World.curObject.readyForDeletion = true;
				World.curObject = null;
				if (!nextTurn) {
					world.deleteDeadObjects();
				}
			}
			return;
		}

		if (curPlayer.actions == 0) {
			for (Entry<MenuAction, Boolean> entry : activatedActions.entrySet()) {
				if (entry.getValue()) {
					entry.setValue(false);
					showOutOfActionsMessage();
					break;
				}
			}
		} else {
			boolean didAction = false;
			for (Entry<MenuAction, Boolean> entry : activatedActions.entrySet()) {
				if (entry.getValue()) {
					didAction = true;
					MenuAction menuAction = entry.getKey();
					switch (menuAction) {
					case ADD_FARMER:
						curPlayer.AddFarmer(cell0);
						break;
					case ADD_EATER:
						curPlayer.AddEater(cell0);
						break;
					case ADD_PAVER:
						curPlayer.AddPaver(cell0);
						break;
					case ADD_BUILDER:
						curPlayer.AddBuilder(cell0);
						break;
					case ADD_WORKER:
						curPlayer.AddFactoryWorker(cell0, null);
						break;
					case ADD_TOXIN_COLLECTOR:
						curPlayer.AddToxicCollector(cell0);
						break;
					case ADD_GATHERER:
						curPlayer.AddGatherer(cell0,
								curPlayer.getColor(curRole));
						break;
					case ADD_CARRIER:
						curPlayer
								.AddCarrier(cell0, curPlayer.getColor(curRole));
						break;
					case ADD_BANKER:
						curPlayer.AddBanker(cell0);
						break;
					}
					entry.setValue(false);
				}
			}
			if (didAction) {
				curPlayer.actions--;
			}
		}
	}

	@Override
	public void reinitGame() {
		nextTurnBtn.y = 400;
		nextTurnBtn.x = xMax - WorldCell.ELEMENT_X_MARGIN + 15;
		for (ClickableRect r : ColorRoleButtons) {
			r.x = xMax - WorldCell.ELEMENT_X_MARGIN + 15;
		}
		for (ClickableRect r : PlayerButtons) {
			r.x = xMax - WorldCell.ELEMENT_X_MARGIN + 15;
		}
		ElementsView.setActionsButtonsY(yMax - 80);
	}

	@Override
	public void updateGame() {
		WorldCell.isGameThread.set(true);
		this.updateDelta();
		processMenuRequests();
		processClick();

		if (scenario == null && scenarios.size() > 0) {
			scenario = scenarios.remove(0);
			scenarioCount++;
			scenario.start();
		}

		if (nextTurn) {
			doNextTurn();
			nextTurn = false;
		}
	}
}
