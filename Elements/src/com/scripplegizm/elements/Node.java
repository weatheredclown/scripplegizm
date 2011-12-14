package com.scripplegizm.elements;

import com.scripplegizm.elements.ElementsUtils.Func0;
import com.scripplegizm.elements.ElementsUtils.Func1;
import com.scripplegizm.elements.ElementsUtils.Func2;
import com.scripplegizm.elements.ElementsUtils.Func3;

class Node<T> {
	public Node(T data) {
		this.data = data;
		next = null;
	}

	T GetData() {
		return data;
	}

	T data;
	Node<T> next;

	<X extends Func0<T>> void ForEach(X x) {
		Node<T> pCur = this;
		while (pCur != null) {
			x.call(pCur.data);
			pCur = pCur.next;
		}
	}

	<X extends Func1<T, Y>, Y> void ForEach(X x) {
		Node<T> pCur = this;
		while (pCur != null) {
			x.call(pCur.data, x.y);
			pCur = pCur.next;
		}
	}

	<X extends Func2<T, Y, Z>, Y, Z> void ForEach(X x) {
		Node<T> pCur = this;
		while (pCur != null) {
			x.call(pCur.data, x.y, x.z);
			pCur = pCur.next;
		}
	}

	<X extends Func3<T, Y, Z, A>, Y, Z, A> void ForEach(X x) {
		Node<T> pCur = this;
		while (pCur != null) {
			x.call(pCur.data, x.y, x.z, x.a);
			pCur = pCur.next;
		}
	}
};
