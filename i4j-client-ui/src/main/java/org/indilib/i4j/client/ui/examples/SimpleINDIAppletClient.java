/*
 *  This file is part of INDI for Java Client UI.
 * 
 *  INDI for Java Client UI is free software: you can redistribute it
 *  and/or modify it under the terms of the GNU General Public License 
 *  as published by the Free Software Foundation, either version 3 of 
 *  the License, or (at your option) any later version.
 * 
 *  INDI for Java Client UI is distributed in the hope that it will be
 *  useful, but WITHOUT ANY WARRANTY; without even the implied warranty
 *  of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 * 
 *  You should have received a copy of the GNU General Public License
 *  along with INDI for Java Client UI.  If not, see 
 *  <http://www.gnu.org/licenses/>.
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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */

import java.io.IOException;
import java.util.Date;

import javax.swing.JOptionPane;

import org.indilib.i4j.Constants;
import org.indilib.i4j.client.INDIDevice;
import org.indilib.i4j.client.INDIServerConnection;
import org.indilib.i4j.client.INDIServerConnectionListener;
import org.indilib.i4j.client.ui.INDIDevicePanel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A simple GUI INDI Client using the library.
 * 
 * @author S. Alonso (Zerjillo) [zerjioi at ugr.es]
 * @version 1.21, April 4, 2012
 */
public class SimpleINDIAppletClient extends javax.swing.JApplet implements INDIServerConnectionListener {

    private static final Logger LOG = LoggerFactory.getLogger(SimpleINDIAppletClient.class);

    private INDIServerConnection connection;

    public void connect() {
        String host = "";
        int port = Constants.INDI_DEFAULT_PORT;

        String s = null;

        while (s == null) {
            Object[] possibilities = null;
            s = (String) JOptionPane.showInputDialog(null, "Enter host:port to connect:", "INDI Connection Dialog", JOptionPane.PLAIN_MESSAGE, null, possibilities, null);

            if (s == null) {
                stop(); // If canel was pressed stop
            }

            if (s.length() > 0) {
                int pos = s.indexOf(":");

                if (pos == -1) {
                    s = null;
                } else {
                    host = s.substring(0, pos);
                    try {
                        port = Integer.parseInt(s.substring(pos + 1));
                    } catch (NumberFormatException e) {
                        s = null;
                    }
                }
            }
        }

        connection = new INDIServerConnection(host, port);

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
        } catch (Exception e) {
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
        addMessage("Connection lost.");
    }

    @Override
    public void newMessage(INDIServerConnection connection, Date timestamp, String message) {
        addMessage(timestamp + " - " + message);
    }

    /**
     * Not efficient at all. Will certainly produce problems when many mesages
     * are printed.
     * 
     * @param msg
     */
    private void addMessage(String msg) {
        messageArea.setText(messageArea.getText() + "\n" + msg);
    }

    /**
     * Initializes the applet SimpleINDIAppletClient
     */
    @Override
    public void init() {

        /*
         * Create and display the applet
         */
        try {
            java.awt.EventQueue.invokeAndWait(new Runnable() {

                @Override
                public void run() {
                    initComponents();
                    connect();
                }
            });
        } catch (Exception ex) {
            LOG.error("Problem with library. Should not happen unless errors in Client library", ex);
        }
    }

    /**
     * This method is called from within the init() method to initialize the
     * form. WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed"
    // desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        tabs = new javax.swing.JTabbedPane();
        jPanel1 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        messageArea = new javax.swing.JTextArea();

        getContentPane().add(tabs, java.awt.BorderLayout.CENTER);

        jPanel1.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5));
        jPanel1.setLayout(new java.awt.BorderLayout());

        messageArea.setColumns(20);
        messageArea.setRows(5);
        jScrollPane1.setViewportView(messageArea);

        jPanel1.add(jScrollPane1, java.awt.BorderLayout.CENTER);

        getContentPane().add(jPanel1, java.awt.BorderLayout.SOUTH);
    }// </editor-fold>//GEN-END:initComponents
     // Variables declaration - do not modify//GEN-BEGIN:variables

    private javax.swing.JPanel jPanel1;

    private javax.swing.JScrollPane jScrollPane1;

    private javax.swing.JTextArea messageArea;

    private javax.swing.JTabbedPane tabs;
    // End of variables declaration//GEN-END:variables
}
