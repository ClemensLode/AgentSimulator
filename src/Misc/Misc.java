package com.clawsoftware.agentsimulator.Misc;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;

import com.clawsoftware.agentsimulator.agent.Configuration;

/**
 *
 * This static class contains all in/output and random functions
 *
 * @author Clemens Lode, clemens at lode.de, University Karlsruhe (TH)
 */
public class Misc {

	private static Random generator = new Random();
	private static String outputDirectory = null;
	private static String baseDate = null;
	private static int counter = 1;
	private static BufferedWriter plot_out;

	/**
	 * don't allow instances of this class
	 */
	private Misc() {
	}

	public static void initSeed(final long seed) {
		generator.setSeed(seed);
	}

	/**
	 * Creates a new output directory, marked with the current time index All
	 * output files will be put there
	 */
	public static void initNewOutputDirectory(final String date) {
		baseDate = date;
		outputDirectory = "output_" + baseDate;
		final File output_dir = new File(outputDirectory);
		if (!output_dir.exists()) {
			output_dir.mkdir();
		}
	}

	// http://en.wikipedia.org/wiki/Knuth_shuffle
	public static int[] getRandomArray(final int length) {
		final int[] array = new int[length];
		for (int i = 0; i < length; i++) {
			array[i] = i;
		}

		int n = length; // The number of items left to shuffle (loop invariant).
		while (n > 1) {
			final int k = generator.nextInt(n); // 0 <= k < n.
			n--; // n is now the last pertinent index;
			final int temp = array[n]; // swap array[n] with array[k] (does
										// nothing if k == n).
			array[n] = array[k];
			array[k] = temp;
		}

		return array;
	}

	/**
	 * @return The current output directory
	 * @throws java.lang.Exception
	 *             If the output directory was not initialized
	 */
	public static String getOutputDirectory() throws Exception {
		if (outputDirectory == null) {
			throw new Exception("initNewOutputDirectory was not called!");
		}
		return outputDirectory;
	}

	/**
	 * Generate new random integer [0;n[
	 * 
	 * @param n
	 *            Upper boundary
	 * @return the random number
	 */
	public static int nextInt(final int n) {
		return generator.nextInt(n);
	}

	/**
	 * @return a new random number [0;1]
	 */
	public static double nextDouble() {
		return generator.nextDouble();
	}

	public static double round(final double d, final double increment) {
		return Math.rint(d / increment) * increment;
		// return ((int) ((d + 0.5 * increment) / increment) * increment);
	}

	/**
	 * @param prefix
	 *            Prefix of the file name
	 * @return A new file name, marked with the current time index
	 * @throws java.lang.Exception
	 *             If the output directory was not initialized
	 */
	public static String getFileName(final String prefix) throws Exception {
		return getOutputDirectory() + "/" + prefix + "-" + baseDate + "-"
				+ counter;
	}

	/**
	 * @param prefix
	 *            Prefix of the file name
	 * @return The file name without the experiment counter, for averaged data
	 *         and config files
	 * @throws java.lang.Exception
	 *             If the output directory was not initialized
	 */
	public static String getBaseFileName(final String prefix) throws Exception {
		return getOutputDirectory() + "/" + prefix + "-" + baseDate;
	}

	public static String getFileName(final String prefix, final int c)
			throws Exception {
		return getOutputDirectory() + "/" + getShortFileName(prefix, c);
	}

	public static String getShortFileName(final String prefix, final int c) {
		return prefix + "-" + baseDate + "-" + c;
	}

	public static void nextExperiment() {
		counter++;
	}

	public static void resetExperimentCounter() {
		counter = 1;
	}

	public static void initPlotFile() {
		String entry = new String("");
		entry += "set key left box\n" + "set xrange [0:"
				+ Configuration.getTotalTimeSteps() + "]\n";
		final String file_name = "plot-all-" + baseDate + ".plt";

		try {
			plot_out = new BufferedWriter(new FileWriter(file_name, true));
			plot_out.write(entry);
		} catch (final Exception e) {
			Log.errorLog("Unable to open plot file: ", e);
		}
	}

	private static void closePlotFile() {
		try {
			plot_out.flush();
			plot_out.close();
		} catch (final Exception e) {
			Log.errorLog("Unable to close plot file: ", e);
		}
	}

	public static void appendPlotFile() {
		String header = new String("");
		String do_plot1 = new String("");
		String do_plot2 = new String("");

		header += "set output \"plot_";
		do_plot1 += ".eps\"\n" + "set terminal postscript eps\n" + "plot ";
		do_plot2 += ".png\"\n" + "set terminal png\n" + "plot ";

		final String[] stats = { "average_last_x_steps_goal_agent_observed",
				"best_last_x_steps_goal_agent_observed" };// ,
															// "goal_percentage"};//,
															// "points_spread",
															// "points_average",
															// "distance_spread",
															// "goal_agent_distance_spread",
															// "distance_average",
															// "goal_agent_distance_average",
															// "covered_area"};
		final String[] yrange = { "0:50", "0:50", "0:50", "0:50" };
		// "0:" + Configuration.getNumberOfSteps() *
		// Configuration.getNumberOfProblems(), "0:" + 2 *
		// Configuration.getSightRange(), "0:" + 2 *
		// Configuration.getSightRange(), "0:" + 2 *
		// Configuration.getSightRange(), "0:" + 2 *
		// Configuration.getSightRange(), "0.9:1.0", "0.95:1.0"};
		String entry = new String("");

		for (int i = 0; i < stats.length; i++) {
			entry += "set style line " + (1 + i) + " lt " + (1 + i) + " lw 1\n";
		}

		System.out.println("Creating plot file...");

		entry += "set yrange [" + yrange[0] + "]\n";
		String dat_files = new String("");
		int n = 0;
		for (final String s : stats) {
			n++;
			dat_files += "\"output_" + baseDate + "\\\\" + s + "-" + baseDate
					+ ".dat\" with lines ls " + n;
			if (n < stats.length) {
				dat_files += ", ";
			}
		}

		dat_files += "\n";
		entry += header + "comp" + "-" + baseDate + do_plot1 + dat_files;
		entry += header + "comp" + "-" + baseDate + do_plot2 + dat_files;

		try {
			plot_out.write(entry);
		} catch (final IOException e) {
			Log.errorLog("Unable to append data to plot file: ", e);
		}
		closePlotFile();
		System.out.println("Done creating plot file.");
	}
}
