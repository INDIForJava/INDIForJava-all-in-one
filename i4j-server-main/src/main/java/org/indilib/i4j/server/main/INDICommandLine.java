package org.indilib.i4j.server.main;

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
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
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
import org.indilib.i4j.server.api.INDIDeviceInterface;
import org.indilib.i4j.server.api.INDIServerInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The commandline processing class.
 * 
 * @author Richard van Nieuwenhoven
 */
@SuppressWarnings("static-access")
public class INDICommandLine {

    /**
     * Logger to log to.
     */
    private static final Logger LOG = LoggerFactory.getLogger(INDICommandLine.class);

    /**
     * the help option.
     */
    private final Option help = OptionBuilder //
            .withDescription("print this message") //
            .create("help");

    /**
     * the Lists all loaded drivers option.
     */
    private final Option list = OptionBuilder //
            .withDescription("Lists all loaded drivers") //
            .withLongOpt("list")//
            .create("ld");

    /**
     * the Lists all posible drivers option.
     */
    private final Option listAvailable = OptionBuilder //
            .withDescription("Lists all posible drivers") //
            .withLongOpt("listAll").create("la");

    /**
     * the Stops the Server and breaks all Client connections option.
     */
    private final Option stop = OptionBuilder //
            .withDescription("Stops the Server and breaks all Client connections") //
            .withLongOpt("stop")//
            .create("s");

    /**
     * the indi base directory option.
     */
    private final Option home = OptionBuilder //
            .withDescription("indi base directory ~/.i4j") //
            .hasArgs() //
            .withArgName("directory")//
            .withLongOpt("indiHome")//
            .create("d");

    /**
     * the host interface to bind the server option.
     */
    private final Option host = OptionBuilder //
            .withDescription("host interface to bind the server") //
            .hasArgs() //
            .withLongOpt("host")//
            .withArgName("host-ip")//
            .create("h");

    /**
     * the ip port to bind the server option.
     */
    private final Option port = OptionBuilder //
            .withDescription("ip port to bind the server") //
            .hasArgs()//
            .withType(Integer.class) //
            .withLongOpt("port")//
            .withArgName("port-number")//
            .create("p");

    /**
     * the Loads all INDIDrivers in the jarFile option.
     */
    private final Option add = OptionBuilder //
            .withDescription("Loads all INDIDrivers in the jarFile") //
            .hasArgs() //
            .withLongOpt("addJar")//
            .withArgName("jar")//
            .withValueSeparator(':')//
            .create("j");

    /**
     * the Loads the INDIDriver specified by the case insensitive class name
     * option.
     */
    private final Option addC = OptionBuilder //
            .withDescription("Loads the INDIDriver specified by the case insensitive class name (simple orf full)") //
            .hasArgs() //
            .withLongOpt("addClass")//
            .withArgName("class")//
            .withValueSeparator(':')//
            .create("c");

    /**
     * the Removes the INDIDriver specified by the case insensitive class name
     * option.
     */
    private final Option removeC = OptionBuilder //
            .withDescription("Removes the INDIDriver specified by the case insensitive class name (simple orf full)") //
            .hasArgs() //
            .withArgName("class")//
            .withLongOpt("removeClass")//
            .withValueSeparator(':')//
            .create("r");

    /**
     * the Loads the native driver described by driverPath option.
     */
    private final Option addN = OptionBuilder //
            .withDescription("Loads the native driver described by driverPath") //
            .hasArgs() //
            .withLongOpt("addNative") //
            .withArgName("executable")//
            .withValueSeparator(':')//
            .create("n");

    /**
     * the Removes the native driver described by driverPath option.
     */
    private final Option removeN = OptionBuilder //
            .withDescription("Removes the native driver described by driverPath") //
            .hasArgs() //
            .withLongOpt("removeNative") //
            .withArgName("executable")//
            .withValueSeparator(':')//
            .create("rn");

    /**
     * the Loads the drivers in a remote INDI server option.
     */
    private final Option connect = OptionBuilder //
            .withDescription("Loads the drivers in a remote INDI server") //
            .hasArgs() //
            .withValueSeparator(':')//
            .withLongOpt("connect")//
            .withArgName("host[:port]")//
            .create("nc");

    /**
     * the Removes the drivers in a remote INDI server option.
     */
    private final Option disconnect = OptionBuilder //
            .withDescription("Removes the drivers in a remote INDI server") //
            .hasArgs() //
            .withValueSeparator(':')//
            .withArgName("host[:port]")//
            .withLongOpt("disconnect")//
            .create("dn");

    /**
     * the server listens to an interactive command interface (default no
     * command interface will be started) option.
     */
    private final Option interactive = OptionBuilder //
            .withDescription("The server listens to an interactive command interface (default no command interface will be started)") //
            .withLongOpt("interactive")//
            .create("i");

    /**
     * the directory with jar files to add to the classpath default
     * $indiHome/lib option.
     */
    private final Option lib = OptionBuilder //
            .withDescription("directory with jar files to add to the classpath default $indiHome/lib") //
            .hasArgs() //
            .withValueSeparator(':')//
            .withArgName("directory-list")//
            .withLongOpt("lib")//
            .create("l");

