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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program. If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */

import java.io.IOException;

import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Tab;

import org.indilib.i4j.Constants.LightStates;
import org.indilib.i4j.Constants.PropertyStates;
import org.indilib.i4j.Constants.SwitchStatus;
import org.indilib.i4j.client.INDIDeviceListener;
import org.indilib.i4j.client.INDIElementListener;
import org.indilib.i4j.client.INDIPropertyListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This factory reads the corresponding fxml resources for the indi elements,
 * and connects the controller to the gui and the indi element.
 * 
 * @author Richard van Nieuwenhoven
 */
public final class INDIFxFactory {

    /**
     * utility class, must not be instanciated.
     */
    private INDIFxFactory() {
    }

    /**
     * property in the fx gui to store the controller.
     */
    private static final String FX_CONTROLLER_PROPERTY = "controller";

    /**
     * controller wrapper, to connect the model/gui and controllers.
     * 
     * @author Richard van Nieuwenhoven
     * @param <FX>
     *            the gui
     * @param <INDIType>
     *            the indi model
     * @param <CONTROLLER>
     *            the controller.
     */
    public static class FxController<FX, INDIType, CONTROLLER extends INDIController<INDIType>> {

        /**
         * constuctor for the controller wrapper.
         * 
         * @param fx
         *            the gui.
         * @param controller
         *            the controller.
         */
        public FxController(FX fx, CONTROLLER controller) {
            this.fx = fx;
            this.controller = controller;
        }

        /**
         * the gui.
         */
        private final FX fx;

        /**
         * the controller.
         */
        private final CONTROLLER controller;

        /**
         * @return the controller of this wrapper.
         */
        public CONTROLLER controller() {
            return controller;
        }

        /**
         * @return the fx gui of this wrapper.
         */
        public FX fx() {
            return fx;
        }

        /**
         * initialize the indi model into the wrapper by connecting it to the
         * gui and the controller.
         * 
         * @param indiObject
         *            the indi model
         * @return the proxy that redirects all calls to the gui thread.
         * @param <T>
         *            the expected return type (attention! runtime exception if
         *            you save it wrongly!).
         */
        protected <T> T initializeFx(INDIType indiObject) {
            if (fx instanceof Node) {
                ((Node) fx).getProperties().put(FX_CONTROLLER_PROPERTY, controller);
            } else if (fx instanceof Tab) {
                ((Tab) fx).getProperties().put(FX_CONTROLLER_PROPERTY, controller);
            }
            controller.setIndi(indiObject);
            return (T) INDIFxPlatformThreadConnector.connect(controller, INDIDeviceListener.class, INDIElementListener.class, INDIPropertyListener.class, INDIFxAccess.class);
        }

    }

    /**
     * the logger to use.
     */
    private static final Logger LOG = LoggerFactory.getLogger(INDIFxFactory.class);

    /**
     * resource pointer to the fxml resource.
     */
    private static final String INDI_FX_BLOB_ELEMENT = "INDIFxBlobElement.fxml";

    /**
     * resource pointer to the fxml resource.
     */
    private static final String INDI_FX_BLOB_PROPERTY = "INDIFxBlobProperty.fxml";

    /**
     * resource pointer to the fxml resource.
     */
    private static final String INDI_FX_DEVICE = "INDIFxDevice.fxml";

    /**
     * resource pointer to the fxml resource.
     */
    private static final String INDI_FX_DEVICES = "INDIFxDevices.fxml";

    /**
     * resource pointer to the fxml resource.
     */
    private static final String INDI_FX_GROUP = "INDIFxGroup.fxml";

    /**
     * resource pointer to the fxml resource.
     */
    private static final String INDI_FX_NUMBER_ELEMENT = "INDIFxNumberElement.fxml";

    /**
     * resource pointer to the fxml resource.
     */
    private static final String INDI_FX_NUMBER_PROPERTY = "INDIFxNumberProperty.fxml";

    /**
     * resource pointer to the fxml resource.
     */
    private static final String INDI_FX_SWITCH_ELEMENT = "INDIFxSwitchElement.fxml";

    /**
     * resource pointer to the fxml resource.
     */
    private static final String INDI_FX_SWITCH_PROPERTY = "INDIFxSwitchProperty.fxml";

