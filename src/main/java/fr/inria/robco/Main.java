package fr.inria.robco;

import org.apache.commons.cli.*;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Created by aelie on 25/08/16.
 */
public class Main {
    static final int PEAQ = 0;
    static int metric;

    public static void main(String[] args) {
        String program = "";
        String reference = "";
        String test = "";

        Options optionsMain = new Options();
        optionsMain.addOption("h", "help", false, "display this message");
        optionsMain.addOption("p", "peaqb", true, "path to the PEAQb program");
        optionsMain.addOption("r", "reference", true, "path to the reference sound file");
        optionsMain.addOption("t", "test", true, "path to the test sound file");
        optionsMain.addOption("m", "metric", true, "name of the metric to be used");
        CommandLineParser commandLineParser = new DefaultParser();
        CommandLine commandLine = null;
        try {
            commandLine = commandLineParser.parse(optionsMain, args);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        if(args.length == 0 || commandLine.hasOption("help")) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("Robco", optionsMain);
            return;
        }
        if (commandLine.hasOption("peaqb")) {
            program = commandLine.getOptionValue("peaqb");
            System.out.println("Using program " + program);
        } else {
            System.err.println("No PEAQb program specified");
            System.exit(1);
        }
        if (commandLine.hasOption("reference")) {
            reference = commandLine.getOptionValue("reference");
            System.out.println("Using reference " + reference);
        } else {
            System.err.println("No reference file specified");
            System.exit(1);
        }
        if (commandLine.hasOption("test")) {
            test = commandLine.getOptionValue("test");
            System.out.println("Using test " + test);
        } else {
            System.err.println("No test file specified");
            System.exit(1);
        }
        if (commandLine.hasOption("metric")) {
            String metricParam = commandLine.getOptionValue("metric");
            if(metricParam.equalsIgnoreCase("PEAQ")) {
                metric = PEAQ;
                System.out.println("Using metric " + metricParam);
            } else {
                System.err.println("Couldn't recognize metric " + metricParam);
                System.exit(1);
            }
        } else {
            System.err.println("No metric specified");
            System.exit(1);
        }
        switch(metric) {
            case PEAQ:
                System.out.println("MeanODG=" + executePEAQAnalysis(program, reference, test).getMean());
            default:
                return;
        }
    }

    static SummaryStatistics executePEAQAnalysis(String program, String reference, String test) {
        SummaryStatistics stats = new SummaryStatistics();
        try {
            System.out.println("Starting analysis, this may take a while...");
            Process p = Runtime.getRuntime().exec(program + " -r " + reference + " -t " + test);
            BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line = "";
            while((line = br.readLine()) != null) {
                if(line.startsWith("ODG")) {
                    System.out.print("-");
                    stats.addValue(Double.parseDouble(line.split("\\s")[1]));
                }
            }
            System.out.println();
            System.out.println("Done!");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return stats;
    }
}