    /**
     * the commands in the specified file are executed on startup (defaults to
     * $indiHome/etc/server.boot) option.
     */
    private final Option startup = OptionBuilder //
            .withDescription("the commands in the specified file are executed on startup (defaults to $indiHome/etc/server.boot)") //
            .hasArgs() //
            .withLongOpt("file")//
            .withLongOpt("startup")//
            .create("b");

    /**
     * the list of options.
     */
    private final Options options = new Options();

    /**
     * the parsed commandline.
     */
    private CommandLine commandLine;

    /**
     * the started basic server.
     */
    private INDIBasicServer basicServer;

    /**
     * set the basic server in this and all sub command lines.
     * 
     * @param newBasicServer
     *            the server to set
     * @return this.
     */
    public INDICommandLine setBasicServer(INDIBasicServer newBasicServer) {
        this.basicServer = newBasicServer;
        if (startupCommandLines != null) {
            for (INDICommandLine startupCommandLine : startupCommandLines) {
                startupCommandLine.setBasicServer(newBasicServer);
            }
        }
        return this;
    }

    /**
     * the sub command lines, from startup scripts .
     */
    private List<INDICommandLine> startupCommandLines;

    /**
     * construct the commandline (add all options together) .
     */
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

    /**
     * construct a kommand line based on a line with arguments (interactive so
     * the first string is missing the "-" prefix.
     * 
     * @param commandLine
     *            the commandline string to parse
     * @throws Exception
     *             if the command was not properly formattet.
     */
    public INDICommandLine(String commandLine) throws Exception {
        this();
        parseArgument(splittString(commandLine));
    }

    /**
     * print the normal help to the std out.
     */
    public void printHelp() {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("i4j-server", options);
    }

    /**
     * print the interactive help to the std out (no "-" and "--" prefixes.
     */
    public void printInteractiveHelp() {
        HelpFormatter formatter = new HelpFormatter();
        formatter.defaultOptPrefix = "";
        formatter.defaultLongOptPrefix = "";
        formatter.printHelp("i4j-server", options);

    }

    /**
     * parse the array of argument strings.
     * 
     * @param args
     *            teh argumants
     * @return this
     * @throws Exception
     *             if the command was not properly formattet.
     */
    public INDICommandLine parseArgument(String[] args) throws Exception {
        CommandLineParser parser = new PosixParser();
        commandLine = parser.parse(options, args);
        this.startupCommandLines = parseStartupCommands();
        return this;
    }

    /**
     * @return the port number (or null) from the commandline optione or one of
     *         it sub commandlines.
     * @throws ParseException
     *             if the given option was no number.
     */
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