    /**
     * resource pointer to the fxml resource.
     */
    private static final String INDI_FX_LIGHT_ELEMENT = "INDIFxLightElement.fxml";

    /**
     * resource pointer to the fxml resource.
     */
    private static final String INDI_FX_LIGHT_PROPERTY = "INDIFxLightProperty.fxml";

    /**
     * resource pointer to the fxml resource.
     */
    private static final String INDI_FX_TEXT_ELEMENT = "INDIFxTextElement.fxml";

    /**
     * resource pointer to the fxml resource.
     */
    private static final String INDI_FX_TEXT_PROPERTY = "INDIFxTextProperty.fxml";

    /**
     * style used to represent a idle light.
     */
    public static final String STYLE_STATE_IDLE = "idleLight";

    /**
     * style used to represent a alert light.
     */
    public static final String STYLE_STATE_ALERT = "alertLight";

    /**
     * style used to represent a buzy light.
     */
    public static final String STYLE_STATE_BUSY = "busyLight";

    /**
     * style used to represent a ok light.
     */
    public static final String STYLE_STATE_OK = "okLight";

    /**
     * style used to represent a idle switch state.
     */
    public static final String STYLE_SWITCH_STATE_IDLE = "icIdle";

    /**
     * style used to represent a ok switch state.
     */
    public static final String STYLE_SWITCH_STATE_OK = "icOk";

    /**
     * all possible state styles.
     */
    public static final String[] STYLE_STATES = {
        STYLE_STATE_IDLE,
        STYLE_STATE_ALERT,
        STYLE_STATE_BUSY,
        STYLE_STATE_OK
    };

    /**
     * all possible light styles.
     */
    public static final String[] LIGHT_STATES = {
        STYLE_STATE_IDLE,
        STYLE_STATE_ALERT,
        STYLE_STATE_BUSY,
        STYLE_STATE_OK
    };

    /**
     * all possible switch styles.
     */
    public static final String[] STYLE_SWITCH_STATES = {
        STYLE_SWITCH_STATE_IDLE,
        STYLE_SWITCH_STATE_OK
    };

    /**
     * @param <T>
     *            the expected return type (attention! runtime exception if you
     *            save it wrongly!).
     * @return create a new Gui/Controller pair for a blob element.
     */
    public static <T> T newINDIFxBlobElementFxml() {
        return load(INDI_FX_BLOB_ELEMENT);
    }

    /**
     * @param <T>
     *            the expected return type (attention! runtime exception if you
     *            save it wrongly!).
     * @return create a new Gui/Controller pair for a blob property.
     */
    public static <T> T newINDIFxBlobPropertyFxml() {
        return load(INDI_FX_BLOB_PROPERTY);
    }

    /**
     * @param <T>
     *            the expected return type (attention! runtime exception if you
     *            save it wrongly!).
     * @return create a new Gui/Controller pair for a devices list.
     */
    public static <T> T newINDIFxDevicesFxml() {
        return load(INDI_FX_DEVICES);
    }

    /**
     * @param <T>
     *            the expected return type (attention! runtime exception if you
     *            save it wrongly!).
     * @return create a new Gui/Controller pair for a device.
     */
    public static <T> T newINDIFxDeviceFxml() {
        return load(INDI_FX_DEVICE);
    }

    /**
     * @param <T>
     *            the expected return type (attention! runtime exception if you
     *            save it wrongly!).
     * @return create a new Gui/Controller pair for a group.
     */
    public static <T> T newINDIFxGroupFxml() {
        return load(INDI_FX_GROUP);
    }

    /**
     * @param <T>
     *            the expected return type (attention! runtime exception if you
     *            save it wrongly!).
     * @return create a new Gui/Controller pair for a number element.
     */
    public static <T> T newINDIFxNumberElementFxml() {
        return load(INDI_FX_NUMBER_ELEMENT);
    }

    /**
     * @param <T>
     *            the expected return type (attention! runtime exception if you
     *            save it wrongly!).
     * @return create a new Gui/Controller pair for a number property.
     */
    public static <T> T newINDIFxNumberPropertyFxml() {
        return load(INDI_FX_NUMBER_PROPERTY);
    }

