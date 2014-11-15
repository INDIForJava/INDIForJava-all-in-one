package org.indilib.i4j.server.examples;

/*
 * #%L
 * INDI for Java Server Examples
 * %%
 * Copyright (C) 2012 - 2014 indiforjava
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.indilib.i4j.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("static-access")
public class INDICommandLine {

    /**
     * Logger to log to.
     */
    private static final Logger LOG = LoggerFactory.getLogger(INDICommandLine.class);

    private final Option help = OptionBuilder //
            .withDescription("print this message") //
            .create("help");

    private final Option list = OptionBuilder //
            .withDescription("Lists all loaded drivers") //
            .withLongOpt("list")//
            .create("ld");

    private final Option listAvailable = OptionBuilder //
            .withDescription("Lists all posible drivers") //
            .withLongOpt("listAll").create("la");

    private final Option stop = OptionBuilder //
            .withDescription("Stops the Server and breaks all Client connections") //
            .withLongOpt("stop")//
            .create("s");

    private final Option home = OptionBuilder //
            .withDescription("indi base directory ~/.i4j") //
            .hasArgs() //
            .withArgName("directory")//
            .withLongOpt("indiHome")//
            .create("d");

    private final Option host = OptionBuilder //
            .withDescription("host interface to bind the server") //
            .hasArgs() //
            .withLongOpt("host")//
            .withArgName("host-ip")//
            .create("h");

    private final Option port = OptionBuilder //
            .withDescription("ip port to bind the server") //
            .hasArgs()//
            .withType(Integer.class) //
            .withLongOpt("port")//
            .withArgName("port-number")//
            .create("p");

    private final Option add = OptionBuilder //
            .withDescription("Loads all INDIDrivers in the jarFile") //
            .hasArgs() //
            .withLongOpt("addJar")//
            .withArgName("jar")//
            .withValueSeparator(':')//
            .create("j");

    private final Option addC = OptionBuilder //
            .withDescription("Loads the INDIDriver specified by the case insensitive class name (simple orf full)") //
            .hasArgs() //
            .withLongOpt("addClass")//
            .withArgName("class")//
            .withValueSeparator(':')//
            .create("c");

    private final Option removeC = OptionBuilder //
            .withDescription("Removes the INDIDriver specified by the case insensitive class name (simple orf full)") //
            .hasArgs() //
            .withArgName("class")//
            .withLongOpt("removeClass")//
            .withValueSeparator(':')//
            .create("r");

    private final Option addN = OptionBuilder //
            .withDescription("Loads the native driver described by driverPath") //
            .hasArgs() //
            .withLongOpt("addNative") //
            .withArgName("executable")//
            .withValueSeparator(':')//
            .create("n");

    private final Option removeN = OptionBuilder //
            .withDescription("Removes the native driver described by driverPath") //
            .hasArgs() //
            .withLongOpt("removeNative") //
            .withArgName("executable")//
            .withValueSeparator(':')//
            .create("rn");

    private final Option connect = OptionBuilder //
            .withDescription("Loads the drivers in a remote INDI server") //
            .hasArgs() //
            .withValueSeparator(':')//
            .withLongOpt("connect")//
            .withArgName("host[:port]")//
            .create("nc");

    private final Option disconnect = OptionBuilder //
            .withDescription("Removes the drivers in a remote INDI server") //
            .hasArgs() //
            .withValueSeparator(':')//
            .withArgName("host[:port]")//
            .withLongOpt("disconnect")//
            .create("dn");

    private final Option interactive = OptionBuilder //
            .withDescription("The server listens to an interactive command interface (default no command interface will be started)") //
            .withLongOpt("interactive")//
            .create("i");

    private final Option lib = OptionBuilder //
            .withDescription("directory with jar files to add to the classpath default $indiHome/lib and directory of the server.jar") //
            .hasArgs() //
            .withValueSeparator(':')//
            .withArgName("directory-list")//
            .withLongOpt("lib")//
            .create("l");

    private final Option startup = OptionBuilder //
            .withDescription("the commands in the specified file are executed on startup (defaults to $indiHome/etc/server.boot)") //
            .hasArgs() //
            .withLongOpt("file")//
            .withLongOpt("startup")//
            .create("b");

    private final Options options = new Options();

    private CommandLine commandLine;

    private INDIBasicServer basicServer;

    public INDICommandLine setBasicServer(INDIBasicServer basicServer) {
        this.basicServer = basicServer;
        if (startupCommandLines != null) {
            for (INDICommandLine startupCommandLine : startupCommandLines) {
                startupCommandLine.setBasicServer(basicServer);
            }
        }
        return this;
    }

    private List<INDICommandLine> startupCommandLines;

    public INDICommandLine() {
        options.addOption(help);
        options.addOption(list);
        options.addOption(listAvailable);
        options.addOption(stop);
        options.addOption(home);
        options.addOption(host);
        options.addOption(port);
        options.addOption(add);
        options.addOption(addC);
        options.addOption(removeC);
        options.addOption(addN);
        options.addOption(removeN);
        options.addOption(connect);
        options.addOption(disconnect);
        options.addOption(interactive);
        options.addOption(lib);
        options.addOption(startup);
    }

    public INDICommandLine(String commandLine) throws Exception {
        this();
        parseArgument(splittString(commandLine));
    }

    public void printHelp() {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("i4j-server", options);
    }

    public void printInteractiveHelp() {
        HelpFormatter formatter = new HelpFormatter();
        formatter.defaultOptPrefix = "";
        formatter.defaultLongOptPrefix = "";
        formatter.printHelp("i4j-server", options);

    }

    public INDICommandLine parseArgument(String[] args) throws Exception {
        CommandLineParser parser = new PosixParser();
        commandLine = parser.parse(options, args);
        this.startupCommandLines = parseStartupCommands();
        return this;
    }

    public Integer getPort() throws ParseException {
        Integer parsedOptionValue = (Integer) commandLine.getParsedOptionValue(port.getLongOpt());
        if (parsedOptionValue == null && startupCommandLines != null) {
            for (INDICommandLine startupCommand : startupCommandLines) {
                parsedOptionValue = startupCommand.getPort();
                if (parsedOptionValue != null) {
                    return parsedOptionValue;
                }
            }
        }
        return parsedOptionValue;
    }

    public String getHost() throws ParseException {
        String optionValue = commandLine.getOptionValue(host.getLongOpt());
        if (optionValue == null && startupCommandLines != null) {
            for (INDICommandLine startupCommand : startupCommandLines) {
                optionValue = startupCommand.getHost();
                if (optionValue != null) {
                    return optionValue;
                }
            }
        }
        return optionValue;
    }

    public Option[] getOptions() {
        return commandLine.getOptions();
    }

    public void execute(boolean interactive) {
        if (!interactive && startupCommandLines != null) {
            for (INDICommandLine startupCommandLine : startupCommandLines) {
                startupCommandLine.execute(interactive);
            }
        }
        for (Option option : commandLine.getOptions()) {
            if (option.equals(help)) {
                if (interactive) {
                    printInteractiveHelp();
                } else {
                    printHelp();
                }
            } else if (option.equals(list)) {
                basicServer.listDevices();
            } else if (option.equals(listAvailable)) {
                basicServer.listAvailableDevices();
            } else if (option.equals(stop)) {
                basicServer.stopServer();
            } else if (option.equals(home)) {
                FileUtils.setI4JBaseDirectory(commandLine.getOptionValue(home.getLongOpt()));
            } else if (option.equals(host) || option.equals(port)) {
                if (interactive) {
                    basicServer.print("server already started change of host or port has no effect.");
                }
            } else if (option.equals(add)) {
                basicServer.loadJava(commandLine.getOptionValue(option.getLongOpt()));
            } else if (option.equals(addC)) {
                basicServer.loadJavaClass(commandLine.getOptionValue(option.getLongOpt()));
            } else if (option.equals(removeC)) {
                basicServer.unloadJavaClass(commandLine.getOptionValue(option.getLongOpt()));
            } else if (option.equals(addN)) {
                basicServer.loadNative(commandLine.getOptionValue(option.getLongOpt()));
            } else if (option.equals(removeN)) {
                basicServer.unloadNative(commandLine.getOptionValue(option.getLongOpt()));
            } else if (option.equals(connect)) {
                String[] optionValues = commandLine.getOptionValues(option.getLongOpt());
                basicServer.connect(optionValues[0], Integer.parseInt(optionValues[1]));
            } else if (option.equals(disconnect)) {
                String[] optionValues = commandLine.getOptionValues(option.getLongOpt());
                basicServer.connect(optionValues[0], Integer.parseInt(optionValues[1]));
            } else if (option.equals(this.interactive)) {
                if (interactive) {
                    basicServer.print("server already interactive.");
                }
            } else if (option.equals(lib)) {
                String[] libs = commandLine.getOptionValues(option.getLongOpt());
                for (String libDirectory : libs) {
                    basicServer.addLib(libDirectory);
                }
            } else if (option.equals(startup)) {
                if (interactive) {
                    basicServer.print("server already started, no startup possible anymore.");
                }
            } else {
                basicServer.print("unknown command: " + option);
            }
        }
    }

    public List<INDICommandLine> parseStartupCommands() throws Exception {
        if (commandLine.hasOption(home.getLongOpt())) {
            FileUtils.setI4JBaseDirectory(commandLine.getOptionValue(home.getLongOpt()));
        }
        if (!commandLine.hasOption(startup.getLongOpt())) {
            return startup(new File(FileUtils.getI4JBaseDirectory(), "server.boot").getAbsolutePath());
        }
        return null;
    }

    private List<INDICommandLine> startup(String fileName) throws Exception {
        List<INDICommandLine> startupCommands = new ArrayList<>();
        File file = new File(fileName);
        if (file.exists()) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), Charset.defaultCharset()));
            String line = reader.readLine();
            while (line != null) {
                startupCommands.add(new INDICommandLine().parseArgument(splittString(line.trim())));
                line = reader.readLine();
            }
            reader.close();
        }
        return startupCommands;
    }

    private String[] splittString(String command) {
        ArrayList<String> subStrings = new ArrayList<>();
        boolean lastCharBackslash = false;
        StringBuffer current = new StringBuffer();
        for (int index = 0; index < command.length(); index++) {
            char character = command.charAt(index);
            if (character == '\\') {
                if (lastCharBackslash) {
                    current.append('\\');
                    lastCharBackslash = false;
                } else {
                    lastCharBackslash = true;
                }
            } else if (Character.isWhitespace(character)) {
                if (lastCharBackslash) {
                    current.append(character);
                } else if (current.length() > 0) {
                    subStrings.add(current.toString());
                    current.setLength(0);
                }
                lastCharBackslash = false;
            } else {
                if (lastCharBackslash) {
                    current.append('\\');
                    lastCharBackslash = false;
                }
                current.append(character);
            }
        }
        if (current.length() > 0) {
            subStrings.add(current.toString());
        }
        // now change the first string to an option.
        if (subStrings.size() > 0) {
            String firstString = subStrings.get(0);
            if (!firstString.startsWith("-")) {
                // is it the long option variant or the short?
                if (firstString.length() <= 2) {
                    subStrings.set(0, "-" + firstString);
                } else {
                    subStrings.set(0, "--" + firstString);
                }
            }
        }
        return subStrings.toArray(new String[subStrings.size()]);
    }

    public boolean isInteractive() {
        boolean isInteractive = commandLine.hasOption(interactive.getLongOpt());
        if (isInteractive) {
            return true;
        }
        if (startupCommandLines != null) {
            for (INDICommandLine startupCommand : startupCommandLines) {
                if (startupCommand.isInteractive()) {
                    return true;
                }
            }
        }
        return false;
    }
}