    /**
     * @return the host name (or null) that was specified in the command line
     *         options or one of ist sub command lines.
     */
    public String getHost() {
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

    /**
     * execute the given commandline options. including the ones of the sub
     * commandlines.
     * 
     * @param interactiveMode
     *            is this in the interactive mode?
     */
    public void execute(boolean interactiveMode) {
        if (!interactiveMode && startupCommandLines != null) {
            for (INDICommandLine startupCommandLine : startupCommandLines) {
                startupCommandLine.execute(interactiveMode);
            }
        }
        for (Option option : commandLine.getOptions()) {
            try {
                executeCommand(interactiveMode, option);
            } catch (Exception e) {
                LOG.error("could not execute command deu to an exception", e);
                INDIBasicServer.print("error on command " + option + " error message was " + e.getMessage());
            }
        }
    }

    /**
     * execute one option command.
     * 
     * @param interactiveMode
     *            is this in the interactive mode?
     * @param option
     *            the option to execute.
     * @throws Exception
     *             the the action failed.
     */
    protected void executeCommand(boolean interactiveMode, Option option) throws Exception {
        INDIServerInterface server = basicServer.getServer();
        if (option.equals(help)) {
            if (interactiveMode) {
                printInteractiveHelp();
            } else {
                printHelp();
            }
        } else if (option.equals(list)) {
            List<INDIDeviceInterface> devs = this.basicServer.getServer().getDevices();
            INDIBasicServer.print("Number of loaded Drivers: " + devs.size());
            for (INDIDeviceInterface indiDeviceInterface : devs) {
                INDIBasicServer.print("  - " + indiDeviceInterface);
            }
        } else if (option.equals(listAvailable)) {
            List<String> classes = this.basicServer.getServer().getAvailableDevices();
            INDIBasicServer.print("Number of available Drivers: " + classes.size());
            for (String className : classes) {
                INDIBasicServer.print("  - " + className);
            }
        } else {
            if (option.equals(stop)) {
                server.stopServer();
            } else if (option.equals(home)) {
                FileUtils.setI4JBaseDirectory(getArg(home));
            } else if (option.equals(host) || option.equals(port)) {
                if (interactiveMode) {
                    INDIBasicServer.print("server already started change of host or port has no effect.");
                }
            } else if (option.equals(add)) {
                server.loadJavaDriversFromJAR(getArg(option));
            } else if (option.equals(addC)) {
                server.loadJavaDriver(getArg(option));
            } else if (option.equals(removeC)) {
                server.destroyJavaDriver(getArg(option));
            } else if (option.equals(addN)) {
                server.loadNativeDriver(getArg(option));
            } else if (option.equals(removeN)) {
                server.destroyNativeDriver(getArg(option));
            } else if (option.equals(connect)) {
                String[] optionValues = commandLine.getOptionValues(option.getLongOpt());
                server.loadNetworkDriver(optionValues[0], Integer.parseInt(optionValues[1]));
            } else if (option.equals(disconnect)) {
                String[] optionValues = commandLine.getOptionValues(option.getLongOpt());
                server.destroyNetworkDriver(optionValues[0], Integer.parseInt(optionValues[1]));
            } else if (option.equals(this.interactive)) {
                if (interactiveMode) {
                    INDIBasicServer.print("server already interactive.");
                }
            } else if (option.equals(lib)) {
                String[] libs = commandLine.getOptionValues(option.getLongOpt());
                for (String libDirectory : libs) {
                    extendClasspath(new File(libDirectory));
                }
            } else if (option.equals(startup)) {
                if (interactiveMode) {
                    INDIBasicServer.print("server already started, no startup possible anymore.");
                }
            } else {
                INDIBasicServer.print("unknown command: " + option);
            }
        }
    }

    /**
     * @param option
     *            the option to get the argument from
     * @return the option argument.
     */
    private String getArg(Option option) {
        return commandLine.getOptionValue(option.getLongOpt());
    }

    /**
     * @return the parsed startup commands.
     * @throws Exception
     *             if the startup commands where not correctly formatted.
     */
    public List<INDICommandLine> parseStartupCommands() throws Exception {
        if (commandLine.hasOption(home.getLongOpt())) {
            FileUtils.setI4JBaseDirectory(commandLine.getOptionValue(home.getLongOpt()));
        }
        List<INDICommandLine> result = new ArrayList<>();
        if (commandLine.hasOption(startup.getLongOpt())) {
            String startupFileName = commandLine.getOptionValue(startup.getLongOpt());
            File startUpFile = new File(startupFileName);
            if (!startUpFile.isAbsolute()) {
                startUpFile = new File(FileUtils.getI4JBaseDirectory(), startupFileName);
            }
            result.addAll(startup(startUpFile.getAbsolutePath()));
        }
        result.addAll(startup(new File(FileUtils.getI4JBaseDirectory(), "etc/server.boot").getAbsolutePath()));
        return result;
    }

    /**
     * parse the file and convert every line in a INDICommandLine.
     * 
     * @param fileName
     *            the file to read.
     * @return a list with commands from the file (never null)
     * @throws Exception
     *             if the file could not be read or the commands in it are not
     *             legal.
     */
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

    /**
     * splitt the argument in a array of substrings (separator characters ar
     * blanks). use the escape charater backslash to include a whitespace
     * character in a string.
     * 
     * @param command
     *            the string to splitt.
     * @return the array of substrings.
     */
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

    /**
     * @return true if the interactive mode is active in the commandline or in
     *         one of the sub command lines.
     */
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

    /**
     * extend the classpath with one file or a directory with classes. If the
     * directory cotains jars add them all to the classpath else just the
     * directory.
     * 
     * @param dirOrJar
     *            the directory or jar file
     * @throws Exception
     *             if the classpath could not be extended.
     */
    protected static void extendClasspath(File dirOrJar) throws Exception {
        if (!dirOrJar.isAbsolute()) {
            dirOrJar = new File(FileUtils.getI4JBaseDirectory(), dirOrJar.getPath());
        }
        if (!dirOrJar.exists()) {
            LOG.warn("classpath ignored because not existent. " + dirOrJar);
            return;
        }
        if (dirOrJar.isDirectory()) {
            boolean containsJar = false;
            for (File child : dirOrJar.listFiles()) {
                if (child.getName().endsWith(".jar")) {
                    containsJar = true;
                    extendClasspath(child);
                }
            }
            if (containsJar) {
                // no classes dir;
                return;
            }
        }
        URLClassLoader sysLoader = (URLClassLoader) ClassLoader.getSystemClassLoader();
        URL udir = dirOrJar.toURI().toURL();
        Class<URLClassLoader> sysClass = URLClassLoader.class;
        Method method = sysClass.getDeclaredMethod("addURL", new Class[]{
            URL.class
        });
        method.setAccessible(true);
        method.invoke(sysLoader, new Object[]{
            udir
        });
    }

    /**
     * extend the classpath with the spesified libraries.
     * 
     * @throws Exception
     *             if the classpath could not be extended.
     */
    public void addLibraries() throws Exception {
        extendClasspath(new File("lib"));
        if (commandLine.hasOption(lib.getLongOpt())) {
            String[] libs = commandLine.getOptionValues(lib.getLongOpt());
            for (String libDirectory : libs) {
                extendClasspath(new File(libDirectory));
            }
        }
        if (startupCommandLines != null) {
            for (INDICommandLine indiCommandLine : startupCommandLines) {
                indiCommandLine.addLibraries();
            }
        }
    }
}
