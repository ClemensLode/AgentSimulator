package com.clawsoftware.agentsimulator.Misc;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

/**
 * This auxiliary class handles the output of errors and results initialize()
 * needs to be called at the beginning of each agent log file finalise() needs
 * to be called at the end
 *
 * @author Clemens Lode, clemens at lode.de, University Karlsruhe (TH)
 */
public class Log {

	private static BufferedWriter out;
	private static BufferedWriter error_out;
	private static boolean doLog = false;
	private static boolean errorLogInitialized = false;
	private static String errorLogFileName = null;
	private static boolean notFinalised = false;
	private static BufferedWriter custom_out;

	/**
	 * @return the doLog
	 */
	public static boolean isDoLog() {
		return doLog;
	}

	/**
	 * Don't allow instances of this class
	 */
	private Log() {
	}

	/**
	 * initialized the log file
	 * 
	 * @param do_log
	 *            no logging will be done if set to false
	 */
	public static void initialize(final boolean do_log) {
		doLog = do_log;

		if (!isDoLog()) {
			return;
		}

		if (notFinalised) {
			finalise();
		}
		notFinalised = true;

		try {
			final String filename = Misc.getFileName("log") + ".txt";
			out = new BufferedWriter(new FileWriter(filename));
		} catch (final Exception e) {
			errorLog("initialize log failed: ", e);
		}
	}

	public static void log(final double d) {
		log(String.valueOf(d));
	}

	public static void log(final int i) {
		log(String.valueOf(i));
	}

	public static void log(final boolean b) {
		log(String.valueOf(b));
	}

	public static void log(final long l) {
		log(String.valueOf(l));
	}

	/**
	 * output to the log file if logging is activated
	 * 
	 * @param s
	 *            the String to put into the file
	 */
	public static void log(final String s) {
		if (!isDoLog()) {
			return;
		}
		try {
			out.write(s + "\n");
		} catch (final IOException e) {
			errorLog("Error writing log: ", e);
		}
	}

	/**
	 * Append the String to a data file with the prefix and close the file
	 * 
	 * @param prefix
	 *            Prefix of the data file
	 * @param s
	 *            String to append to the file
	 */
	public static void newCustomLog(final String prefix) {
		final String file_name = prefix + ".dat";
		try {
			custom_out = new BufferedWriter(new FileWriter(file_name, true));
		} catch (final IOException e) {
			errorLog("Error writing custom log: ", e);
		}
	}

	public static void customLog(final String s) throws Exception {
		custom_out.write(s);
	}

	public static void closeCustomLog() {
		try {
			custom_out.flush();
			custom_out.close();
		} catch (final IOException e) {
			errorLog("Error writing custom log: ", e);
		}
	}

	/**
	 * Create a new error log if it hasn't been already created, write the
	 * String and close the log
	 * 
	 * @param s
	 *            String to write in the error log
	 */
	public static void errorLog(final String s) {
		System.out.println("ERROR: " + s);
		if (!errorLogInitialized) {
			try {
				if (errorLogFileName == null) {
					errorLogFileName = Misc.getFileName("error") + ".txt";
				}
				error_out = new BufferedWriter(new FileWriter(errorLogFileName,
						true));
				errorLogInitialized = true;
			} catch (final Exception e) {
				System.err.print(e);
				e.printStackTrace();
				return;
			}

		}

		try {
			error_out.write(s);
		} catch (final IOException e) {
			System.err.print(e);
			e.printStackTrace();
		}

		closeErrorLog();
	}

	/**
	 * flushes and closes the error log if it was initialized
	 */
	private static void closeErrorLog() {
		if (errorLogInitialized) {
			try {
				error_out.flush();
				error_out.close();
				errorLogInitialized = false;
			} catch (final IOException e) {
				System.err.print(e);
				e.printStackTrace();
			}
		}
	}

	/**
	 * Format an exception to a readable string and write it to the error log
	 * 
	 * @param s
	 *            String to explain the error
	 * @param e
	 *            The exception that we want to write to the error log
	 */
	public static void errorLog(final String s, final Throwable e) {
		final StringBuilder error_string = new StringBuilder();
		final String NEW_LINE = System.getProperty("line.separator");

		error_string.append("- " + s);
		error_string.append(NEW_LINE);
		error_string.append("  ( " + e.toString() + " )");
		error_string.append(NEW_LINE);
		for (final StackTraceElement ste : e.getStackTrace()) {
			error_string.append(ste.toString());
			error_string.append(NEW_LINE);
		}
		error_string.append(NEW_LINE);

		errorLog(error_string.toString());
	}

	/**
	 * closes the error log and the log
	 */
	public static void finalise() {
		if (!notFinalised) {
			return;
		}
		closeErrorLog();

		if (!isDoLog()) {
			return;
		}

		try {
			out.flush();
			out.close();
		} catch (final IOException e) {
			errorLog("finalise failed: ", e);
		}
		notFinalised = false;
	}

	/**
	 * flushes the error log
	 */
	private static void flushErrorLog() {
		if (errorLogInitialized) {
			try {
				error_out.flush();
			} catch (final IOException e) {
				System.err.print(e);
				e.printStackTrace();
			}
		}
	}

	/**
	 * flushes the error log and the log
	 */
	public static void flush() {
		flushErrorLog();

		if (!isDoLog()) {
			return;
		}

		try {
			out.flush();
		} catch (final IOException e) {
			errorLog("flush failed: ", e);
		}
	}
}
