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
            try {
                executeCommand(interactive, option);
            } catch (Exception e) {
                LOG.error("could not execute command deu to an exception", e);
                INDIBasicServer.print("error on command " + option + " error message was " + e.getMessage());
            }
        }
    }

    protected void executeCommand(boolean interactive, Option option) throws Exception {
        INDIServerInterface server = basicServer.getServer();
        if (option.equals(help)) {
            if (interactive) {
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
                if (interactive) {
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
                if (interactive) {
                    INDIBasicServer.print("server already interactive.");
                }
            } else if (option.equals(lib)) {
                String[] libs = commandLine.getOptionValues(option.getLongOpt());
                for (String libDirectory : libs) {
                    extendClasspath(new File(libDirectory));
                }
            } else if (option.equals(startup)) {
                if (interactive) {
                    INDIBasicServer.print("server already started, no startup possible anymore.");
                }
            } else {
                INDIBasicServer.print("unknown command: " + option);
            }
        }
    }

    protected String getArg(Option option) {
        return commandLine.getOptionValue(option.getLongOpt());
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

    /**
     * extend the classpath with one file or a directory with classes. If the
     * directory cotains jars add them all to the classpath else just the
     * directory.
     * 
     * @param dirOrJar
     *            the directory or jar file
     */
    protected static void extendClasspath(File dirOrJar) throws Exception {
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
}
