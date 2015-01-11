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

import org.controlsfx.dialog.Dialogs;
import org.indilib.i4j.client.INDIDevice;
import org.indilib.i4j.client.INDIServerConnection;
import org.indilib.i4j.client.INDIServerConnectionListener;
import org.indilib.i4j.client.fx.INDIFxFactory.FxController;
import org.indilib.i4j.protocol.api.INDIConnection;
import org.indilib.i4j.protocol.url.INDIURLStreamHandlerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * standard fx client interface for the indi protocal, for more sofisticated
 * guis please replace this class. Do not forget to create a css for your gui.
 * 
 * @author Richard van Nieuwenhoven
 */
public class INDIClient extends Application implements INDIServerConnectionListener {

    /**
     * default heigth and width of the main window.
     */
    private static final int DEFAULT_WINDOW_SIZE = 800;

    /**
     * A logger for the errors.
     */
    private static final Logger LOG = LoggerFactory.getLogger(INDIClient.class);

    /**
     * the indi server connection.
     */
    private INDIServerConnection connection;

    /**
     * the currently controlled devices.
     */
    private FxController<TabPane, Object, INDIDevicesController> devices;

    /**
     * the main stage of the application.
     */
    private Stage mainStage;

    @Override
    public void start(Stage stage) throws Exception {
        this.mainStage = stage;

        connection = new INDIServerConnection((INDIConnection) new URL(getParameters().getRaw().get(0)).openConnection());

        connection.addINDIServerConnectionListener(INDIFxPlatformThreadConnector.connect(this, INDIServerConnectionListener.class));

        try {
            connection.connect();
            connection.askForDevices();
        } catch (IOException e) {
            LOG.error("Problem connecting to " + connection.toString(), e);
        }

        devices = INDIFxFactory.newINDIFxDevicesFxml();

        Scene scene = new Scene(devices.fx(), DEFAULT_WINDOW_SIZE, DEFAULT_WINDOW_SIZE);
        // START SNIPPET: style
        scene.getStylesheets().add(getClass().getResource("indistyle.css").toExternalForm());
        // END SNIPPET: style

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

    /**
     * main program start, redirected to javafx.
     * 
     * @param args
     *            the program arguments.
     */
    public static void main(String[] args) {
        INDIURLStreamHandlerFactory.init();
        launch(args);
    }

    @Override
    public void newDevice(INDIServerConnection serverConnection, INDIDevice device) {
        try {
            Tab tab = ((INDIFxAccess) device.getDefaultUIComponent()).getGui(Tab.class);
            device.blobsEnableAlso();
            devices.fx().getTabs().add(tab);
        } catch (Exception e) {
            throw new RuntimeException("?", e);
        }
    }

    @Override
    public void removeDevice(INDIServerConnection serverConnection, INDIDevice device) {
        for (Tab tab : new ArrayList<>(devices.fx().getTabs())) {
            INDIDeviceController controller = INDIFxFactory.controller(tab);
            if (controller != null && controller.indi == device) {
                devices.fx().getTabs().remove(tab);
            }
        }
    }

    @Override
    public void connectionLost(INDIServerConnection serverConnection) {
        Dialogs.create()//
                .owner(mainStage)//
                .title("Connection to server lost")//
                .message("The connection to the server was servered, application will now close.")//
                .showWarning();
        mainStage.close();
    }

    @Override
    public void newMessage(INDIServerConnection serverConnection, Date timestamp, String message) {
    }

}
