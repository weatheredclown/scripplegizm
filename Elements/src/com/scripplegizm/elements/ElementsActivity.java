package com.scripplegizm.elements;

import android.view.Menu;
import android.view.MenuInflater;
import android.view.SubMenu;

import com.scripplegizm.gameutils.GameActivity;
import com.scripplegizm.gameutils.GameView;

public class ElementsActivity extends GameActivity {
		
	ElementsView eView;
	
	@Override
	public GameView createGameView() {
		eView = new ElementsView(this);
		return eView;
	}

	static final int MAGIC_MENU_BASE = 0xDEADBEEF;

	enum MenuAction {
		ADD_FARMER(0, "Add Farmer"),
		ADD_BUILDER(1, "Add Builder"),
		ADD_PAVER(2, "Add Paver"),
		ADD_WORKER(3, "Add Worker"),
		ADD_TOXIN_COLLECTOR(4, "Add Toxin Collector"), 
		ADD_GATHERER(5, "Add Gatherer"),
		ADD_CARRIER(6, "Add Carrier"),
		ADD_EATER(7, "Add Eater"),
		ADD_BANKER(8, "Add Banker");

		final int index;
		private String name;

		MenuAction(int i, String title) {
			this.index = MAGIC_MENU_BASE + i;
			this.name = title;
		}

		public static MenuAction fromId(int itemId) {
			for (MenuAction action : values()) {
				if (action.index == itemId) {
					return action;
				}
			}
			return null;
		}
	}

	@Override
	public boolean onPrepareOptionsMenu (Menu menu)	{
		if (eView.scenario != null && eView.scenario.allowed.size() != 0) {
			SubMenu items = menu.getItem(0).getSubMenu();
			for (int i = 0; i < items.size(); i++) {
				items.getItem(i).setVisible(false);
			}
			for (MenuAction action : eView.scenario.allowed) {
				items.findItem(action.index).setVisible(true);
			}
		} else {
			SubMenu items = menu.getItem(0).getSubMenu();
			for (int i = 0; i < items.size(); i++) {
				items.getItem(i).setVisible(true);
			}
		}
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main, menu);
		SubMenu submenu = menu.addSubMenu(0, 0, 0, "Add");
		for (MenuAction action : MenuAction.values()) {
			submenu.add(0, action.index, 0, action.name);
		}
		return true;
	}
}
