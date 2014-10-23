package org.indilib.i4j.server.examples;

/*
 * #%L
 * INDI for Java Server examples
 * %%
 * Copyright (C) 2013 - 2014 indiforjava
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
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.indilib.i4j.Constants;
import org.indilib.i4j.INDIException;
import org.indilib.i4j.driver.INDIDriver;
import org.indilib.i4j.server.DefaultINDIServer;
import org.indilib.i4j.server.INDIClient;
import org.indilib.i4j.server.INDIDevice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A simple INDI Server that basically sends all messages from drivers and
 * clients and viceversa, just performing basic checks of messages integrity. It
 * allows to dinamically load / unload different kinds of Devices with simple
 * shell commands.
 * 
 * @author S. Alonso (Zerjillo) [zerjioi at ugr.es]
 * @version 1.32, July 23, 2013
 */
public class INDIBasicServer extends DefaultINDIServer {

    /**
     * poll interfall.
     */
    private static final int LOAD_POLL_INTERFALL = 500;

    /**
     * Logger to log to.
     */
    private static final Logger LOG = LoggerFactory.getLogger(INDIBasicServer.class);

    /**
     * The only server.
     */
    private static INDIBasicServer server;

    /**
     * Loaded jar files.
     */
    private final List<String> jarFiles = new ArrayList<String>();

    /**
     * Constructs the server.
     */
    public INDIBasicServer() {
        super();
    }

    /**
     * Constructs the server with a particular port.
     * 
     * @param port
     *            The port to which the server will listen.
     */
    public INDIBasicServer(int port) {
        super(port);
    }

    /**
     * Loads the Java Drivers in a JAR file.
     * 
     * @param jar
     *            The JAR file
     * @see #unloadJava
     */
    public void loadJava(String jar) {
        try {
            loadJavaDriversFromJAR(jar);

            jarFiles.add(jar);
        } catch (INDIException e) {
            System.err.println(e.getMessage());
        }
    }

