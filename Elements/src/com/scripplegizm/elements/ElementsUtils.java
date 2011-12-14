package com.scripplegizm.elements;

public class ElementsUtils {
	static abstract class Func0<T> {
		abstract void call(T t);
	}

	static abstract class Func1<T, Y> {
		Y y;

		Func1(Y y1) {
			y = y1;
		}

		abstract void call(T t, Y y);
	}

	static abstract class Func2<T, Y, Z> {
		Y y;
		Z z;

		Func2(Y y1, Z z1) {
			y = y1;
			z = z1;
		}

		abstract void call(T t, Y y, Z z);
	}

	static abstract class Func3<T, Y, Z, A> {
		Y y;
		Z z;
		A a;

		Func3(Y y1, Z z1, A a1) {
			y = y1;
			z = z1;
			a = a1;
		}

		abstract void call(T t, Y y, Z z, A a);
	}

	// use this like a pointer?
	static class Value<T> {
		T value;

		public Value(T f) {
			value = f;
		}

		boolean equals(Value<?> that) {
			return value.equals(that.value);
		}
	}

	/*
	 * static class gmWorldObjNode extends Node<GameObject> { public
	 * gmWorldObjNode(GameObject data) { super(data); }
	 * 
	 * public gmWorldObjNode getNext() { return (gmWorldObjNode) next; } }
	 * 
	 * static class gmCellRoadNode extends Node<Road> { gmCellRoadNode(Road
	 * data) { super(data); }
	 * 
	 * public gmCellRoadNode getNext() { return (gmCellRoadNode) next; } }
	 */
	static class gmWorldCellNode extends Node<WorldCell> {
		public gmWorldCellNode(WorldCell data) {
			super(data);
		}

		public gmWorldCellNode getNext() {
			return (gmWorldCellNode) next;
		}
	}
}