    /**
     * @param <T>
     *            the expected return type (attention! runtime exception if you
     *            save it wrongly!).
     * @return create a new Gui/Controller pair for a switch element.
     */
    public static <T> T newINDIFxSwitchElementFxml() {
        return load(INDI_FX_SWITCH_ELEMENT);
    }

    /**
     * @param <T>
     *            the expected return type (attention! runtime exception if you
     *            save it wrongly!).
     * @return create a new Gui/Controller pair for a switch property.
     */
    public static <T> T newINDIFxSwitchPropertyFxml() {
        return load(INDI_FX_SWITCH_PROPERTY);
    }

    /**
     * @param <T>
     *            the expected return type (attention! runtime exception if you
     *            save it wrongly!).
     * @return create a new Gui/Controller pair for a light element.
     */
    public static <T> T newINDIFxLightElementFxml() {
        return load(INDI_FX_LIGHT_ELEMENT);
    }

    /**
     * @param <T>
     *            the expected return type (attention! runtime exception if you
     *            save it wrongly!).
     * @return create a new Gui/Controller pair for a light property.
     */
    public static <T> T newINDIFxLightPropertyFxml() {
        return load(INDI_FX_LIGHT_PROPERTY);
    }

    /**
     * @param <T>
     *            the expected return type (attention! runtime exception if you
     *            save it wrongly!).
     * @return create a new Gui/Controller pair for a text element.
     */
    public static <T> T newINDIFxTextElementFxml() {
        return load(INDI_FX_TEXT_ELEMENT);
    }

    /**
     * @param <T>
     *            the expected return type (attention! runtime exception if you
     *            save it wrongly!).
     * @return create a new Gui/Controller pair for a text property.
     */
    public static <T> T newINDIFxTextPropertyFxml() {
        return load(INDI_FX_TEXT_PROPERTY);
    }

    /**
     * load the fxml resource and connecte the gui with the controller. wrap it
     * in a gui thread redirector.
     * 
     * @param <T>
     *            the expected return type (attention! runtime exception if you
     *            save it wrongly!).
     * @param resource
     *            the resource to load
     * @return the wrapper
     */
    private static <T> T load(String resource) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader();
            final FxController fxc = new FxController<>(//
                    fxmlLoader.load(INDIFxFactory.class.getResource(resource).openStream()), //
                    fxmlLoader.getController());
            if (fxc.controller() == null) {
                throw new IllegalArgumentException("controller missing?");
            }
            return (T) fxc;
        } catch (IOException e) {
            LOG.error("could not load resource!", e);
            throw new IllegalArgumentException("resource missing?", e);
        }
    }

    /**
     * convert a indi state to a fx style name.
     * 
     * @param state
     *            the indi state.
     * @return the style name to use
     */
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

    /**
     * convert a indi switch status to a fx style name.
     * 
     * @param value
     *            indi switch status.
     * @return the style name to use
     */
    public static String switchStatusToStyle(SwitchStatus value) {
        switch (value) {
            case OFF:
                return STYLE_SWITCH_STATE_IDLE;
            case ON:
                return STYLE_SWITCH_STATE_OK;
            default:
                return STYLE_SWITCH_STATE_IDLE;
        }
    }

    /**
     * convert a light state to a fx style name.
     * 
     * @param value
     *            the indi light state
     * @return the style name to use
     */
    public static String lightStateToStyle(LightStates value) {
        switch (value) {
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

    /**
     * get the controller of a fx gui element.
     * 
     * @param <T>
     *            the expected return type (attention! runtime exception if you
     *            save it wrongly!).
     * @param tab
     *            the fx gui element
     * @return the controller.
     */
    public static <T> T controller(Tab tab) {
        return (T) tab.getProperties().get(FX_CONTROLLER_PROPERTY);
    }

    /**
     * get the controller of a fx gui element.
     * 
     * @param <T>
     *            the expected return type (attention! runtime exception if you
     *            save it wrongly!).
     * @param node
     *            the fx gui element
     * @return the controller.
     */
    public static <T> T controller(Node node) {
        return (T) node.getProperties().get(FX_CONTROLLER_PROPERTY);
    }

}
