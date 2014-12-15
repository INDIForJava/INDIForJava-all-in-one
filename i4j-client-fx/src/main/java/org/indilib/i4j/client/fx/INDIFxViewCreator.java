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
        return null;
    }

    @Override
    public INDIElementListener createLightElementView(INDILightElement indiLightElement, PropertyPermissions permission) throws INDIException {
        return null;
    }

    @Override
    public INDIElementListener createNumberElementView(INDINumberElement indiNumberElement, PropertyPermissions permission) throws INDIException {
        FxController<Tab, INDINumberElementController> newINDIFxNumberElement = INDIFxFactory.newINDIFxNumberElement();
        newINDIFxNumberElement.controller().setIndi(indiNumberElement);
        return INDIFxPlatformThreadConnector.connect(newINDIFxNumberElement.controller(), INDIElementListener.class, INDIFxAccess.class);
    }

    @Override
    public INDIElementListener createSwitchElementView(INDISwitchElement indiSwitchElement, PropertyPermissions permission) throws INDIException {
        FxController<Tab, INDISwitchElementController> newINDIFxSwitchElement = INDIFxFactory.newINDIFxSwitchElement();
        newINDIFxSwitchElement.controller().setIndi(indiSwitchElement);
        return INDIFxPlatformThreadConnector.connect(newINDIFxSwitchElement.controller(), INDIElementListener.class, INDIFxAccess.class);
    }

    @Override
    public INDIElementListener createTextElementView(INDITextElement indiTextElement, PropertyPermissions permission) throws INDIException {
        FxController<Tab, INDITextElementController> newINDIFxTextElement = INDIFxFactory.newINDIFxTextElement();
        newINDIFxTextElement.controller().setIndi(indiTextElement);
        return INDIFxPlatformThreadConnector.connect(newINDIFxTextElement.controller(), INDIElementListener.class, INDIFxAccess.class);
    }

    @Override
    public INDIPropertyListener createBlobPropertyView(INDIBLOBProperty indiProperty) throws INDIException {
        return null;
    }

    @Override
    public INDIPropertyListener createNumberPropertyView(INDINumberProperty indiProperty) throws INDIException {
        FxController<Tab, INDINumberPropertyController> newINDIFxNumberProperty = INDIFxFactory.newINDIFxNumberProperty();
        newINDIFxNumberProperty.controller().setIndi(indiProperty);
        return INDIFxPlatformThreadConnector.connect(newINDIFxNumberProperty.controller(), INDIPropertyListener.class, INDIFxAccess.class);
    }

    @Override
    public INDIPropertyListener createTextPropertyView(INDITextProperty indiProperty) throws INDIException {
        FxController<Tab, INDITextPropertyController> newINDIFxTextProperty = INDIFxFactory.newINDIFxTextProperty();
        newINDIFxTextProperty.controller().setIndi(indiProperty);
        return INDIFxPlatformThreadConnector.connect(newINDIFxTextProperty.controller(), INDIPropertyListener.class, INDIFxAccess.class);
    }

    @Override
    public INDIPropertyListener createSwitchPropertyView(INDISwitchProperty indiProperty) throws INDIException {
        FxController<Tab, INDISwitchPropertyController> newINDIFxSwitchProperty = INDIFxFactory.newINDIFxSwitchProperty();
        newINDIFxSwitchProperty.controller().setIndi(indiProperty);
        return INDIFxPlatformThreadConnector.connect(newINDIFxSwitchProperty.controller(), INDIPropertyListener.class, INDIFxAccess.class);
    }

    @Override
    public INDIPropertyListener createLightPropertyView(INDILightProperty indiProperty) throws INDIException {
        return null;
    }

    @Override
    public INDIDeviceListener createDeviceView(INDIDevice indiDevice) throws INDIException {
        FxController<Tab, INDIController<INDIDevice>> newINDIFxDevice = INDIFxFactory.newINDIFxDevice();
        newINDIFxDevice.controller().setIndi(indiDevice);
        return INDIFxPlatformThreadConnector.connect(newINDIFxDevice.controller(), INDIDeviceListener.class, INDIFxAccess.class);
    }

}
