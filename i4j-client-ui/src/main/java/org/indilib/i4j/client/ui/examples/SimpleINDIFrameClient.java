/*
 * This file is part of INDI for Java Client UI.
 * 
 * INDI for Java Client UI is free software: you can redistribute it
 * and/or modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version.
 * 
 * INDI for Java Client UI is distributed in the hope that it will be
 * useful, but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with INDI for Java Client UI. If not, see
 * <http://www.gnu.org/licenses/>.
 */
package org.indilib.i4j.client.ui.examples;

/*
 * #%L
 * INDI for Java Client UI Library
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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program. If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */

import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.IOException;
import java.net.URL;
import java.util.Date;

import org.indilib.i4j.Constants;
import org.indilib.i4j.client.INDIDevice;
import org.indilib.i4j.client.INDIServerConnection;
import org.indilib.i4j.client.INDIServerConnectionListener;
import org.indilib.i4j.client.ui.INDIDevicePanel;
import org.indilib.i4j.protocol.api.INDIConnection;
import org.indilib.i4j.protocol.url.INDIURLStreamHandlerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A simple GUI INDI Client using the library.
 * 
 * @author S. Alonso (Zerjillo) [zerjioi at ugr.es]
 */
public class SimpleINDIFrameClient extends javax.swing.JFrame implements INDIServerConnectionListener {

    private static final Logger LOG = LoggerFactory.getLogger(SimpleINDIFrameClient.class);

    INDIServerConnection connection;

    /**
     * Creates new form SimpleINDIFrameClient
     * 
     * @param host
     *            host to connect to
     * @param port
     *            port to connect to
     */
    public SimpleINDIFrameClient(String host, int port) {
        initComponents();

        int width = 800;
        int height = 600;
        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        setBounds((dim.width - width) / 2, (dim.height - height) / 2, width, height);

        connection = new INDIServerConnection(host, port);
    }

    public SimpleINDIFrameClient(INDIConnection connection) {
        initComponents();

        int width = 800;
        int height = 600;
        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        setBounds((dim.width - width) / 2, (dim.height - height) / 2, width, height);
        this.connection = new INDIServerConnection(connection);
    }

    public void connect() {
        connection.addINDIServerConnectionListener(this);

        try {
            connection.connect();
            connection.askForDevices();
        } catch (IOException e) {
            LOG.error("Problem connecting to " + connection.toString(), e);
        }
    }

    @Override
    public void newDevice(INDIServerConnection connection, INDIDevice device) {
        INDIDevicePanel p = null;

        try {
            p = (INDIDevicePanel) device.getDefaultUIComponent();
        } catch (Exception e) { //
            LOG.error("Problem with library. Should not happen unless errors in Client library", e);
            System.exit(-1);
        }

        tabs.addTab(device.getName(), p);
    }

    @Override
    public void removeDevice(INDIServerConnection connection, INDIDevice device) {
        INDIDevicePanel p = null;

        try {
            p = (INDIDevicePanel) device.getDefaultUIComponent();
        } catch (Exception e) {
            LOG.error("Problem with library. Should not happen unless errors in Client library", e);
            System.exit(-1);
        }

        tabs.remove(p);
    }

    @Override
    public void connectionLost(INDIServerConnection connection) {
        LOG.info("Connection lost. Exiting.");
        System.exit(0);
    }

    @Override
    public void newMessage(INDIServerConnection connection, Date timestamp, String message) {
        LOG.info(message);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed"
    // desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        tabs = new javax.swing.JTabbedPane();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Simple INDI Frame Client");
        getContentPane().add(tabs, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /**
     * @param args
     *            the command line arguments
     */
    public static void main(String args[]) {
        INDIURLStreamHandlerFactory.init();

        if (args.length == 1) {
            try {
                INDIConnection connection = (INDIConnection) new URL(args[0]).openConnection();
                SimpleINDIFrameClient f = new SimpleINDIFrameClient(connection);
                f.setVisible(true);
                f.connect();
                return;
            } catch (Exception e) {
                LOG.error(args[0] + " was no url, trying host and port");
                return;
                // ok no url lets try it the old fascion way
            }
        }

        if ((args.length < 1) || (args.length > 2)) {
            printErrorMessageAndExit();
        }

        String host = args[0];
        int port = Constants.INDI_DEFAULT_PORT;

        if (args.length > 1) {
            try {
                port = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                printErrorMessageAndExit();
            }
        }

        SimpleINDIFrameClient f = new SimpleINDIFrameClient(host, port);
        f.setVisible(true);
        f.connect();
    }

    private static void printErrorMessageAndExit() {
        System.out.println("The program must be called in the following way:");

        System.out.println("> java SimpleINDIFormClient host [port]\n  where");
        System.out.println("    host - is the INDI Server to connect to");
        System.out.println("    port - is the INDI Server port. If not present the default port (" + Constants.INDI_DEFAULT_PORT + ") will be used.\n");

        System.exit(-1);
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTabbedPane tabs;
    // End of variables declaration//GEN-END:variables
}
