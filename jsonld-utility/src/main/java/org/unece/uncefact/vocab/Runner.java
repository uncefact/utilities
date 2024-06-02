package org.unece.uncefact.vocab;

import org.apache.commons.cli.*;
import org.apache.commons.lang3.StringUtils;
import org.unece.uncefact.vocab.md.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.*;
import java.util.jar.Attributes;
import java.util.jar.Manifest;


public class Runner {
    final static Options options = new Options();

    public static void main(String[] args) throws ParseException, IOException {
        System.setErr(new PrintStream("err.md"));
        System.setOut(new PrintStream("out.md"));
        Attributes mainAttributes = readProperties();
        String version = mainAttributes.getValue("Implementation-Version");

        Option runningModeOption = new Option("m", true, "running mode.\n" +
                String.format("Allowed values: %s, %s, %s.\n", "merge", "md", "cleanup") +
                String.format("Default value: %s.", "merge"));
        Option prettyPrintOption = new Option("p", "pretty-print",false, "an output file to be created as a result of transformation. Default value: output.jsonld.");
        Option workindDirOption = new Option("d", "directory",true, "Working directory for files to be merged or for md generation. Default value for merge - \"vocab/\", for md - \"\"");
        Option inputFilesOption = new Option("i", "input-file",true, "Input file name to be merged or for md generation. Default value for md generation - \"merged.jsonld/\"");
        Option outputFileNameOption = new Option("o", "output-file",true, "Input file name to be merged or for md generation. Default value for md generation - \"merged.jsonld/\"");
        Option versionOption = new Option("?", "version", false, "display this help.");

        options.addOption(runningModeOption);
        options.addOption(prettyPrintOption);
        options.addOption(workindDirOption);
        options.addOption(inputFilesOption);
        options.addOption(outputFileNameOption);
        options.addOption(versionOption);

        HelpFormatter formatter = new HelpFormatter();

        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = parser.parse(options, args);

        if (cmd.hasOption(versionOption.getOpt())) {
            formatter.printHelp(String.format("java -jar vocab-transformer-%s.jar", version), options);
            return;
        }

        boolean prettyPrint = false;
        String runningMode = "merge";
        String workingDir = null;
        String outputFileName = null;
        Set<String> inputFileNames = new TreeSet<>();
        Iterator<Option> optionIterator = cmd.iterator();
        while (optionIterator.hasNext()) {
            Option option = optionIterator.next();
            String opt = StringUtils.defaultIfEmpty(option.getOpt(), "");
            if (opt.equals(prettyPrintOption.getOpt())) {
                prettyPrint = Boolean.TRUE;
            } else if (opt.equals(runningModeOption.getOpt())) {
                runningMode = option.getValue();
            } else if (opt.equals(workindDirOption.getOpt())) {
                workingDir = option.getValue();
            } else if (opt.equals(outputFileNameOption.getOpt())) {
                outputFileName = option.getValue();
            } else if (opt.equals(inputFilesOption.getOpt())) {
                inputFileNames.add(option.getValue());
            }
        }
        if (workingDir == null) {
            switch (runningMode.toLowerCase()){
                case "merge":
                    workingDir = "vocab/";
                    break;
                case "md-locode":
                    workingDir = ".";
                case "md":
                case "minify":
                case "pretty-print":
                    workingDir = "";
                    break;
            }
        }

        if (outputFileName == null) {
            switch (runningMode.toLowerCase()){
                case "merge":
                    outputFileName = "merged.jsonld";
                    break;
                case "minify":
                    outputFileName = "minified.jsonld";
                    break;
                case "pretty-print":
                    outputFileName = "pretty-print.jsonld";
                    break;
            }
        }

        if (inputFileNames.isEmpty()) {
            switch (runningMode.toLowerCase()){
                case "md":
                case "minify":
                case "pretty-print":
                    inputFileNames.add("merged.jsonld");
                    break;
            }
        }
        switch (runningMode.toLowerCase()) {
            case "merge":
                JsonLDMerge.merge(workingDir, inputFileNames, prettyPrint, outputFileName);
                break;

            case "md":
                DomainsToMD.generate(workingDir, inputFileNames);
                break;

            case "md-locode":
                UNLCDToMD.generate(workingDir);
                UNLCDCountriesToMD.generate(workingDir);
                UNLCDFunctionsToMD.generate(workingDir);
                UNLCDSubdivisionsToMD.generate(workingDir);
                break;
            case "minify":
                JsonLDMerge.format(workingDir, inputFileNames, false, outputFileName);
                break;

            case "pretty-print":
                JsonLDMerge.format(workingDir, inputFileNames, true, outputFileName);
                break;
        }
    }

    public static Attributes readProperties() throws IOException {
        final InputStream resourceAsStream = ClassLoader.getSystemResourceAsStream("META-INF/MANIFEST.MF");
        final Manifest manifest = new Manifest(resourceAsStream);
        final Attributes mainAttributes = manifest.getMainAttributes();
        return mainAttributes;
    }
}
