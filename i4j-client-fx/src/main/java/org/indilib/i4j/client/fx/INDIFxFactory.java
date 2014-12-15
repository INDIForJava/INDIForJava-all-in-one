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

import javafx.fxml.FXMLLoader;
import javafx.scene.control.Tab;

import org.indilib.i4j.Constants.PropertyStates;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class INDIFxFactory {

    public static class FxController<FX, CONTROLLER extends INDIController<?>> {

        FX fx;

        CONTROLLER controller;

        public CONTROLLER controller() {
            return controller;
        }
    }

    private static final Logger LOG = LoggerFactory.getLogger(INDIFxFactory.class);

    private static final String INDIFxBlobElement = "INDIFxBlobElement.fxml";

    private static final String INDIFxBlobProperty = "INDIFxBlobProperty.fxml";

    private static final String INDIFxDevice = "INDIFxDevice.fxml";

    private static final String INDIFxDevices = "INDIFxDevices.fxml";

    private static final String INDIFxGroup = "INDIFxGroup.fxml";

    private static final String INDIFxNumberElement = "INDIFxNumberElement.fxml";

    private static final String INDIFxNumberProperty = "INDIFxNumberProperty.fxml";

    private static final String INDIFxSwitchElement = "INDIFxSwitchElement.fxml";

    private static final String INDIFxSwitchProperty = "INDIFxSwitchProperty.fxml";

    private static final String INDIFxLightElement = "INDIFxLightElement.fxml";

    private static final String INDIFxLightProperty = "INDIFxLightProperty.fxml";

    private static final String INDIFxTextElement = "INDIFxTextElement.fxml";

    private static final String INDIFxTextProperty = "INDIFxTextProperty.fxml";

    public static final String STYLE_STATE_IDLE = "idleLight";

    public static final String STYLE_STATE_ALERT = "alertLight";

    public static final String STYLE_STATE_BUSY = "busyLight";

    public static final String STYLE_STATE_OK = "okLight";

    public static final String[] STYLE_STATES = {
        STYLE_STATE_IDLE,
        STYLE_STATE_ALERT,
        STYLE_STATE_BUSY,
        STYLE_STATE_OK
    };

    public static <T> T newINDIFxBlobElement() {
        return load(INDIFxBlobElement);
    }

    public static <T> T newINDIFxBlobProperty() {
        return load(INDIFxBlobProperty);
    }

    public static <T> T newINDIFxDevices() {
        return load(INDIFxDevices);
    }

    public static <T> T newINDIFxDevice() {
        return load(INDIFxDevice);
    }

    public static <T> T newINDIFxGroup() {
        return load(INDIFxGroup);
    }

    public static <T> T newINDIFxNumberElement() {
        return load(INDIFxNumberElement);
    }

    public static <T> T newINDIFxNumberProperty() {
        return load(INDIFxNumberProperty);
    }

    public static <T> T newINDIFxSwitchElement() {
        return load(INDIFxSwitchElement);
    }

    public static <T> T newINDIFxSwitchProperty() {
        return load(INDIFxSwitchProperty);
    }

    public static <T> T newINDIFxLightElement() {
        return load(INDIFxLightElement);
    }

    public static <T> T newINDIFxLightProperty() {
        return load(INDIFxLightProperty);
    }

    public static <T> T newINDIFxTextElement() {
        return load(INDIFxTextElement);
    }

    public static <T> T newINDIFxTextProperty() {
        return load(INDIFxTextProperty);
    }

    private static <T> T load(String resource) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader();
            final FxController fxc = new FxController<>();
            fxc.fx = fxmlLoader.load(INDIFxFactory.class.getResource(resource).openStream());
            fxc.controller = fxmlLoader.getController();
            if (fxc.controller == null) {
                throw new IllegalArgumentException("controller missing?");
            }
            return (T) fxc;
        } catch (IOException e) {
            LOG.error("could not load resource!", e);
            throw new IllegalArgumentException("resource missing?", e);
        }
    }

    public static String stateToStyle(PropertyStates state) {
        switch (state) {
            case IDLE:
                return STYLE_STATE_IDLE;
            case ALERT:
                return STYLE_STATE_ALERT;
            case BUSY:
                return STYLE_STATE_BUSY;
            case OK:
                return STYLE_STATE_OK;
            default:
                return STYLE_STATE_IDLE;
        }
    }
}
