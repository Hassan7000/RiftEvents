package org.hassan.riftevents.rifteventsrecoded.utils;

public enum ImageChar {
	BLOCK("BLOCK", 0, '\u2588'),
	DARK_SHADE("DARK_SHADE", 1, '\u2593'),
	MEDIUM_SHADE("MEDIUM_SHADE", 2, '\u2592'),
	LIGHT_SHADE("LIGHT_SHADE", 3, '\u2591');

	private char c;

	private ImageChar(final String name, final int ordinal, final char c) {
		this.c = c;
	}

	public char getChar() {
		return this.c;
	}
}
