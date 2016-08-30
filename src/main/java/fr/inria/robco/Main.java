package fr.inria.robco;

import fr.inria.robco.metrics.Ssi;
import fr.inria.robco.utils.SQLiteConnector;
import org.apache.commons.cli.*;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.List;

/**
 * Created by aelie on 25/08/16.
 */
public class Main {
    static final int PEAQ = 0;
    static final int SSIM = 1;
    static int metric;

    public static void main(String[] args) {
        String database = "";
        String programPEAQ = "";
        String reference = "";
        String test = "";

        Options optionsMain = new Options();
        optionsMain.addOption("h", "help", false, "display this message");
        optionsMain.addOption("db", "database", true, "path to the database");
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
        if (args.length == 0 || commandLine.hasOption("help")) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("Robco", optionsMain);
            return;
        }
        if (commandLine.hasOption("database")) {
            database = commandLine.getOptionValue("database");
            System.out.println("Using database " + database);
        } else {
            System.err.println("No database file specified");
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
            if (metricParam.equalsIgnoreCase("PEAQ")) {
                metric = PEAQ;
                System.out.println("Using metric " + metricParam);
                if (commandLine.hasOption("peaqb")) {
                    programPEAQ = commandLine.getOptionValue("peaqb");
                    System.out.println("Using programPEAQ " + programPEAQ);
                } else {
                    System.err.println("No PEAQb programPEAQ specified");
                    System.exit(1);
                }
            } else if (metricParam.equalsIgnoreCase("SSIM")) {
                metric = SSIM;
                System.out.println("Using metric " + metricParam);
            } else {
                System.err.println("Couldn't recognize metric " + metricParam);
                System.exit(1);
            }
        } else {
            System.err.println("No metric specified");
            System.exit(1);
        }
        double result;
        switch (metric) {
            case PEAQ:
                SQLiteConnector connectorPEAQ = new SQLiteConnector(database, "peaq", "ODG", "REF", "TEST", "ID");
                result = executePEAQAnalysis(programPEAQ, reference, test).getMean();
                System.out.println("MeanODG=" + result);
                connectorPEAQ.write(result, reference, test, getUsableId(connectorPEAQ) + 1);
                System.exit(0);
            case SSIM:
                SQLiteConnector connectorSSIM = new SQLiteConnector(database, "ssim", "VALUE", "REF", "TEST", "ID");
                result = executeSSIMAnalysis(reference, test);
                System.out.println("SSIMIndex=" + result);
                connectorSSIM.write(result, reference, test, getUsableId(connectorSSIM) + 1);
                System.exit(0);
            default:
                return;
        }
    }

    static int getUsableId(SQLiteConnector connector) {
        List<Integer> ids = connector.getIdList();
        Collections.sort(ids);
        return ids.size() > 0 ? ids.get(ids.size() - 1) : 0;
    }

    static SummaryStatistics executePEAQAnalysis(String program, String reference, String test) {
        SummaryStatistics stats = new SummaryStatistics();
        try {
            System.out.println("Starting PEAQ analysis, this may take a while...");
            Process p = Runtime.getRuntime().exec(program + " -r " + reference + " -t " + test);
            BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line = "";
            while ((line = br.readLine()) != null) {
                if (line.startsWith("ODG")) {
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

    static double executeSSIMAnalysis(String reference, String test) {
        double result = -1;
        System.out.println("Starting SSIM analysis, this may take a while...");
        Ssi ssi = new Ssi();
        result = ssi.index(reference, test);
        System.out.println("Done!");
        return result;
    }
}
