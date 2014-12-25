package org.indilib.i4j.client.fx;

/*
 * #%L
 * INDI for Java Client UI Library
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

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import org.indilib.i4j.INDIException;
import org.indilib.i4j.client.INDIDevice;
import org.indilib.i4j.client.INDIServerConnection;
import org.indilib.i4j.client.INDIServerConnectionListener;
import org.indilib.i4j.client.fx.INDIFxFactory.FxController;
import org.indilib.i4j.protocol.api.INDIConnection;
import org.indilib.i4j.protocol.url.INDIURLStreamHandlerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class INDIClient extends Application implements INDIServerConnectionListener {

    private static final Logger LOG = LoggerFactory.getLogger(INDIClient.class);

    private INDIServerConnection connection;

    private FxController<TabPane, Object, INDIDevicesController> devices;

    @Override
    public void start(Stage stage) throws Exception {

        connection = new INDIServerConnection((INDIConnection) new URL(getParameters().getRaw().get(0)).openConnection());

        connection.addINDIServerConnectionListener(INDIFxPlatformThreadConnector.connect(this, INDIServerConnectionListener.class));

        try {
            connection.connect();
            connection.askForDevices();
        } catch (IOException e) {
            LOG.error("Problem connecting to " + connection.toString(), e);
        }

        devices = INDIFxFactory.newINDIFxDevices();

        Scene scene = new Scene(devices.fx, 800, 275);
        scene.getStylesheets().add(getClass().getResource("indistyle.css").toExternalForm());

        stage.setOnCloseRequest(new EventHandler<WindowEvent>() {

            @Override
            public void handle(WindowEvent event) {
                Platform.exit();
                System.exit(0);
            }
        });
        stage.setTitle("INDI Client");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        INDIURLStreamHandlerFactory.init();
        launch(args);
    }

    @Override
    public void newDevice(INDIServerConnection connection, INDIDevice device) {
        try {
            Tab tab = ((INDIFxAccess) device.getDefaultUIComponent()).getGui(Tab.class);
            device.blobsEnableAlso();
            devices.fx.getTabs().add(tab);
        } catch (Exception e) {
            throw new RuntimeException("?", e);
        }
    }

    @Override
    public void removeDevice(INDIServerConnection connection, INDIDevice device) {
        for (Tab tab : new ArrayList<>(devices.fx.getTabs())) {
            if (tab.getUserData() == device) {
                devices.fx.getTabs().remove(tab);
            }
        }
    }

    @Override
    public void connectionLost(INDIServerConnection connection) {
        // TODO
    }

    @Override
    public void newMessage(INDIServerConnection connection, Date timestamp, String message) {
        // TODO
    }

}
