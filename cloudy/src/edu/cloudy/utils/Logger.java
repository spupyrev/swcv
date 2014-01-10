package edu.cloudy.utils;

/**
 * Apr 25, 2013 change to log4j
 */
public class Logger {
	public static boolean doLogging = true;

	public static void log(String message) {
		if (doLogging)
			System.out.print(message);
	}

	public static void print(String message) {
		if (doLogging)
			System.out.print(message);
	}

	public static void println(String message) {
		if (doLogging)
			System.out.println(message);
	}

	public static void printf(String message, Object... o) {
		if (doLogging)
			System.out.printf(message, o);
	}
}
