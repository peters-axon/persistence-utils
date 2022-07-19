package com.axonivy.utils.persistence.logging;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;

/**
 * Can be used to create tree like output in ASCII.
 *
 * @author peter
 *
 */
public class AsciiTree {

	private final Deque<List<Object>> stack;
	private List<Object> list;

	/**
	 *
	 */
	public AsciiTree() {
		list = new ArrayList<>();
		stack = new ArrayDeque<>();
	}

	/**
	 * Go down a level in the hierarchy.
	 */
	public void down () {
		stack.push(list);
		ArrayList<Object> newList = new ArrayList<>();
		list.add(newList);
		list = newList;
	}

	/**
	 * Go up a level in the hierarchy.
	 */
	public void up() {
		if (!stack.isEmpty()) {
			list = stack.pop();
		}
	}

	/**
	 * Add a {@link String} at the current level.
	 *
	 * See {@link String#format(String, Object...)}.
	 *
	 * @param format format string
	 * @param args arguments referenced by the format specifiers in the format string
	 */
	public void format(String format, Object... args) {
		list.add(String.format(format, args));
	}

	/**
	 * See {@link Object#toString()}.
	 *
	 */
	@Override
	public String toString() {
		StringWriter stringWriter = new StringWriter();
		PrintWriter out = new PrintWriter(stringWriter);
		toString (out, list, "");
		return stringWriter.toString();
	}

	@SuppressWarnings("unchecked")
	private void toString(PrintWriter out, List<Object> level, String prefix) {
		Iterator<Object> iterator = level.iterator();
		Object last = null;
		while (iterator.hasNext()) {
			Object object = iterator.next();
			if (!(object instanceof List)) {
				last = object;
			}
		}

		iterator = level.iterator();
		while (iterator.hasNext()) {
			Object object = iterator.next();
			if (object instanceof List) {
				toString (out, (List<Object>) object, prefix + (iterator.hasNext() ? "|   " : "    "));
			} else {
				String string = (String)object;
				out.print(prefix);
				out.print (object == last ? "\\" : "+");
				out.print("--- ");
				out.println(string);
			}
		}
	}
}
