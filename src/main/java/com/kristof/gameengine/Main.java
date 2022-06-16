package com.kristof.gameengine;

import com.kristof.gameengine.engine.Engine;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Main {
	private static final Logger LOGGER = LogManager.getLogger(Main.class);

	public static void main(String[] argv) {
		LOGGER.debug("Main started");
		try {
			final Engine engine = new Engine("engine.ini");
			engine.run();
		} catch (Exception e) {
			e.printStackTrace();
		}
		LOGGER.debug("Main terminates");
	}
}