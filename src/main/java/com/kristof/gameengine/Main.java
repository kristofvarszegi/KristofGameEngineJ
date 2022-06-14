package com.kristof.gameengine;

import com.kristof.gameengine.engine.Engine;

public class Main {
	public static void main(String[] argv) {
		try {
			final Engine engine = new Engine("engine.ini");
			engine.run();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}