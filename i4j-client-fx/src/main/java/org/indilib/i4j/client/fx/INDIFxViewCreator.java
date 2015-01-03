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

import javafx.scene.control.Tab;

import org.indilib.i4j.Constants.PropertyPermissions;
import org.indilib.i4j.INDIException;
import org.indilib.i4j.client.INDIBLOBElement;
import org.indilib.i4j.client.INDIBLOBProperty;
import org.indilib.i4j.client.INDIDevice;
import org.indilib.i4j.client.INDIDeviceListener;
import org.indilib.i4j.client.INDIElementListener;
import org.indilib.i4j.client.INDILightElement;
import org.indilib.i4j.client.INDILightProperty;
import org.indilib.i4j.client.INDINumberElement;
import org.indilib.i4j.client.INDINumberProperty;
import org.indilib.i4j.client.INDIPropertyListener;
import org.indilib.i4j.client.INDISwitchElement;
import org.indilib.i4j.client.INDISwitchProperty;
import org.indilib.i4j.client.INDITextElement;
import org.indilib.i4j.client.INDITextProperty;
import org.indilib.i4j.client.fx.INDIFxFactory.FxController;

public class INDIFxViewCreator implements org.indilib.i4j.client.INDIViewCreatorInterface {

    @Override
    public INDIDeviceListener createDeviceListener(INDIDevice indiDevice) throws INDIException {
        return null;
    }

    @Override
    public INDIElementListener createBlobElementView(INDIBLOBElement indiblobElement, PropertyPermissions permission) throws INDIException {
        FxController<Tab, INDIBLOBElement, INDIBlobElementController> newINDIFxNumberElement = INDIFxFactory.newINDIFxBlobElementFxml();
        return newINDIFxNumberElement.initializeFx(indiblobElement);
    }

    @Override
    public INDIElementListener createLightElementView(INDILightElement indiLightElement, PropertyPermissions permission) throws INDIException {
        FxController<Tab, INDILightElement, INDILightElementController> newINDIFxNumberElement = INDIFxFactory.newINDIFxLightElementFxml();
        return newINDIFxNumberElement.initializeFx(indiLightElement);
    }

    @Override
    public INDIElementListener createNumberElementView(INDINumberElement indiNumberElement, PropertyPermissions permission) throws INDIException {
        FxController<Tab, INDINumberElement, INDINumberElementController> newINDIFxNumberElement = INDIFxFactory.newINDIFxNumberElementFxml();
        return newINDIFxNumberElement.initializeFx(indiNumberElement);
    }

    @Override
    public INDIElementListener createSwitchElementView(INDISwitchElement indiSwitchElement, PropertyPermissions permission) throws INDIException {
        FxController<Tab, INDISwitchElement, INDISwitchElementController> newINDIFxSwitchElement = INDIFxFactory.newINDIFxSwitchElementFxml();
        return newINDIFxSwitchElement.initializeFx(indiSwitchElement);
    }

    @Override
    public INDIElementListener createTextElementView(INDITextElement indiTextElement, PropertyPermissions permission) throws INDIException {
        FxController<Tab, INDITextElement, INDITextElementController> newINDIFxTextElement = INDIFxFactory.newINDIFxTextElementFxml();
        return newINDIFxTextElement.initializeFx(indiTextElement);
    }

    @Override
    public INDIPropertyListener createBlobPropertyView(INDIBLOBProperty indiProperty) throws INDIException {
        FxController<Tab, INDIBLOBProperty, INDIBlobPropertyController> newFx = INDIFxFactory.newINDIFxBlobPropertyFxml();
        return newFx.initializeFx(indiProperty);
    }

    @Override
    public INDIPropertyListener createNumberPropertyView(INDINumberProperty indiProperty) throws INDIException {
        FxController<Tab, INDINumberProperty, INDINumberPropertyController> newINDIFxNumberProperty = INDIFxFactory.newINDIFxNumberPropertyFxml();
        return newINDIFxNumberProperty.initializeFx(indiProperty);
    }

    @Override
    public INDIPropertyListener createTextPropertyView(INDITextProperty indiProperty) throws INDIException {
        FxController<Tab, INDITextProperty, INDITextPropertyController> newINDIFxTextProperty = INDIFxFactory.newINDIFxTextPropertyFxml();
        return newINDIFxTextProperty.initializeFx(indiProperty);
    }

    @Override
    public INDIPropertyListener createSwitchPropertyView(INDISwitchProperty indiProperty) throws INDIException {
        FxController<Tab, INDISwitchProperty, INDISwitchPropertyController> newINDIFxSwitchProperty = INDIFxFactory.newINDIFxSwitchPropertyFxml();
        return newINDIFxSwitchProperty.initializeFx(indiProperty);
    }

    @Override
    public INDIPropertyListener createLightPropertyView(INDILightProperty indiProperty) throws INDIException {
        FxController<Tab, INDILightProperty, INDILightPropertyController> newFx = INDIFxFactory.newINDIFxLightPropertyFxml();
        return newFx.initializeFx(indiProperty);
    }

    @Override
    public INDIDeviceListener createDeviceView(INDIDevice indiDevice) throws INDIException {
        FxController<Tab, INDIDevice, INDIController<INDIDevice>> newINDIFxDevice = INDIFxFactory.newINDIFxDeviceFxml();
        return newINDIFxDevice.initializeFx(indiDevice);
    }

}
