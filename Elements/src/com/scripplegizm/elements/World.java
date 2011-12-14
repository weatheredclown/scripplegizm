package com.scripplegizm.elements;

import java.util.ArrayList;
import java.util.List;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint.Style;
import android.util.Log;

import com.scripplegizm.elements.ElementsUtils.Func0;
import com.scripplegizm.elements.ElementsUtils.Func1;
import com.scripplegizm.elements.ElementsUtils.Func3;
import com.scripplegizm.elements.ElementsUtils.Value;
import com.scripplegizm.elements.ElementsUtils.gmWorldCellNode;
import com.scripplegizm.elements.GameObject.WorldColor;
import com.scripplegizm.elements.PlayerData.ColorRole;
import com.scripplegizm.elements.Scenario.Rule.Processor;
import com.scripplegizm.elements.Structure.Factory;
import com.scripplegizm.gameutils.GameView;
import com.scripplegizm.gameutils.Point2D;

class World {

	public String toString() {
		return this.getClass().getSimpleName() + ":" + (this.hashCode() & 0xFF);
	}

	gmWorldCellNode head;
	List<PlayerData> players = new ArrayList<PlayerData>();

	World() {
		Log.i("_", "Create: " + this);
		ElementsView.turns = 0;
		head = null;
	}

	void WriteHTML(String filename) {
		Log.d(filename, "<html><body><table border=1><tr>");
		// m_Head.ForEach(&gmWorldCell::WriteHTML,p);
		if (head != null) {
			head.ForEach(new Func1<WorldCell, String>(filename) {
				@Override
				void call(WorldCell t, String y) {
					t.WriteHTML(y);
				}
			});
		}
		Log.d(filename, "</tr></table></body></html>");
	}

	public void AddPlayer(PlayerData player) {
		players.add(player);
	}

	void AddCell(WorldCell cell) {
		gmWorldCellNode node = new gmWorldCellNode(cell);
		if (head == null) {
			head = node;
			return;
		}
		gmWorldCellNode cur = head;
		while (cur.next != null) {
			cur = cur.getNext();
		}
		cur.next = node;
	}

	void Update() {
		for (PlayerData player : players) {
			player.actions = PlayerData.STARTING_ACTIONS;
		}
		Log.i("W", "\n== Turn " + (ElementsView.turns++) + "==\n");
		if (head != null) {
			head.ForEach(new Func0<WorldCell>() {
				@Override
				void call(WorldCell t) {
					t.update();
				}
			});
		}
		Log.i("W", "\n== Turn complete ==\n");
	}

	static Point2D curColorPos = new Point2D(0, 0);
	static WorldColor curColor = WorldColor.NONE;

	static void setClickedColor(Point2D colorPos, WorldColor color) {
		if (curColor == color) {
			return;
		}
		Log.i("_", "Set Clicked Color: " + color);
		curColorPos = colorPos;
		curColor = color;

		if (color != WorldColor.NONE && curObject instanceof Factory) {
			Factory factory = (Factory) curObject;
			if (factory.input1 == WorldColor.NONE) {
				factory.input1 = color;
			} else if (factory.input2 == WorldColor.NONE) {
				factory.input2 = color;
			}
		}
	}

	public void draw(final Canvas canvas) {
		Value<Integer> y = new Value<Integer>(0);
		if (head != null) {
			head.ForEach(new Func1<WorldCell, Value<Integer>>(y) {
				@Override
				void call(WorldCell t, Value<Integer> y) {
					t.draw(canvas, y.value);
					y.value += 340;
				}
			});
		}

		if (curObject != null) {
			GameView.draw.getPaint().setStyle(Style.STROKE);
			GameView.draw.getPaint().setColor(
					Color.rgb(255, GameView.rand.nextInt(255), 0));
			canvas.drawRect(curObject.x - 10, curObject.y - 10,
					curObject.x + 10, curObject.y + 10,
					GameView.draw.getPaint());
			canvas.drawText(curObject.getName(), 10, GameView.yMax - 10,
					GameView.draw.getPaint());
		}

		if (curColor != WorldColor.NONE) {
			GameView.draw.getPaint().setStyle(Style.STROKE);
			GameView.draw.getPaint().setColor(
					Color.rgb(255, GameView.rand.nextInt(255), 0));
			canvas.drawRect(curColorPos.getX() - 5, curColorPos.getY() - 20,
					curColorPos.getX() + 25, curColorPos.getY() + 5,
					GameView.draw.getPaint());
			GameView.draw.getPaint().setColor(curColor.colorValue);
			ColorRole colorRole = ElementsView.curPlayer.getColorRole(curColor);
			String color = curColor.toString()
					+ (colorRole != null ? " (" + colorRole + ")" : "");
			canvas.drawText(color, GameView.xMax - 200, GameView.yMax - 10,
					GameView.draw.getPaint());
		}

	}

	static GameObject curObject = null;

	public void deleteDeadObjects() {
		if (head != null) {
			head.ForEach(new Func0<WorldCell>() {
				@Override
				void call(WorldCell t) {
					t.deleteDeadObjects();
				}
			});
		}
	}

	public void click(float x, float y) {

		Value<Float> dist2 = new Value<Float>(2500.0f);
		Point2D pos = new Point2D(x, y);
		Value<GameObject> obj = new Value<GameObject>(null);
		// Log.i("CLICK", "WORLD");
		if (head != null) {
			head.ForEach(new Func3<WorldCell, Value<Float>, Point2D, Value<GameObject>>(
					dist2, pos, obj) {
				@Override
				void call(WorldCell t, Value<Float> y, Point2D z,
						Value<GameObject> obj) {
					t.click(y, z, obj);
				}
			});
		}

		if (curColor == WorldColor.NONE) {
			setCurObject(obj.value);
		}
	}

	public int GetColorAvailable(final WorldColor color) {
		Value<Integer> count = new Value<Integer>(0);
		if (head != null) {
			head.ForEach(new Func1<WorldCell, Value<Integer>>(count) {

				@Override
				void call(WorldCell t, Value<Integer> count) {
					count.value += t.getColorAvailable(color);
				}
			});
		}
		return count.value;
	}

	public static void setCurObject(GameObject obj) {
		if (obj == curObject) {
			return;
		}
		Log.i("_", "setCurObject: " + obj);
		if (curObject != null) {
			curObject.deselect();
		}
		if (obj != null && obj.onClick(curObject)) {
			curObject = obj;
		} else {
			curObject = null;
		}
		ElementsView.UpdateCurObject();
	}

	public void process(final ArrayList<Processor> processors) {
		if (head != null) {
			head.ForEach(new Func0<WorldCell>() {

				@Override
				void call(WorldCell t) {
					t.process(processors);
				}
			});
		}
	}

	public int GetColorResourceAvailable(final WorldColor color) {
		Value<Integer> count = new Value<Integer>(0);
		if (head != null) {
			head.ForEach(new Func1<WorldCell, Value<Integer>>(count) {

				@Override
				void call(WorldCell t, Value<Integer> count) {
					count.value += t.getColorResourceAvailable(color);
				}
			});
		}
		return count.value;
	}
}