    /**
     * Loads the Java Drivers in Form the current classpath.
     * 
     * @param className
     *            class name of the driver
     */
    public void loadJavaClass(String className) {
        try {
            Class<?> cls = Thread.currentThread().getContextClassLoader().loadClass(className);
            if (INDIDriver.class.isAssignableFrom(cls)) {
                loadJavaDriver(cls);
            }
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }

    /**
     * Unloads the Java Drivers in a JAR file.
     * 
     * @param jar
     *            The JAR file.
     * @see #loadJava
     */
    public void unloadJava(String jar) {
        destroyJavaDriversFromJAR(jar);

        jarFiles.remove(jar);
    }

    /**
     * reload the drivers in the jar.
     * 
     * @param jar
     *            the jar to reload.
     */
    private void reloadJava(String jar) {
        server.unloadJava(jar);

        try {
            Thread.sleep(LOAD_POLL_INTERFALL);
        } catch (InterruptedException e) {
            LOG.warn("sleep interupted", e);
        }

        while (server.isAlreadyLoaded(jar)) {
            System.err.println("Waiting for " + jar + " to unload");
            try {
                Thread.sleep(LOAD_POLL_INTERFALL);
            } catch (InterruptedException e) {
                LOG.warn("sleep interupted", e);
            }
        }

        server.loadJava(jar);
    }

    /**
     * Loads a Native Driver.
     * 
     * @param path
     *            The path of the Driver.
     * @see #unloadNative
     */
    public void loadNative(String path) {
        try {
            server.loadNativeDriver(path);
        } catch (INDIException e) {
            System.err.println(e.getMessage());
        }
    }

    /**
     * Unloads a Native Driver.
     * 
     * @param path
     *            The path of the Driver. see #loadNative
     */
    public void unloadNative(String path) {
        destroyNativeDriver(path);
    }

    /**
     * Connects to a Network Driver (another Server).
     * 
     * @param host
     *            The host of the other Server.
     * @param port
     *            The port of the other Server.
     * @see #disconnect
     */
    public void connect(String host, int port) {
        try {
            loadNetworkDriver(host, port);
        } catch (INDIException e) {
            System.err.println(e.getMessage());
        }
    }

    /**
     * Disconnects from a Network Driver (another Server).
     * 
     * @param host
     *            The host of the other Server.
     * @param port
     *            The port of the other Server.
     * @see #connect
     */
    public void disconnect(String host, int port) {
        destroyNetworkDriver(host, port);
    }

    /**
     * Prints a list of the loaded devices to the Rrror stream.
     */
    public void listDevices() {
        List<INDIDevice> devs = getDevices();

        System.err.println("Number of loaded Drivers: " + devs.size());

        for (int i = 0; i < devs.size(); i++) {
            System.err.println("  - " + devs.get(i));
        }

        System.err.println("");
    }

    /**
     * Main logic of the program: creates the server, parses the arguments and
     * attends shell commands from the standard input.
     * 
     * @param args
     *            The arguments of the program.
     */
    public static void main(String[] args) {
        System.err.println("INDI for Java Basic Server initializing...");

        int port = Constants.INDI_DEFAULT_PORT;

        for (int i = 0; i < args.length; i++) {
            String[] s = splitArgument(args[i]);

            if (s[0].equals("-p")) {
                try {
                    port = Integer.parseInt(s[1]);
                } catch (NumberFormatException e) {
                    System.err.println("Incorrect port number");
                    printArgumentHelp();
                    System.exit(-1);
                }
            }
        }

        server = new INDIBasicServer(port);

        // Parse arguments
        for (int i = 0; i < args.length; i++) {
            boolean correct = parseArgument(args[i]);

            if (!correct) {
                System.err.println("Argument '" + args[i] + "' not correct. Use -help for help. Exiting.");
                System.exit(-1);
            }
        }

        System.err.println("Type 'help' for help.");

        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        String line;

        try {
            while (true) {
                line = in.readLine();
                line = line.trim();

                if (line.length() > 0) {
                    parseInputLine(line);
                }
            }
        } catch (IOException e) {
            LOG.error("io exception", e);
        }
    }

    /**
     * Parses a single argument of the application. Possible arguments are:
     * <code>-help</code>, <code>-add jarFile</code>,
     * <code>-addn driverPath</code> and <code>-connect host[:port]</code>.
     * 
     * @param arg
     *            The argument to be parsed.
     * @return <code>true</code> if it has been correctly parsed.
     *         <code>false</code> otherwise.
     */
    private static boolean parseArgument(String arg) {
        String[] s = splitArgument(arg);

        if (s[0].equals("-help")) {
            printArgumentHelp();
            return true;
        } else if (s[0].equals("-add")) {
            server.loadJava(s[1]);

            return true;
        } else if (s[0].equals("-addc")) {
            server.loadJavaClass(s[1]);

            return true;
        } else if (s[0].equals("-addn")) {
            server.loadNative(s[1]);

            return true;
        } else if (s[0].equals("-connect")) {
            int port = Constants.INDI_DEFAULT_PORT;
            String host;

            int pos = s[1].indexOf(":");

            if (pos == -1) {
                host = s[1];
            } else {
                host = s[1].substring(0, pos - 1);

                String p = s[1].substring(pos + 1);

                try {
                    port = Integer.parseInt(p);
                } catch (NumberFormatException e) {
                    return false;
                }
            }

            server.connect(host, port);

            return true;
        } else if (s[0].equals("-p")) {
            return true;
        }

        return false;
    }

    /**
     * Prints some help about the possible arguments of the program to the error
     * stream.
     */
    private static void printArgumentHelp() {
        System.err.println("\nThe following arguments can be used:");
        System.err.println("  -help                Shows this help.");
        System.err.println("  -p=port              Port to which the Server will listen.");
        System.err.println("  -add=jarFile         Loads all INDIDrivers in the jarFile.");
        System.err.println("  -addn=driverPath     Loads the native driver described by driverPath.");
        System.err.println("  -connect=host[:port] Loads the drivers in a remote INDI server.\n");
    }

    /**
     * Prints some help about the possible commands of the program to the error
     * stream.
     */
    private static void printCommandHelp() {
        System.err.println("\nThe following commands can be used:");
        System.err.println("  help                    Shows this help.");
        System.err.println("  list                    Lists all loaded drivers.");
        System.err.println("  add jarFile             Loads all INDIDrivers in the jarFile.");
        System.err.println("  addc class              Loads the INDIDriver specified by the class.");
        System.err.println("  remove jarFile          Removes all INDIDrivers in the jarFile");
        System.err.println("  reload jarFile          Reloads all INDIDrivers in the jarFile");
        System.err.println("  r                       Reloads all INDIDrivers in jarFiles (useful for testing)");
        System.err.println("  addn driverPath         Loads the native driver described by driverPath");
        System.err.println("  removen driverPath      Removes the native driver described by driverPath");
        System.err.println("  reloadn driverPath      Reloads the native driver described by driverPath");
        System.err.println("  connect host[:port]     Loads the drivers in a remote INDI server.");
        System.err.println("  disconnect host[:port]  Removes the drivers in a remote INDI server.");
        System.err.println("  stop                    Stops the Server and breaks all Client connections.");
        System.err.println("  start                   Starts the Server (if it was previously not).\n");
    }

    /**
     * Splits an argument in two. Separator char is =
     * 
     * @param arg
     *            The argument to split.
     * @return An array of the two components of the parameter.
     */
    private static String[] splitArgument(String arg) {
        int pos = arg.indexOf("=");

        if (pos != -1) {
            return new String[]{
                arg.substring(0, pos),
                arg.substring(pos + 1)
            };
        } else {
            return new String[]{
                arg,
                ""
            };
        }
    }

    /**
     * Parses an input command. Possible commands are <code>help</code>,
     * <code>list</code>, <code>add jarFile</code>, <code>remove jarFile</code>,
     * <code>reload jarFile</code>, <code>r</code>, <code>addn driverPath</code>
     * , <code>addc driverClass</code>, <code>removen driverPath</code>,
     * <code>reloadn driverPath</code>, <code>connect host[:port]</code>,
     * <code>disconnect host[:port]</code>, <code>stop</code> and
     * <code>start</code>.
     * 
     * @param line
     *            The line to be parsed.
     */
    private static void parseInputLine(String line) {
        String[] args = line.trim().split("\\s+");

        if (args.length < 1) {
            return;
        }

        if (args[0].equals("help")) {
            printCommandHelp();

            return;
        } else if (args[0].equals("list")) {
            server.listDevices();

            return;
        } else if (args[0].equals("stop")) {
            server.stopServer();

            return;
        } else if (args[0].equals("start")) {
            if (server.isServerRunning()) {
                System.err.println("Server already started");
            } else {
                server.startListeningToClients();
            }

            return;
        } else if (args[0].equals("r")) {
            List<String> copy = new ArrayList<>(server.jarFiles);

            for (int i = 0; i < copy.size(); i++) {
                String jar = copy.get(i);

                server.reloadJava(jar);
            }

            return;
        }

        if (args.length < 2) {
            System.err.println("Command error. 'help' for help.\n");

            return;
        }

        if (args[0].equals("add")) {
            String f = args[1];

            server.loadJava(f);

            return;
        } else if (args[0].equals("remove")) {
            String f = args[1];

            server.unloadJava(f);

            return;
        } else if (args[0].equals("reload")) {
            String f = args[1];

            server.reloadJava(f);

            return;
        } else if ((args[0].equals("addN")) || (args[0].equals("addn"))) {
            String f = args[1];

            server.loadNative(f);

            return;
        } else if ((args[0].equals("addC")) || (args[0].equals("addc"))) {
            String className = args[1];

            server.loadJavaClass(className);

            return;
        } else if ((args[0].equals("removeN")) || (args[0].equals("removen"))) {
            String f = args[1];

            server.unloadNative(f);

            return;
        } else if ((args[0].equals("reloadN")) || (args[0].equals("reloadn"))) {
            String f = args[1];

            server.unloadNative(f);

            try {
                Thread.sleep(LOAD_POLL_INTERFALL);
            } catch (InterruptedException e) {
                LOG.warn("sleep interupted", e);
            }

            while (server.isAlreadyLoaded(f)) {
                System.err.println("Waiting for " + f + " to unload");
                try {
                    Thread.sleep(LOAD_POLL_INTERFALL);
                } catch (InterruptedException e) {
                    LOG.warn("sleep interupted", e);
                }
            }

            server.loadNative(f);

            return;
        } else if ((args[0].equals("connect")) || (args[0].equals("disconnect"))) {
            String host = args[1];
            int port = Constants.INDI_DEFAULT_PORT;

            if (args.length > 2) {
                String p = args[2];

                try {
                    port = Integer.parseInt(p);
                } catch (NumberFormatException e) {
                    System.err.println("Port not valid.");
                    return;
                }
            }

            if (args[0].equals("connect")) {
                server.connect(host, port);
            } else if (args[0].equals("disconnect")) {
                server.disconnect(host, port);
            }

            return;
        }

        System.err.println("Command error. 'help' for help.\n");
    }

    /**
     * Prints a message about the broken connection to the standard err.
     * 
     * @param client
     *            The Client whose connection has been broken
     */
    @Override
    protected void connectionWithClientBroken(INDIClient client) {
        System.err.println("Connection with client " + client.getInetAddress() + " has been broken.");
    }

    /**
     * Prints a message about the established connection to the standard err.
     * 
     * @param client
     *            The Client whose connection has been established
     */
    @Override
    protected void connectionWithClientEstablished(INDIClient client) {
        System.err.println("Connection with client " + client.getInetAddress() + " established.");
    }

    /**
     * Prints a message about the driver which has been disconnected.
     * 
     * @param driverIdentifier
     *            driver identifier.
     * @param deviceNames
     *            the names of the devices.
     */
    @Override
    protected void driverDisconnected(String driverIdentifier, String[] deviceNames) {
        String names = "[";

        for (int i = 0; i < deviceNames.length; i++) {
            if (i != 0) {
                names += ", ";
            }

            names += deviceNames[i];
        }

        names += "]";

        System.err.println("Driver " + driverIdentifier + " has been disconnected. The following devices have dissapeared: " + names);
    }
}
