package org.indilib.i4j.client.ui;

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

import org.indilib.i4j.Constants.PropertyPermissions;
import org.indilib.i4j.Constants.SwitchRules;
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
import org.indilib.i4j.client.INDIProperty;
import org.indilib.i4j.client.INDIPropertyListener;
import org.indilib.i4j.client.INDISwitchElement;
import org.indilib.i4j.client.INDISwitchProperty;
import org.indilib.i4j.client.INDITextElement;
import org.indilib.i4j.client.INDITextProperty;
import org.indilib.i4j.client.INDIViewCreatorInterface;

public class INDISwingViewCreator implements INDIViewCreatorInterface {

    @Override
    public INDIDeviceListener createDeviceListener(INDIDevice indiDevice) throws INDIException {
        return new org.indilib.i4j.client.ui.INDIDevicePanel(indiDevice);
    }

    @Override
    public INDIElementListener createBlobElementView(INDIBLOBElement indiblobElement, PropertyPermissions permission) throws INDIException {
        return new INDIBLOBElementPanel(indiblobElement, permission);
    }

    @Override
    public INDIElementListener createLightElementView(INDILightElement indiLightElement, PropertyPermissions permission) throws INDIException {
        return new INDILightElementPanel(indiLightElement);
    }

    @Override
    public INDIElementListener createNumberElementView(INDINumberElement indiNumberElement, PropertyPermissions permission) throws INDIException {
        return new INDINumberElementPanel(indiNumberElement, permission);
    }

    @Override
    public INDIElementListener createSwitchElementView(INDISwitchElement indiSwitchElement, PropertyPermissions permission) throws INDIException {
        return new INDISwitchElementPanel(indiSwitchElement, permission);
    }

    @Override
    public INDIElementListener createTextElementView(INDITextElement indiTextElement, PropertyPermissions permission) throws INDIException {
        return new INDITextElementPanel(indiTextElement, permission);
    }

    public INDIPropertyListener createDefaultPropertyView(INDIProperty indiProperty) throws INDIException {
        return new INDIDefaultPropertyPanel(indiProperty);
    }

    @Override
    public INDIPropertyListener createBlobPropertyView(INDIBLOBProperty indiProperty) throws INDIException {
        return createDefaultPropertyView(indiProperty);
    }

    @Override
    public INDIPropertyListener createNumberPropertyView(INDINumberProperty indiProperty) throws INDIException {
        return createDefaultPropertyView(indiProperty);
    }

    @Override
    public INDIPropertyListener createTextPropertyView(INDITextProperty indiProperty) throws INDIException {
        return createDefaultPropertyView(indiProperty);
    }

    @Override
    public INDIPropertyListener createSwitchPropertyView(INDISwitchProperty indiProperty) throws INDIException {
        if (indiProperty.getName().equals("CONNECTION")) {
            return new INDIConnectionPropertyPanel(indiProperty);
        } else if (indiProperty.getRule() == SwitchRules.ONE_OF_MANY) {
            return new INDISwitchOneOfManyPropertyPanel(indiProperty);
        } else {
            return createDefaultPropertyView(indiProperty);
        }
    }

    @Override
    public INDIPropertyListener createLightPropertyView(INDILightProperty indiProperty) throws INDIException {
        return createDefaultPropertyView(indiProperty);
    }
}
